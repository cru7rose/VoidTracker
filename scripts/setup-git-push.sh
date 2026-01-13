#!/bin/bash

# Setup Git Push to GitHub
# Usage: ./scripts/setup-git-push.sh [token|ssh|gh]

set -e

echo "╔════════════════════════════════════════╗"
echo "║   GIT PUSH SETUP - GITHUB             ║"
echo "╚════════════════════════════════════════╝"
echo ""

METHOD="${1:-token}"

case "$METHOD" in
  token)
    echo "🔑 Konfiguracja z Personal Access Token"
    echo ""
    echo "1. Utwórz token w GitHub:"
    echo "   https://github.com/settings/tokens"
    echo "   → Generate new token (classic)"
    echo "   → Scope: repo (full control)"
    echo ""
    read -p "2. Wklej token: " TOKEN
    
    if [ -z "$TOKEN" ]; then
      echo "❌ Token nie może być pusty"
      exit 1
    fi
    
    git remote set-url origin "https://${TOKEN}@github.com/cru7rose/VoidTracker.git"
    echo "✅ Remote skonfigurowany z tokenem"
    echo ""
    echo "🚀 Teraz możesz zrobić push:"
    echo "   git push origin main"
    ;;
    
  ssh)
    echo "🔐 Konfiguracja z SSH Key"
    echo ""
    
    # Sprawdź czy klucz istnieje
    if [ ! -f ~/.ssh/id_ed25519.pub ] && [ ! -f ~/.ssh/id_rsa.pub ]; then
      echo "📝 Generowanie nowego klucza SSH..."
      ssh-keygen -t ed25519 -C "cru7rose@github" -f ~/.ssh/id_ed25519 -N ""
      echo "✅ Klucz wygenerowany"
    fi
    
    # Znajdź klucz publiczny
    if [ -f ~/.ssh/id_ed25519.pub ]; then
      KEY_FILE=~/.ssh/id_ed25519.pub
    elif [ -f ~/.ssh/id_rsa.pub ]; then
      KEY_FILE=~/.ssh/id_rsa.pub
    else
      echo "❌ Nie znaleziono klucza SSH"
      exit 1
    fi
    
    echo ""
    echo "📋 Skopiuj poniższy klucz i dodaj do GitHub:"
    echo "   https://github.com/settings/keys"
    echo "   → New SSH key"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    cat "$KEY_FILE"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    read -p "Naciśnij Enter gdy dodasz klucz do GitHub..."
    
    # Test SSH connection
    echo "🔍 Testowanie połączenia SSH..."
    ssh -T git@github.com 2>&1 | grep -q "successfully authenticated" && echo "✅ SSH działa!" || echo "⚠️  SSH może wymagać konfiguracji"
    
    git remote set-url origin git@github.com:cru7rose/VoidTracker.git
    echo "✅ Remote skonfigurowany z SSH"
    echo ""
    echo "🚀 Teraz możesz zrobić push:"
    echo "   git push origin main"
    ;;
    
  gh)
    echo "📦 Instalacja GitHub CLI"
    echo ""
    
    if ! command -v gh &> /dev/null; then
      echo "📥 Instalowanie GitHub CLI..."
      curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg
      echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
      sudo apt update && sudo apt install -y gh
      echo "✅ GitHub CLI zainstalowany"
    else
      echo "✅ GitHub CLI już zainstalowany"
    fi
    
    echo ""
    echo "🔐 Logowanie do GitHub..."
    gh auth login
    
    echo "✅ Zalogowano"
    echo ""
    echo "🚀 Teraz możesz zrobić push:"
    echo "   git push origin main"
    ;;
    
  *)
    echo "❌ Nieznana metoda: $METHOD"
    echo ""
    echo "Użycie:"
    echo "  ./scripts/setup-git-push.sh token  # Personal Access Token"
    echo "  ./scripts/setup-git-push.sh ssh    # SSH Key"
    echo "  ./scripts/setup-git-push.sh gh     # GitHub CLI"
    exit 1
    ;;
esac
