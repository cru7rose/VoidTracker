# 📊 GitHub Actions Build Logs - Agent Access Guide

## 🔍 Automatyczne Zbieranie Logów

GitHub Actions workflow automatycznie zbiera i analizuje logi buildu dla każdego serwisu.

## 📦 Artifacts z Logami

Po każdym buildzie (sukces lub failure) są dostępne następujące artifacts:

### 1. Build Logs (Pełne logi Maven)
- `commons-build-logs` - Logi buildu danxils-commons
- `iam-build-logs` - Logi buildu IAM Service
- `order-build-logs` - Logi buildu Order Service
- `planning-build-logs` - Logi buildu Planning Service

**Lokalizacja:** GitHub Actions → Workflow Run → Artifacts

**Zawartość:**
- Pełny output Maven (`mvn clean package`)
- Błędy kompilacji
- Ostrzeżenia
- Czas buildu

### 2. Build Summary (Podsumowanie)
- `build-summary` - Automatycznie wygenerowane podsumowanie

**Zawartość:**
- Status każdego serwisu (✅/❌)
- Czas buildu
- Kluczowe błędy (jeśli są)
- Linki do pełnych logów

## 🤖 Dostęp dla Agenta

### Opcja 1: GitHub Actions UI (Rekomendowane)
Agent może poprosić użytkownika o:
1. Otwarcie GitHub Actions: https://github.com/cru7rose/VoidTracker/actions
2. Wybranie workflow run
3. Pobranie artifacts z logami
4. Wklejenie logów do chatu

### Opcja 2: GitHub API (Automatyczne)
Agent może użyć GitHub API do pobrania logów:

```bash
# Pobierz listę workflow runs
curl -H "Authorization: token $GITHUB_TOKEN" \
  https://api.github.com/repos/cru7rose/VoidTracker/actions/runs

# Pobierz artifacts dla konkretnego run
curl -H "Authorization: token $GITHUB_TOKEN" \
  https://api.github.com/repos/cru7rose/VoidTracker/actions/runs/{run_id}/artifacts

# Pobierz logi dla konkretnego job
curl -H "Authorization: token $GITHUB_TOKEN" \
  https://api.github.com/repos/cru7rose/VoidTracker/actions/runs/{run_id}/jobs/{job_id}/logs
```

### Opcja 3: Webhook (Przyszłość)
Można dodać webhook, który automatycznie wysyła podsumowanie buildu na serwer.

## 📋 Przykładowe Zapytania dla Agenta

### "Sprawdź ostatni build"
Agent powinien:
1. Sprawdzić ostatni workflow run w GitHub Actions
2. Pobrać build-summary artifact
3. Przeanalizować status każdego serwisu
4. Zasugerować działania jeśli są błędy

### "Dlaczego IAM Service nie zbudował się?"
Agent powinien:
1. Pobrać `iam-build-logs` artifact
2. Przeszukać logi pod kątem błędów
3. Zidentyfikować przyczynę (compilation error, dependency issue, etc.)
4. Zasugerować naprawę

### "Porównaj buildy z ostatnich 3 dni"
Agent powinien:
1. Pobrać build-summary z ostatnich 3 workflow runs
2. Porównać czasy buildu
3. Zidentyfikować trendy (szybsze/wolniejsze)
4. Zasugerować optymalizacje

## 🔧 Analiza Logów

### Typowe Błędy w Logach

#### 1. Compilation Errors
```
[ERROR] /path/to/file.java:[line] error: cannot find symbol
```
**Analiza:** Brakująca zależność lub błąd składni

#### 2. Dependency Issues
```
[ERROR] Failed to execute goal ... Could not resolve dependencies
```
**Analiza:** Problem z Maven dependencies

#### 3. Test Failures
```
[ERROR] Tests run: 10, Failures: 2, Errors: 0
```
**Analiza:** Testy nie przeszły (ale build może być z `-DskipTests`)

#### 4. Memory Issues
```
java.lang.OutOfMemoryError: Java heap space
```
**Analiza:** Niewystarczająca pamięć dla Maven

## 📊 Build Summary Format

Przykładowe podsumowanie:

```markdown
# 📊 Build Summary

**Workflow Run:** 7
**Commit:** abc12345
**Branch:** main
**Triggered by:** cru7rose

## Build Status

✅ **commons**: Build Success
   - Build time: 45s

✅ **iam**: Build Success
   - Build time: 120s

❌ **order**: Build Failed
```
[ERROR] Compilation failure
[ERROR] /path/to/OrderService.java:15: error: cannot find symbol
```

✅ **planning**: Build Success
   - Build time: 180s

## Full Logs

Download build logs from artifacts:
- commons-build-logs
- iam-build-logs
- order-build-logs
- planning-build-logs
```

## 🚀 Quick Start

1. **Sprawdź ostatni build:**
   - Otwórz: https://github.com/cru7rose/VoidTracker/actions
   - Kliknij na ostatni workflow run
   - Pobierz artifact `build-summary`

2. **Analizuj błędy:**
   - Jeśli build failed, pobierz odpowiedni `*-build-logs` artifact
   - Przeszukaj logi pod kątem `ERROR` lub `BUILD FAILURE`

3. **Dla Agenta:**
   - Poproś użytkownika o wklejenie logów lub podsumowania
   - Albo użyj GitHub API (jeśli masz token)

## 📚 Related Documentation

- [Agent Log Analysis](AGENT_LOG_ANALYSIS.md) - Szczegółowy przewodnik analizy
- [CI/CD Setup](.github/CICD_SETUP.md) - Konfiguracja CI/CD
- [Server Startup Workflow](SERVER_STARTUP_WORKFLOW.md) - Workflow na serwerze
