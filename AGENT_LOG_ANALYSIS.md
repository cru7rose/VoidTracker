# 🤖 Agent Log Analysis Guide

## 📋 Overview

Ten dokument opisuje jak agent może analizować logi z GitHub Actions i serwera dla VoidTracker.

## 🔍 Logi Dostępne dla Agenta

### 1. GitHub Actions Logs (Główne źródło)

**Dostęp:**
- GitHub API: `GET /repos/cru7rose/VoidTracker/actions/runs`
- GitHub UI: https://github.com/cru7rose/VoidTracker/actions

**Zawartość:**
- ✅ Logi kompilacji Maven dla każdego serwisu
- ✅ Błędy buildów (compilation errors, test failures)
- ✅ Czas buildów (performance metrics)
- ✅ Status każdego serwisu (success/failure)
- ✅ Artifacts (JAR files)

**Przykład analizy:**
```python
# Pseudo-kod dla agenta
workflow_runs = github_api.get_workflow_runs("build-and-deploy.yml")
for run in workflow_runs:
    logs = run.get_logs()
    if "BUILD FAILURE" in logs:
        analyze_build_error(logs)
    if "OutOfMemoryError" in logs:
        suggest_memory_fix()
```

### 2. Server Build Logs

**Lokalizacja:**
- `/root/VoidTracker/logs/server-sync.log` - Logi pull & build
- `/root/VoidTracker/logs/webhook-pull-build.log` - Logi webhook

**Zawartość:**
- ✅ Git pull status
- ✅ Maven build output
- ✅ Błędy lokalnego builda
- ✅ Timestamps

### 3. Service Runtime Logs

**Lokalizacja:**
- `/root/VoidTracker/logs/iam-service.log`
- `/root/VoidTracker/logs/order-service.log`
- `/root/VoidTracker/logs/planning-service.log`

**Zawartość:**
- ✅ Application logs
- ✅ Errors i exceptions
- ✅ Performance metrics

## 📊 Struktura Logów

### GitHub Actions Log Format

```
[timestamp] Building IAM Service...
[timestamp] [INFO] Scanning for projects...
[timestamp] [INFO] Building danxils-commons
[timestamp] [ERROR] Compilation failure
[timestamp] [ERROR] /path/to/file.java:[line] error message
```

### Server Sync Log Format

```
[2026-01-12 12:34:56] ╔════════════════════════════════════════╗
[2026-01-12 12:34:56] ║   GIT SYNC & BUILD                     ║
[2026-01-12 12:34:56] 📥 Fetching from GitHub...
[2026-01-12 12:34:57] 🔄 New changes detected: abc12345
[2026-01-12 12:34:58] 🔨 Building IAM Service...
[2026-01-12 12:35:30] ✅ IAM Service built
```

## 🔧 Analiza przez Agenta

### Typowe Problemy i Rozwiązania

#### 1. Build Failures

**Symptomy:**
- `[ERROR] Compilation failure`
- `BUILD FAILURE` w GitHub Actions

**Analiza:**
```bash
# Agent powinien sprawdzić:
1. Który serwis nie zbudował się?
2. Jaki błąd kompilacji?
3. Czy to błąd zależności?
4. Czy to błąd składni?
```

**Przykładowe odpowiedzi:**
- "IAM Service build failed: missing dependency danxils-commons"
- "Order Service compilation error: cannot find symbol 'OrderEntity'"

#### 2. Memory Issues

**Symptomy:**
- `OutOfMemoryError`
- `BUILD FAILED` z timeout

**Analiza:**
```bash
# Agent powinien sprawdzić:
1. Czy MAVEN_OPTS jest ustawione?
2. Czy serwer ma wystarczającą pamięć?
3. Czy build używa zbyt wielu wątków?
```

**Przykładowe odpowiedzi:**
- "Build failed due to insufficient memory. Current MAVEN_OPTS: -Xmx2g"
- "Consider reducing Maven threads: -T 1C"

#### 3. Test Failures

**Symptomy:**
- `Tests run: 10, Failures: 2`
- `BUILD FAILURE` po testach

**Analiza:**
```bash
# Agent powinien sprawdzić:
1. Które testy nie przeszły?
2. Czy to flaky tests?
3. Czy to problem z konfiguracją?
```

#### 4. Deployment Issues

**Symptomy:**
- `Deploy failed`
- `SSH connection timeout`

**Analiza:**
```bash
# Agent powinien sprawdzić:
1. Czy SSH credentials są poprawne?
2. Czy serwer jest dostępny?
3. Czy JAR został zbudowany?
```

## 📝 Przykłady Zapytań dla Agenta

### 1. Sprawdź ostatni build

```python
# Pseudo-kod
latest_run = github_api.get_latest_workflow_run("build-and-deploy.yml")
if latest_run.status == "failure":
    logs = latest_run.get_logs()
    errors = extract_errors(logs)
    return f"Build failed: {errors}"
```

### 2. Analiza trendów buildów

```python
# Pseudo-kod
runs = github_api.get_workflow_runs("build-and-deploy.yml", limit=10)
success_rate = calculate_success_rate(runs)
avg_build_time = calculate_avg_build_time(runs)
return f"Success rate: {success_rate}%, Avg time: {avg_build_time}s"
```

### 3. Porównanie buildów

```python
# Pseudo-kod
current_run = github_api.get_latest_workflow_run()
previous_run = github_api.get_workflow_run(current_run.number - 1)

if current_run.status == "failure" and previous_run.status == "success":
    diff = get_changes_between_runs(previous_run, current_run)
    return f"Build broke after: {diff}"
```

## 🎯 Best Practices dla Agenta

### 1. Analiza Logów

- ✅ **Czytaj pełne logi** - nie tylko błędy
- ✅ **Sprawdzaj kontekst** - co było przed błędem?
- ✅ **Porównuj z poprzednimi buildami** - co się zmieniło?
- ✅ **Identyfikuj wzorce** - czy to powtarzający się problem?

### 2. Raportowanie

- ✅ **Konkretne błędy** - nie ogólniki
- ✅ **Sugestie naprawy** - co można zrobić?
- ✅ **Linki do logów** - gdzie znaleźć szczegóły?
- ✅ **Priorytetyzacja** - co jest krytyczne?

### 3. Automatyzacja

- ✅ **Monitoruj buildy** - alerty przy failure
- ✅ **Analizuj trendy** - czy buildy się pogarszają?
- ✅ **Sugeruj optymalizacje** - jak przyspieszyć buildy?

## 🔗 API Endpoints

### GitHub API

```bash
# List workflow runs
GET /repos/cru7rose/VoidTracker/actions/runs

# Get specific run
GET /repos/cru7rose/VoidTracker/actions/runs/{run_id}

# Get run logs
GET /repos/cru7rose/VoidTracker/actions/runs/{run_id}/logs

# Get artifacts
GET /repos/cru7rose/VoidTracker/actions/artifacts
```

### Server Logs (SSH)

```bash
# Read server sync log
ssh user@server "tail -100 /root/VoidTracker/logs/server-sync.log"

# Read service logs
ssh user@server "tail -100 /root/VoidTracker/logs/iam-service.log"
```

## 📚 Przykładowe Scenariusze

### Scenariusz 1: Build Failure

**Problem:** IAM Service nie buduje się

**Analiza agenta:**
1. Sprawdź logi GitHub Actions dla IAM Service
2. Znajdź błąd kompilacji
3. Sprawdź czy zależności są zbudowane
4. Zasugeruj naprawę

**Odpowiedź:**
```
❌ IAM Service build failed

Error: cannot find symbol: class BaseVoidEntity
Location: modules/nexus/iam-service/src/main/java/.../UserEntity.java:15

Analysis:
- danxils-commons dependency not built
- Build order issue

Fix:
1. Build danxils-commons first:
   cd modules/nexus/danxils-commons && mvn clean install

2. Then build IAM Service:
   cd modules/nexus/iam-service && mvn clean package
```

### Scenariusz 2: Slow Builds

**Problem:** Buildy trwają zbyt długo

**Analiza agenta:**
1. Sprawdź czas buildów w ostatnich 10 runs
2. Porównaj z poprzednimi buildami
3. Zidentyfikuj bottleneck

**Odpowiedź:**
```
⚠️ Build Performance Degradation

Current avg build time: 15m 30s
Previous avg (last week): 8m 15s
Increase: +87%

Analysis:
- Planning Service build time increased from 3m to 10m
- Possible causes:
  * Memory issues (check MAVEN_OPTS)
  * Dependency resolution slow
  * Network issues

Recommendations:
1. Increase Maven memory: MAVEN_OPTS="-Xmx4g"
2. Use Maven dependency cache
3. Check network connectivity
```

## 🚀 Quick Start dla Agenta

```python
# Przykładowy kod dla agenta
import requests

def analyze_latest_build():
    # Get latest workflow run
    response = requests.get(
        "https://api.github.com/repos/cru7rose/VoidTracker/actions/runs",
        headers={"Authorization": "token YOUR_TOKEN"}
    )
    runs = response.json()["workflow_runs"]
    latest = runs[0]
    
    # Get logs
    logs_url = latest["logs_url"]
    logs = requests.get(logs_url).text
    
    # Analyze
    if "BUILD FAILURE" in logs:
        return "Build failed - check logs for details"
    elif "OutOfMemoryError" in logs:
        return "Memory issue - increase MAVEN_OPTS"
    else:
        return "Build successful"
```

## 📖 Related Documentation

- [Server Git Workflow](SERVER_GIT_WORKFLOW.md) - Pełny workflow
- [CI/CD Setup](.github/CICD_SETUP.md) - Konfiguracja CI/CD
- [GitHub Actions Docs](https://docs.github.com/en/actions)
