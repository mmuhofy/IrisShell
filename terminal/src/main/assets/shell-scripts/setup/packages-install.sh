#!/usr/bin/env bash
# Iris Shell base-package install.
#   packages-install.sh — runs INSIDE proot. Installs the curated base-pack:
#   zsh, git, curl, ca-certificates, nano, vim, tree.
#
# Idempotent: apt-get skips already-installed packages.

set -euo pipefail

export DEBIAN_FRONTEND=noninteractive

echo "packages-install: updating apt lists..."
apt-get update -qq
echo "packages-install: installing base packages..."
apt-get install -y --no-install-recommends \
    zsh \
    git \
    curl \
    ca-certificates \
    nano \
    vim \
    tree

echo "packages-install: ok"
