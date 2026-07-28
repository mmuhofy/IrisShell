# Block Engine — Mimari Plan

**Durum**: PLAN modu — implementasyon henüz başlamadı.
**Tarih**: 28 Temmuz 2026
**Faz**: Phase 1 — Terminal Core (AGENT.md §152-153)

---

## 1. Hedef

PTY byte akışını **command→output→prompt** bloklarına bölmek ve her
bloğu bağımsız bir UI kartı olarak göstermek. Eylemler: copy + collapse.
Sonsuz scrollback hissi, RAM dostu. Klasik terminal görünümüne
**full-kart** kabuğuyle sarılır (B konsepti — prompt+output tek kart).

---

## 2. Konsolide Kararlar

| Madde | Karar | Mockup referans |
|------|------|----------------|
| Block kapsamı | Sadece terminal blockları (command/stdout/stderr/running), agent kartları Phase 6 | block-output.html |
| Görünüm | **B full-kart** — prompt+output tek kart, radius 6, sol 2px stroke | B paneli |
| Wrap modu | Flush-left wrap (en-soldan), `word-break: break-all`, hanging-indent yok | spotlight ❷ |
| Block state renkleri | Sol 2px stroke: gold/error/build/success. Prompt text sabit gold | B paneli |
| Toolbar | Desktop hover, Mobile long-press → context menu (Copy / Collapse) | B paneli |
| Long output | 50+ satır → otomatik `⋯ N lines collapsed ⋯` divider | spotlight (B) |
| Scrollback | `LazyColumn` virtualized + engine ring buffer max 5000 satır | — |
| Active prompt | Sticky-bottom — klavyenin üstünde sabit | — |
| Parser | YNC state machine — prompt-aware + ANSI passthrough (Termux zaten parse eder) | §4 |
| Block ID | Runtime UUID — Phase 1'de disk'e yazılmaz, session kapanınca drop | — |
| Render katmanı | Compose `LazyColumn`, Termux `TerminalView` kullanılmaz; sadece TerminalEmulator + TerminalBuffer modeli okunur | §6 |
| ANSI parser | Termux vendored'dan faydalanır; biz block parsing yaparız | §4.4 |

---

## 3. Dosya Yerleşimi

> AGENT.md §107-145 tablosuna göre **layer responsibilities**.

```
terminal/
  src/main/kotlin/com/iris/irisshell/terminal/
    engine/
      TerminalBlock.kt          # data class — tek block modeli
      BlockState.kt             # sealed enum: Idle, Running, Success, Error, Cancelled
      BlockEngine.kt            # state machine — PTY→block parser + ring buffer owner
      BlockParsedOutput.kt      # parsed output segment (text + ANSI style spans) per block
      BlockRingBuffer.kt        # 5000 satırlık circular buffer (en eski drop)
      PromptDetector.kt         # yeni prompt regex tespiti — block kapatıcı sinyal
      ExitCodeProbe.kt          # $? echo ile exit code okuma (opsiyonel; Phase 1'in sonu)
    TerminalManager.kt          # MEVCUT — engine entegrasyonu buraya yapılır
    TerminalSessionClientImpl.kt # MEVCUT — onTextChanged → engine.append(...) çağırır
```

**DI hiyerarşisi**:

- `BlockEngine` `:data`'da değil, `:terminal`'da — bir terminal implementation detail'i.
- `:ui`, Hilt ile bir `BlockEnginePort` interface'sini `:domain`'den enjekte eder (clean architecture seam).
- `:ui`, `TerminalViewModel`'den `StateFlow<List<TerminalBlock>>` toplar ve `LazyColumn`'a basar.

**Domain interface'leri** (`:domain/src/main/kotlin/com/iris/irisshell/domain/terminal/`):
```
BlockRepository.kt       # interface — engine'in facade'si (UI'nin gördüğü)
  fun observe(): StateFlow<List<TerminalBlock>>
  fun appendOutput(bytes: ByteArray)
  fun startBlock(prompt: String, command: String)
  fun endBlock(exitCode: Int)
  fun cancelPending()
  fun setMaxScrollbackLines(n: Int)
```

---

## 4. Parser Stratejisi — YNC State Machine

### 4.1 Akış Diyagramı

```
PTY byte[]
    │
    ▼
TerminalSession (mevcut) → TerminalEmulator (mevcut, UTF-8/ANSI parser!)
    │
    ▼ TerminalBuffer (mevcut, cursor+screen)
    │
    ▼ TerminalSessionClientImpl.onTextChanged(session)   [mevcut]
    │
    ▼ BlockEngine.onTerminalBufferChanged(emulator)         [YENİ]
    │
    ├─ 1. getCursorRow/Col → cursor nerede?
    ├─ 2. getScreen().getTranscriptText() → tüm scrollback
    ├─ 3. PromptDetector.isNewPromptLine(screen, row) → prompt geldi mi?
    │
    ├─[ yeni prompt ]── ▶ mevcut block KAPAT (state=Success/Error), yeni block AÇ (state=Running)
    ├─[ exit-code-probe aktif ve $? değeri geldi ]── ▶ mevcut block.exitCode doldur, state=Success/Error
    ├─[ output halâ akıyor ]── ▶ mevcut block.output'a satır append et, ringbuffer'a yaz
    └─[ session bitti ]── ▶ mevcut block.state=Success(exitCode=0) ya da Error(exitCode!=0)
```

### 4.2 Block State Akışı

```
      ┌─────────┐
      │   Idle  │  ◀── engine boot — active prompt var, komut girilmedi
      └────┬────┘
           │ kullanıcı Enter'a bastı
           ▼
      ┌─────────┐
      │ Running │  ◀── output akıyor, exit code bilinmiyor
      └────┬────┘
           │ exit code geldi ya da yeni prompt
   ┌───────┴────────┐
   │                │
   ▼                ▼
┌──────┐       ┌──────┐
│ Ok   │       │ Err  │
│ exit=0│       │ exit≠0│
└──────┘       └──────┘
   │
   │ yeni prompt → bir sonraki Idle/Running block
   ▼
```

### 4.3 PromptDetector — Prompt Regex Stratejisi

**Problem**: Iris Shell zsh içinden şu PROMPT string'i yazar:
```
PROMPT='%F{yellow}%n@iris-shell%f:%F{blue}%~%f$ '
```
Yani prompt çıktısı şudur (örnek):
```
muhofy@iris-shell:~/IrisShell$ █
```
Bunu yakalamak için regex:
```
^[^\n]*?@iris-shell:[^\n]*?\$\s*$
```

**Daha sağlam**: prompt'un son karakteri sabit `$ ` — yani **satır sonu `$` + opsiyonel boşluk**. Ama bash komutları da `$` ile biten argüman içerebilir (`echo 'foo$bar'`). Bu yüzden **kullanıcı Enter bastığında** prompt-line içerigini tagged marker ile capture ederiz:

Phase 1'de en basit yaklaşım: **PromptDetector her yeni satırda prompt-pattern eşleşmesi kontrol eder**. Exit code çıkışı için kullanıcı komutun sonuna `; echo $?`  eklenebilir — biz otomatik ekleyip gizleriz (Phase 1'in sonunda). Veya exit-code-probe geçici olarak "bilinmeyen" state'inde bırakırız.

### 4.4 ANSI Parsing — OUT-OF-SCOPE

Termux `TerminalEmulator.processByte()` zaten ANSI escape (SGR renkler, cursor control, alt screen, line wrap) işler — `TerminalBuffer.mLines[]` line'larına `TerminalRow` olarak yazar, her cell'de `TextStyle` ile SGR state'i encode edilir.

Block Engine **kendi ANSI parser'ı yazmaz**. Bunun yerine:

1. Termux `TerminalBuffer` `getTranscriptText()` → plain text
2. `TerminalRow` cell'leri tek tek gezip `TextStyle` → `AnnotatedString.SpanStyle` map'lenir

→ Kompleks ANSI parser 0 iş. Sadece render-side span eşlemesi gerek (bağlam: 8 renk → Iris palette).

---

## 5. Model Sınıfları

### 5.1 BlockState

```kotlin
sealed class BlockState {
    object Idle     : BlockState()
    object Running  : BlockState()
    data class Success(val exitCode: Int) : BlockState()
    data class Error(val exitCode: Int)   : BlockState()
    object Cancelled: BlockState()
}
```

### 5.2 BlockParsedOutput

Block'un çıkışını satır satır depolar. Her satırda **AnnotatedSegment** listesi var (text + style).

```kotlin
data class AnnotatedSegment(
    val text: String,
    val style: SpanStyle,           // bold, italic, foreground (Iris palette)
    val isStderr: Boolean = false,
)

data class BlockParsedLine(
    val segments: List<AnnotatedSegment>,
    val raw: String,                // kolaysa copy için ham text
)

data class BlockParsedOutput(
    val lines: List<BlockParsedLine>,
    val totalLineCount: Int,         // collapsed'de bile toplam sayı görünür
    val isCollapsedInMiddle: Boolean,
    val collapsedRange: IntRange?   // 50+ satırda middle'ı gizle
)
```

### 5.3 TerminalBlock

```kotlin
data class TerminalBlock(
    val id: String,                  // UUID
    val prompt: String,              // "muhofy@iris-shell:~/IrisShell$"
    val command: String,             // "ls -la"
    val output: BlockParsedOutput,
    val state: BlockState,
    val startedAtMs: Long,
    val endedAtMs: Long?,
    val isCollapsed: Boolean = false,
    val sourceLines: IntRange,       // TerminalBuffer satır aralığı — edit işlemi için
)
```

---

## 6. Render Mimarisı (Compose katmanı)

> Per AGENT.md §139: `:ui` `:terminal` modülüne **doğrudan** erişmez.
> Bunun yerine `:domain`'deki `BlockRepository` interface üzerinden

### 6.1 Dolambaçlı Veri Yolu

```
TerminalSession (terminal/)
    │
    ▼ TerminalEmulator.append(bytes) — byte akışı işlenir
    │
    ▼ TerminalSessionClientImpl.onTextChanged     [terminal/]
    │
    ▼ BlockEngine.onTerminalBufferChanged          [terminal/]
    │
    ▼ BlockRepository.observe()                   [domain/]
    │
    ▼ TerminalViewModel.blocks                     [ui/]
    │
    ▼ LazyColumn { BlockCard(block) }              [ui/ Compose]
```

### 6.2 Compose Bileşenleri (`:ui/src/main/kotlin/com/iris/irisshell/ui/block/`)

```
TerminalBlockList.kt       # LazyColumn — pre-block boşluk + active prompt align
BlockCard.kt               # single block kart — sol stroke + prompt-row + output
BlockToolbar.kt            # hover/long-press toolbar (Copy / Collapse)
BlockOutputArea.kt         # parsed output render — AnnotatedString Text
CollapsedDivider.kt        # "⋯ N more lines collapsed ⋯" — tıklanınca expand
ActivePrompt.kt            # en alttaki komut-input — sticky
```

### 6.3 BlockCard Davranışı (B full-kart)

```kotlin
@Composable
fun BlockCard(block: TerminalBlock, onCopy: () -> Unit, onToggleCollapse: () -> Unit) {
    Surface(
        color = IrisSurface,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(start = 2.dp, color = stateStroke(block.state)),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Column {
            PromptRow(block.prompt, block.command, block.startedAtMs)
            BlockOutputArea(block.output, block.isCollapsed)
        }
    }
}
```

`stateStroke()` mapping:
- `Idle     → IrisSurfaceVariant` (prompt henüz komut yok)
- `Running  → IrisBuild` (blue)
- `Success  → IrisSuccess` (green)
- `Error    → IrisError` (red)
- `Cancelled→ IrisTextMuted` (gray)

---

## 7. TK+ Impl Inline (Termux API çağrıları)

Aşağıdaki Termux API'leri kullanılacak — vendor kodundan **doğrulanmış**:

| API | Kullanım | Vendor dosya |
|-----|---------|--------------|
| `TerminalEmulator.append(byte[], length)` | PTY byte akışı | TerminalEmulator.java:500 |
| `TerminalEmulator.getScreen(): TerminalBuffer` | tüm buffer | TerminalEmulator.java:347 |
| `TerminalBuffer.getTranscriptText(): String` | full scrollback text | TerminalBuffer.java:40 |
| `TerminalBuffer.mLines` (package-private) | satır başına cell-level style | TerminalBuffer.java:13 |
| `TerminalRow.getCell(`), `getStyle()` | hücrenin SGR state'i | TerminalRow.java (vendor) |
| `TerminalEmulator.getCursorRow()` | cursor satırı | TerminalEmulator.java:424 |
| `TerminalEmulator.getCursorCol()` | cursor kolonu | TerminalEmulator.java:428 |
| `TerminalSession.updateSize(rows, cols)` | ekran rotate'te | TerminalSession.java (vendor) |

**Şüphe bayrağı** — `mLines` package-private. `TerminalBuffer` ile aynı package'ta bir **shim class** yazmamız gerekebilir (örn `com.termux.terminal.IrisBufferAccessor`) — ya da Termux yeniden derleyip public yaparız. **Karar ertelendi** — impl başlarken denenecek.

---

## 8. Block Ring buffer

Maksimum 5000 satır. FIFO. `ArrayDeque<BlockParsedLine>` — basit ve verimli. Eğer limit aşılırsa **en eski block tamamen drop** edilir (satır satır değil) — bu block bütünlüğü için önemli.

Motor bellek tahmini: 5000 satır × 80 char × 2 byte ≈ 800 KB — güvenli.

---

## 9. Test Stratejisi

`astır/prompt sequence'larını **ekran yenileme** olmadan synthetic feed edebiliriz.

`terminal/src/test/kotlin/com/iris/irisshell/terminal/engine/`:
```
BlockEngineTest.kt              # Basit prompt→command→exit→prompt akışı
PromptDetectorTest.kt          # Regex eşleşme + false-positive
RingBufferTest.kt              # max satır drop davranışı
ExitCodeProbeTest.kt           # `$?` okuma (tartışmalı, opsiyonel)
```

---

## 10. Sıralı Uygulama Adımları

> AGENT.md §152: faz sırası korumalı. Phase 1 içinde block engine eklenir.

1. **Step 1 — Model sınıfları**: `BlockState`, `BlockParsedOutput`, `AnnotatedSegment`, `TerminalBlock`. Pure data classes. Unit test yok — data class.
2. **Step 2 — Ring buffer**: `BlockRingBuffer` + Unit test.
3. **Step 3 — PromptDetector** + Unit test — prompt regex.
4. **Step 4 — BlockEngine demo with synthetic session** — `BlockEngineTest`. Bir sahte PTY byte dizisi → block list al. `TerminalSession`'ı gerçekten bağlamadan önce MCP-inline test.
5. **Step 5 — Engine → TerminalSessionClientImpl** binding — `onTextChanged`'e `engine.onTerminalBufferChanged()` entegre.
6. **Step 6 — `:domain` interface (`BlockRepository`)** + `:data` bindings + Hilt module.
7. **Step 7 — `:ui` BlockCard + BlockOutputArea + Compose rendering** (Termux TerminalView'i kaldır).
8. **Step 8 — ActivePrompt Compose** — `BasicTextField` ile, sticky-bottom, klavye handle.
9. **Step 9 — Long-output collapse + toolbar** (Copy/Collapse).
10. **Step 10 — Long-press context menu** (mobile).

Her step sonunda commit + CI. AGENT.md "Git Rules" her implementasyon sonunda push.

---

## 11. Bilinmeyenler — doğrulanması gerek

- **TerminalBuffer.mLines package-private** — impl başlangıcında pratik test gerek. Eğer erişilemezse, Termux yeniden derlenip `mLines` ya da accessor metot public yapılacak (vendored olduğu için mümkün).
- **Exit code probe** — kullanıcının komutuna nasıl `$?` enjekte ederiz? Sentilenmiş prompt marker `\x1b]` olabilir. Phase 1'in sonundaki iş.
- **Mobil IME sticky-bottom** — Compose `imePadding()` modifier + `Scaffold` ile. Küçük bir hack gerekebilir.
- **Selection/copy mode** — Phase 1'de dışarıda. Block-level copy (toolbar butonu) yeterli.

---

## 12. Açık Bırakılanlar

- **Block hareket自己的孩子** (drag to reorder, mark as error/user-flag): Phase 5'te. Phase 1'de değil.
- **Search in block** — Phase 5.
- **Block-level "ship-it"**rate downvote): Phase 5 / 6.
- **Persistency** (session kaydet): Phase 5.
- **Multi-cursor / split-screen**: Phase 5.

---

## 13. Beklenen Sonuç

Bu plan uygulandığında:
- Kullanıcı terminalde komut yazıp Enter basınca → prompt-kart + command-kart tek kart halinde gösterilir
- Komut çalışırken sol çizgi **mavi**, output alt satırlara akarken kart uzar
- Çıktı bittiği anda sol çizgi **yeşil** ya da **kırmızı** (exit code okuma bağlı) olur
- Yeni prompt geldiğinde yeni block açılır, eski block scroll-back'e taşınır
- Scrollback sonsuz hissediyor, RAM 800 KB ile sınırlı
- Long outputlar 50+ satırda otomatik collapse — kart şişmesini önler
- Mobile'da long-press → Copy / Collapse menüsü açılır

Sonraki adım: Step 1 — model sınıfları yazmak. PLAN onaylandıktan sonra.
