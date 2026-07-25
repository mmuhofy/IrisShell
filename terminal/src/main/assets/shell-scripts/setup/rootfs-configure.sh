#!/usr/bin/env bash
# Iris Shell rootfs configure.
#   rootfs-configure.sh — runs INSIDE proot against Ubuntu 24.04 rootfs.
#
# Writes:
#   /etc/resolv.conf, /etc/hostname, /etc/hosts
#   /etc/apt/sources.list (ports.ubuntu.com for arm64/armhf)
#   /home stubs + minimal .bashrc / .bash_profile
#   /etc/skel/.zshrc (basic zsh prompt)
#
# Idempotent: safe to re-run.

set -euo pipefail

write_basic_zshrc() {
    cat > /etc/skel/.zshrc <<'ZSHRC'
export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
export HOME=/home
export TERM=xterm-256color
export LANG=C.UTF-8
export TMPDIR=/tmp

HISTSIZE=5000
HISTFILESIZE=10000
HISTTIMEFORMAT="%F %T "
setopt SHARE_HISTORY HIST_IGNORE_DUPS HIST_IGNORE_SPACE

alias ll='ls -la'
alias la='ls -A'
alias l='ls -CF'
alias ..='cd ..'
alias ...='cd ../..'
alias grep='grep --color=auto'
alias df='df -h'
alias du='du -h'

PROMPT='%F{yellow}%n@iris-shell%f:%F{blue}%~%f$ '
RPROMPT='%F{cyan}%(?..✗ %?)%f'
ZSHRC
}

cat > /etc/resolv.conf <<'RESOLV'
nameserver 8.8.8.8
nameserver 8.8.4.4
RESOLV

echo "iris-shell" > /etc/hostname

cat > /etc/hosts <<'HOSTS'
127.0.0.1 localhost iris-shell
::1 localhost ip6-localhost ip6-loopback
HOSTS

# Drop base-image-specific sources file written by newer Ubuntu images
# (noble+ uses *.sources files; we ship classic .list for arm64 ports).
rm -f /etc/apt/sources.list.d/ubuntu.sources
cat > /etc/apt/sources.list <<'SOURCES'
deb http://ports.ubuntu.com/ubuntu-ports noble main restricted universe multiverse
deb http://ports.ubuntu.com/ubuntu-ports noble-updates main restricted universe multiverse
deb http://ports.ubuntu.com/ubuntu-ports noble-security main restricted universe multiverse
SOURCES

mkdir -p /home /root /tmp

# Minimal bashrc — script compatibility. Will be created in /etc/skel so any
# later user copies inherit it.
cat > /etc/skel/.bashrc <<'BASHRC'
export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
export HOME=/home
export TERM=xterm-256color
export LANG=C.UTF-8
export TMPDIR=/tmp
alias ll='ls -la'
alias la='ls -A'
alias l='ls -CF'
BASHRC

cat > /etc/skel/.bash_profile <<'BASHP'
if [ -f ~/.bashrc ]; then
    . ~/.bashrc
fi
BASHP

write_basic_zshrc

echo "rootfs-configure: ok"
