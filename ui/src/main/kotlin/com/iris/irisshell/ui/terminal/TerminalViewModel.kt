package com.iris.irisshell.ui.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iris.irisshell.domain.terminal.SetTerminalFontSizeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Holds the terminal font size + slider visibility state for
 * [TerminalScreen].
 *
 * Pinch gestures on the terminal area call [bumpFontSize] with a relative
 * fraction (factor > 1 grows the font, < 1 shrinks it). The new value is
 * clamped to [MIN_FONT_SP]..[MAX_FONT_SP] and pushed back into
 * [SetTerminalFontSizeUseCase] so the choice survives process death (DataStore).
 *
 * The slider is only visible while the user is actively pinching —
 * [onPinchEnd] flips [sliderVisible] back to false after a short grace
 * period ([SLIDER_HIDE_DELAY_MS]).
 */
@HiltViewModel
class TerminalViewModel @Inject constructor(
    setTerminalFontSize: SetTerminalFontSizeUseCase,
) : ViewModel() {

    private val persist = setTerminalFontSize

    val fontSizeSp: StateFlow<Int> = persist.observe()
        .map { it.toInt().coerceIn(MIN_FONT_SP, MAX_FONT_SP) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = DEFAULT_FONT_SP,
        )

    private val _sliderVisible = MutableStateFlow(false)
    val sliderVisible: StateFlow<Boolean> = _sliderVisible.asStateFlow()

    fun showSlider() {
        _sliderVisible.value = true
    }

    fun hideSlider() {
        _sliderVisible.value = false
    }

    /**
     * Slider thumb dragged by the user to [value] (in sp units).
     * Persisted and immediately published via [fontSizeSp].
     */
    fun setFontSize(value: Int) {
        val clamped = value.coerceIn(MIN_FONT_SP, MAX_FONT_SP)
        viewModelScope.launch { persist.set(clamped.toFloat()) }
    }

    /**
     * Apply a relative pinch factor. Pinch-out (factor > 1) increases the
     * font; pinch-in (factor < 1) decreases it.
     */
    fun bumpFontSize(factor: Float) {
        val current = fontSizeSp.value.toFloat()
        val target = (current * factor).coerceIn(MIN_FONT_SP.toFloat(), MAX_FONT_SP.toFloat())
        setFontSize(target.toInt())
    }

    fun toggleSlider(visible: Boolean) {
        _sliderVisible.value = visible
    }

    companion object {
        const val MIN_FONT_SP: Int = 10
        const val MAX_FONT_SP: Int = 32
        const val DEFAULT_FONT_SP: Int = 14
    }
}
