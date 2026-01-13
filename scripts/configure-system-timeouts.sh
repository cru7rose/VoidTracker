#!/bin/bash
# System Configuration Script for VoidTracker
# Configures TCP keepalive, connection timeouts, and system limits

set -e

echo "🔧 Configuring system timeouts and connection settings..."

# 1. TCP Keepalive Settings (prevent connection timeouts)
# These settings help prevent connections from timing out during long operations
if [ -f /proc/sys/net/ipv4/tcp_keepalive_time ]; then
    echo "net.ipv4.tcp_keepalive_time = 300" | sudo tee -a /etc/sysctl.conf > /dev/null 2>&1 || true
    echo "net.ipv4.tcp_keepalive_intvl = 30" | sudo tee -a /etc/sysctl.conf > /dev/null 2>&1 || true
    echo "net.ipv4.tcp_keepalive_probes = 5" | sudo tee -a /etc/sysctl.conf > /dev/null 2>&1 || true
    sudo sysctl -w net.ipv4.tcp_keepalive_time=300 > /dev/null 2>&1 || true
    sudo sysctl -w net.ipv4.tcp_keepalive_intvl=30 > /dev/null 2>&1 || true
    sudo sysctl -w net.ipv4.tcp_keepalive_probes=5 > /dev/null 2>&1 || true
    echo "✅ TCP keepalive configured"
fi

# 2. Connection timeout settings
if [ -f /proc/sys/net/ipv4/tcp_fin_timeout ]; then
    echo "net.ipv4.tcp_fin_timeout = 30" | sudo tee -a /etc/sysctl.conf > /dev/null 2>&1 || true
    sudo sysctl -w net.ipv4.tcp_fin_timeout=30 > /dev/null 2>&1 || true
    echo "✅ TCP FIN timeout configured"
fi

# 3. Increase connection tracking table size (for many concurrent connections)
if [ -f /proc/sys/net/netfilter/nf_conntrack_max ]; then
    echo "net.netfilter.nf_conntrack_max = 262144" | sudo tee -a /etc/sysctl.conf > /dev/null 2>&1 || true
    sudo sysctl -w net.netfilter.nf_conntrack_max=262144 > /dev/null 2>&1 || true
    echo "✅ Connection tracking table size increased"
fi

# 4. Increase file descriptor limits (already high, but ensure it's set)
if [ -f /etc/security/limits.conf ]; then
    if ! grep -q "root.*soft.*nofile" /etc/security/limits.conf; then
        echo "root soft nofile 1048576" | sudo tee -a /etc/security/limits.conf > /dev/null 2>&1 || true
        echo "root hard nofile 1048576" | sudo tee -a /etc/security/limits.conf > /dev/null 2>&1 || true
        echo "* soft nofile 1048576" | sudo tee -a /etc/security/limits.conf > /dev/null 2>&1 || true
        echo "* hard nofile 1048576" | sudo tee -a /etc/security/limits.conf > /dev/null 2>&1 || true
        echo "✅ File descriptor limits configured"
    fi
fi

# 5. Increase TCP connection queue size
if [ -f /proc/sys/net/core/somaxconn ]; then
    echo "net.core.somaxconn = 4096" | sudo tee -a /etc/sysctl.conf > /dev/null 2>&1 || true
    sudo sysctl -w net.core.somaxconn=4096 > /dev/null 2>&1 || true
    echo "✅ TCP connection queue size increased"
fi

# 6. Increase TCP max syn backlog
if [ -f /proc/sys/net/ipv4/tcp_max_syn_backlog ]; then
    echo "net.ipv4.tcp_max_syn_backlog = 4096" | sudo tee -a /etc/sysctl.conf > /dev/null 2>&1 || true
    sudo sysctl -w net.ipv4.tcp_max_syn_backlog=4096 > /dev/null 2>&1 || true
    echo "✅ TCP max SYN backlog increased"
fi

# 7. Apply sysctl settings
sudo sysctl -p > /dev/null 2>&1 || true

echo "✅ System timeout configuration complete!"
echo ""
echo "Note: Some changes may require a reboot to take full effect."
echo "For immediate effect, settings have been applied to running kernel."
