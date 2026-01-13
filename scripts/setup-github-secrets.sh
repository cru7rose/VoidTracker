#!/bin/bash

# Setup GitHub Secrets Helper Script
# Generuje klucz SSH i wyświetla instrukcje

set -e

echo "╔════════════════════════════════════════╗"
echo "║   GITHUB SECRETS SETUP HELPER           ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Check if key already exists
KEY_FILE=~/.ssh/github_actions_deploy
if [ -f "$KEY_FILE" ]; then
  echo "⚠️  Klucz już istnieje: $KEY_FILE"
  read -p "Wygenerować nowy? (y/N): " REGEN
  if [ "$REGEN" != "y" ] && [ "$REGEN" != "Y" ]; then
    echo "✅ Używam istniejącego klucza"
  else
    rm -f "$KEY_FILE" "$KEY_FILE.pub"
    echo "🗑️  Usunięto stary klucz"
  fi
fi

# Generate key if needed
if [ ! -f "$KEY_FILE" ]; then
  echo "🔑 Generowanie nowego klucza SSH..."
  ssh-keygen -t ed25519 -C "github-actions-deploy" -f "$KEY_FILE" -N ""
  echo "✅ Klucz wygenerowany"
fi

# Get server info
SSH_HOST=$(hostname -I | awk '{print $1}')
SSH_USER=$(whoami)
SSH_PORT=$(grep -E "^Port" /etc/ssh/sshd_config 2>/dev/null | awk '{print $2}' || echo "22")
REMOTE_BASE=$(pwd)

echo ""
echo "╔════════════════════════════════════════╗"
echo "║   INFORMACJE O SERWERZE                ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo "📍 SSH Host: $SSH_HOST"
echo "👤 SSH User: $SSH_USER"
echo "🔌 SSH Port: $SSH_PORT"
echo "📁 Remote Base: $REMOTE_BASE"
echo ""

# Display private key
echo "╔════════════════════════════════════════╗"
echo "║   DEPLOY_SSH_KEY (SKOPIUJ CAŁĄ)        ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
cat "$KEY_FILE"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "⚠️  WAŻNE: Skopiuj CAŁĄ zawartość powyżej (od -----BEGIN do -----END)"
echo ""

# Display public key (for authorized_keys)
echo "╔════════════════════════════════════════╗"
echo "║   KLUCZ PUBLICZNY (dla authorized_keys) ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
cat "$KEY_FILE.pub"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Ask if add to authorized_keys
read -p "Dodać klucz publiczny do authorized_keys? (Y/n): " ADD_AUTH
if [ "$ADD_AUTH" != "n" ] && [ "$ADD_AUTH" != "N" ]; then
  if ! grep -q "$(cat $KEY_FILE.pub)" ~/.ssh/authorized_keys 2>/dev/null; then
    cat "$KEY_FILE.pub" >> ~/.ssh/authorized_keys
    chmod 600 ~/.ssh/authorized_keys
    echo "✅ Klucz dodany do authorized_keys"
  else
    echo "✅ Klucz już jest w authorized_keys"
  fi
fi

echo ""
echo "╔════════════════════════════════════════╗"
echo "║   GITHUB SECRETS - WARTOŚCI             ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo "1. DEPLOY_SSH_KEY:"
echo "   (skopiuj zawartość klucza prywatnego powyżej)"
echo ""
echo "2. DEPLOY_SSH_HOST:"
echo "   $SSH_HOST"
echo ""
echo "3. DEPLOY_SSH_USER:"
echo "   $SSH_USER"
echo ""
echo "4. DEPLOY_SSH_PORT:"
echo "   $SSH_PORT"
echo ""
echo "5. DEPLOY_REMOTE_BASE:"
echo "   $REMOTE_BASE"
echo ""

echo "╔════════════════════════════════════════╗"
echo "║   INSTRUKCJA DODAWANIA                 ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo "1. Otwórz: https://github.com/cru7rose/VoidTracker/settings/secrets/actions"
echo ""
echo "2. Dla każdego secret:"
echo "   - Kliknij 'New repository secret'"
echo "   - Wklej wartość z powyżej"
echo "   - Kliknij 'Add secret'"
echo ""
echo "3. Po dodaniu wszystkich secrets:"
echo "   ./scripts/git-push-from-server.sh 'test: Verify secrets'"
echo ""
echo "📖 Pełna instrukcja: GITHUB_SECRETS_SETUP.md"
echo ""
