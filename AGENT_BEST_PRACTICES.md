# 🤖 Najlepsze Praktyki dla Agenta AI na Serwerze

## 📋 Spis Treści
1. [Konfiguracja Użytkownika](#konfiguracja-użytkownika)
2. [Bezpieczeństwo SSH](#bezpieczeństwo-ssh)
3. [Ochrona Plików](#ochrona-plików)
4. [Monitorowanie](#monitorowanie)
5. [Rozwiązywanie Problemów](#rozwiązywanie-problemów)

---

## 🔐 Konfiguracja Użytkownika

### Utworzenie Ograniczonego Użytkownika

Uruchom skrypt konfiguracyjny:
```bash
sudo bash /root/VoidTracker/scripts/setup_restricted_agent_user.sh
```

Ten skrypt tworzy użytkownika `voidtracker-agent` z następującymi ograniczeniami:

#### ✅ **DOZWOLONE Operacje:**
- Czytanie plików projektu w `/root/VoidTracker`
- Czytanie logów Docker: `sudo docker logs <container>`
- Sprawdzanie statusu serwisów: `sudo systemctl status <service>`
- Czytanie logów systemowych: `sudo journalctl -u <service>`

#### ❌ **ZABRONIONE Operacje:**
- Modyfikacja kluczy SSH w `/root/.ssh/`
- Usuwanie plików systemowych
- Modyfikacja konfiguracji SSH w `/etc/ssh/`
- Zmiana uprawnień do katalogów systemowych

### Struktura Uprawnień

```
voidtracker-agent
├── ✅ ~/workspace/          (pełny dostęp)
├── ✅ /root/VoidTracker/    (tylko do odczytu)
├── ❌ /root/.ssh/           (zabroniony dostęp)
├── ❌ /etc/ssh/             (zabroniony dostęp)
└── ❌ /etc/sudoers*         (zabroniony dostęp)
```

---

## 🔒 Bezpieczeństwo SSH

### 1. Ochrona Kluczy SSH

#### Automatyczna Ochrona
```bash
# Ustaw immutable flag (nie można usunąć/modyfikować)
sudo chattr +i /root/.ssh/authorized_keys

# Sprawdź uprawnienia
ls -la /root/.ssh/
# Powinno być: -rw------- (600) dla kluczy prywatnych
# Powinno być: -rw-r--r-- (644) dla kluczy publicznych
```

#### Backup Kluczy
```bash
# Automatyczny backup (uruchamiany przez skrypt)
/root/.ssh_backup_YYYYMMDD_HHMMSS/
```

### 2. Konfiguracja SSH dla Stabilności

Uruchom skrypt poprawy stabilności:
```bash
sudo bash /root/VoidTracker/scripts/improve_ssh_stability.sh
```

#### Kluczowe Ustawienia:

**Na Serwerze (`/etc/ssh/sshd_config`):**
```ini
# Keep-Alive - zapobiega zrywaniu połączenia
ClientAliveInterval 60        # Wysyłaj ping co 60 sekund
ClientAliveCountMax 3         # Maksymalnie 3 brakujące odpowiedzi
TCPKeepAlive yes              # Użyj TCP keep-alive

# Timeout
LoginGraceTime 120            # Czas na zalogowanie
MaxStartups 10:30:100         # Limit równoczesnych połączeń

# Kompresja (pomaga przy wolnych połączeniach)
Compression yes
```

**Na Kliencie (`~/.ssh/config`):**
```ini
Host voidtracker
    ServerAliveInterval 60
    ServerAliveCountMax 3
    TCPKeepAlive yes
    ConnectTimeout 30
    # Automatyczne ponowne połączenie
    ControlMaster auto
    ControlPath ~/.ssh/control-%h-%p-%r
    ControlPersist 10m
```

### 3. Monitorowanie Połączeń SSH

#### Automatyczne Monitorowanie
```bash
# Sprawdzanie co 10 minut (skonfigurowane w crontab)
/usr/local/bin/monitor_ssh_connections.sh
```

#### Ręczne Sprawdzanie
```bash
# Aktywne połączenia
who
w

# Logi SSH
sudo journalctl -u ssh -f
sudo tail -f /var/log/auth.log

# Statystyki połączeń
sudo last | head -20
```

---

## 🛡️ Ochrona Plików

### 1. Ochrona Przed Usunięciem

#### Immutable Flag (Najsilniejsza Ochrona)
```bash
# Ustaw na krytyczne pliki
sudo chattr +i /root/.ssh/authorized_keys
sudo chattr +i /etc/ssh/sshd_config

# Sprawdź status
lsattr /root/.ssh/authorized_keys

# Usuń ochronę (tylko jeśli naprawdę potrzebne)
sudo chattr -i /root/.ssh/authorized_keys
```

#### Backup Automatyczny
```bash
# Backup kluczy SSH (uruchamiany przez skrypt setup)
# Lokalizacja: /root/.ssh_backup_YYYYMMDD_HHMMSS/
```

### 2. Monitorowanie Zmian

#### Monitor Kluczy SSH
```bash
# Automatyczne sprawdzanie co 5 minut
/usr/local/bin/monitor_ssh_keys.sh

# Logi: /var/log/ssh_key_monitor.log
```

#### Inotify (Zaawansowane)
```bash
# Monitoruj zmiany w czasie rzeczywistym
sudo apt-get install inotify-tools

# Przykład monitorowania
inotifywait -m /root/.ssh/ -e modify,delete,create
```

### 3. Kontrola Dostępu do Plików

#### AppArmor (Opcjonalne, Zaawansowane)
```bash
# Utwórz profil AppArmor dla agenta
sudo aa-genprof voidtracker-agent
```

#### SELinux (Jeśli używany)
```bash
# Ustaw kontekst dla katalogu agenta
sudo chcon -R -t user_home_t /home/voidtracker-agent/
```

---

## 📊 Monitorowanie

### 1. Logi Systemowe

#### SSH Logi
```bash
# Ostatnie 50 linii
sudo journalctl -u ssh -n 50

# Śledzenie w czasie rzeczywistym
sudo journalctl -u ssh -f

# Błędy SSH
sudo journalctl -u ssh | grep -i error
```

#### Logi Agenta
```bash
# Jeśli agent loguje do pliku
tail -f /var/log/voidtracker-agent.log

# Logi sudo (operacje agenta)
sudo cat /var/log/auth.log | grep voidtracker-agent
```

### 2. Metryki Połączeń

#### Sprawdzanie Statystyk
```bash
# Liczba aktywnych sesji
who | wc -l

# Długość sesji
w

# Historia połączeń
last | grep voidtracker-agent
```

### 3. Alerty

#### Email Alerty (Opcjonalne)
```bash
# Zainstaluj mailutils
sudo apt-get install mailutils

# Dodaj do skryptu monitorowania
echo "Alert: SSH key modified!" | mail -s "SSH Security Alert" admin@example.com
```

---

## 🔧 Rozwiązywanie Problemów

### Problem: Połączenie SSH się zrywa

#### Rozwiązanie 1: Sprawdź Keep-Alive
```bash
# Na serwerze
sudo grep -E "ClientAlive|TCPKeepAlive" /etc/ssh/sshd_config

# Powinno być:
# ClientAliveInterval 60
# ClientAliveCountMax 3
# TCPKeepAlive yes
```

#### Rozwiązanie 2: Sprawdź Firewall
```bash
# Sprawdź czy port 22 jest otwarty
sudo ufw status
sudo iptables -L -n | grep 22
```

#### Rozwiązanie 3: Sprawdź Timeouty
```bash
# Zwiększ timeout w ~/.ssh/config
ConnectTimeout 60
ServerAliveInterval 30
```

### Problem: Agent modyfikuje klucze SSH

#### Rozwiązanie 1: Sprawdź Uprawnienia
```bash
# Sprawdź czy immutable flag jest ustawiony
lsattr /root/.ssh/authorized_keys

# Jeśli nie, ustaw:
sudo chattr +i /root/.ssh/authorized_keys
```

#### Rozwiązanie 2: Sprawdź Sudoers
```bash
# Sprawdź konfigurację
sudo cat /etc/sudoers.d/voidtracker-agent

# Powinno zawierać:
# voidtracker-agent ALL=(ALL) !/usr/bin/rm -rf /root/.ssh/*
```

#### Rozwiązanie 3: Sprawdź Logi
```bash
# Sprawdź co agent próbował zrobić
sudo grep voidtracker-agent /var/log/auth.log
sudo journalctl -u ssh | grep voidtracker-agent
```

### Problem: Utrata Plików

#### Rozwiązanie 1: Sprawdź Backupy
```bash
# Lista backupów
ls -la /root/.ssh_backup_*/

# Przywróć z backupu
sudo cp /root/.ssh_backup_YYYYMMDD_HHMMSS/* /root/.ssh/
```

#### Rozwiązanie 2: Sprawdź Historię Git
```bash
# Jeśli projekt jest w Git
cd /root/VoidTracker
git log --all --full-history -- <file>
git checkout <commit> -- <file>
```

#### Rozwiązanie 3: Sprawdź Snapshots (Jeśli dostępne)
```bash
# Hetzner Cloud Snapshots
# Sprawdź w Hetzner Console → Servers → Snapshots
```

---

## ✅ Checklist Konfiguracji

### Przed Użyciem Agenta

- [ ] Użytkownik `voidtracker-agent` utworzony
- [ ] Ograniczenia sudoers skonfigurowane
- [ ] Klucze SSH chronione (immutable flag)
- [ ] Backup kluczy SSH utworzony
- [ ] Monitorowanie SSH skonfigurowane
- [ ] Konfiguracja SSH dla stabilności zastosowana
- [ ] Logi są monitorowane
- [ ] Alerty skonfigurowane (opcjonalne)

### Regularne Sprawdzanie

- [ ] Sprawdź logi SSH: `journalctl -u ssh -n 100`
- [ ] Sprawdź logi monitorowania: `/var/log/ssh_key_monitor.log`
- [ ] Sprawdź aktywność agenta: `sudo grep voidtracker-agent /var/log/auth.log`
- [ ] Sprawdź backupy: `ls -la /root/.ssh_backup_*/`
- [ ] Sprawdź uprawnienia: `lsattr /root/.ssh/authorized_keys`

---

## 🚀 Szybki Start

### 1. Pełna Konfiguracja (Jednym Poleceniem)
```bash
# Uruchom oba skrypty
sudo bash /root/VoidTracker/scripts/setup_restricted_agent_user.sh
sudo bash /root/VoidTracker/scripts/improve_ssh_stability.sh

# Zrestartuj SSH
sudo systemctl restart sshd
```

### 2. Weryfikacja
```bash
# Sprawdź użytkownika
id voidtracker-agent

# Sprawdź uprawnienia
sudo -l -U voidtracker-agent

# Sprawdź konfigurację SSH
sudo sshd -t
```

### 3. Test Połączenia
```bash
# Z lokalnego komputera
ssh voidtracker

# Z użytkownikiem agenta
sudo -u voidtracker-agent ssh root@localhost
```

---

## 📞 Wsparcie

W przypadku problemów:
1. Sprawdź logi: `journalctl -u ssh -f`
2. Sprawdź dokumentację: `cat /home/voidtracker-agent/AGENT_README.md`
3. Sprawdź backupy: `/root/.ssh_backup_*/`

---

**Ostatnia aktualizacja:** 2026-01-12  
**Wersja:** 1.0
