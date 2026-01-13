#!/bin/bash
# Script to add swap space to prevent OOM and SSH disconnections

set -e

SWAP_SIZE=${1:-4G}  # Default 4GB, can be overridden: ./add-swap.sh 8G
SWAP_FILE="/swapfile"

echo "╔════════════════════════════════════════╗"
echo "║   ADDING SWAP SPACE                    ║"
echo "╚════════════════════════════════════════╝"
echo ""
echo "📊 Current memory status:"
free -h
echo ""

# Check if swap already exists
if [ -f "$SWAP_FILE" ] || swapon --show | grep -q "$SWAP_FILE"; then
    echo "⚠️  Swap file already exists!"
    swapon --show
    read -p "Do you want to remove existing swap and create new one? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Aborted."
        exit 1
    fi
    echo "🔄 Removing existing swap..."
    swapoff "$SWAP_FILE" 2>/dev/null || true
    rm -f "$SWAP_FILE"
fi

echo "🔨 Creating ${SWAP_SIZE} swap file..."
echo "   This may take a few minutes..."

# Create swap file
sudo fallocate -l "$SWAP_SIZE" "$SWAP_FILE" || {
    echo "❌ fallocate failed, trying dd method (slower)..."
    sudo dd if=/dev/zero of="$SWAP_FILE" bs=1M count=$(echo "$SWAP_SIZE" | sed 's/G/*1024/' | bc) status=progress
}

# Set correct permissions
sudo chmod 600 "$SWAP_FILE"

# Format as swap
echo "📝 Formatting as swap..."
sudo mkswap "$SWAP_FILE"

# Enable swap
echo "✅ Enabling swap..."
sudo swapon "$SWAP_FILE"

# Make it permanent
if ! grep -q "$SWAP_FILE" /etc/fstab 2>/dev/null; then
    echo "💾 Making swap permanent..."
    echo "$SWAP_FILE none swap sw 0 0" | sudo tee -a /etc/fstab
fi

# Set swappiness (how aggressively to use swap)
# 10 = use swap only when RAM is 90% full (good for servers)
echo "⚙️  Setting swappiness to 10 (optimal for servers)..."
if ! grep -q "vm.swappiness" /etc/sysctl.conf 2>/dev/null; then
    echo "vm.swappiness=10" | sudo tee -a /etc/sysctl.conf
fi
sudo sysctl vm.swappiness=10

echo ""
echo "✅ Swap successfully added!"
echo ""
echo "📊 New memory status:"
free -h
echo ""
echo "💡 Tips:"
echo "   - Swap is slower than RAM, but prevents OOM"
echo "   - Monitor with: watch -n 1 'free -h'"
echo "   - To remove swap later: sudo swapoff /swapfile && sudo rm /swapfile"
