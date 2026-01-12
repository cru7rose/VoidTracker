# =============================================================================
# 1. KONFIGURACJA
# =============================================================================

$nazwaBazowa = "danxils-system"

# Ustawienie limitu na plik wynikowy (np. 90MB)
$maksymalnyRozmiar = 90MB 

# Limit wielkości POJEDYNCZEGO pliku źródłowego (np. 1MB).
# Jeśli plik kodu ma więcej niż 1MB, to zazwyczaj jest to zrzut bazy, 
# zminifikowana biblioteka lub log, a nie kod pisany przez człowieka.
$limitPojedynczegoPliku = 1MB

# Lista rozszerzeń - skupiamy się na logice
$uwzglednianeRozszerzenia = @(
    '.java', '.kt',      # Backend logic
    '.xml', '.properties', # Configs (ważne, ale uważamy na duże pliki)
    '.sql',              # Schema (z limitem rozmiaru)
    '.js', '.ts', '.vue' # Frontend logic
    # Usunięto .html i .css, chyba że są kluczowe, często generują szum w szablonach
)

# Katalogi ignorowane - AGRESYWNE FILTROWANIE
$wykluczoneKatalogi = @(
    'target', 'build', 'dist', 'out',       # Build artefacts
    '.mvn', '.git', '.idea', '.vscode',     # IDE & Git
    '.gradle', 'node_modules',              # Dependencies
    'test', 'tests', '__tests__', 'coverage', # Testy (często zbędne do analizy logiki)
    'assets', 'public', 'static', 'resources', # Często zawierają obrazy/czcionki/duże JSONy
    'logs', 'tmp', 'temp', 'doc', 'docs'    # Logi i dokumentacja
)

# Pliki ignorowane po nazwie (konkretne przypadki generujące szum)
$ignorowanePlikiNazwy = @(
    'package-lock.json', 'yarn.lock', 'pnpm-lock.yaml', # Dependency locks (ogromne)
    'stats.json', 'manifest.json' 
)

# =============================================================================
# 2. PRZYGOTOWANIE ŚRODOWISKA
# =============================================================================

$katalogRoboczy = (Get-Location).Path

# Budowanie regexa dla katalogów
$wzorzecWykluczeniaKat = ($wykluczoneKatalogi | ForEach-Object { [System.Text.RegularExpressions.Regex]::Escape($_) }) -join '|'
$wzorzecWykluczeniaKat = "([\\/])($wzorzecWykluczeniaKat)([\\/]|$)"

Write-Host "--------------------------------------------------------" -ForegroundColor Cyan
Write-Host "INTELIGENTNE SKANOWANIE: $katalogRoboczy"
Write-Host "Limit części: $([math]::Round($maksymalnyRozmiar / 1MB, 2)) MB"
Write-Host "Ignorowanie plików > $([math]::Round($limitPojedynczegoPliku / 1KB, 0)) KB"
Write-Host "--------------------------------------------------------" -ForegroundColor Cyan

# Pobieranie i filtrowanie plików
$plikiDoZebrania = Get-ChildItem -Path $katalogRoboczy -Recurse -File | Where-Object {
    $plik = $_
    
    # 1. Sprawdź rozszerzenie
    $pasujeRozszerzenie = $false
    foreach ($rozszerzenie in $uwzglednianeRozszerzenia) {
        if ($plik.Name.EndsWith($rozszerzenie)) {
            $pasujeRozszerzenie = $true
            break
        }
    }
    if (-not $pasujeRozszerzenie) { return $false }

    # 2. Sprawdź wykluczone katalogi
    if ($plik.FullName -match $wzorzecWykluczeniaKat) { return $false }

    # 3. Sprawdź konkretne nazwy plików do ignorowania
    if ($ignorowanePlikiNazwy -contains $plik.Name) { return $false }

    # 4. Sprawdź czy to nie jest zminifikowany plik (np. jquery.min.js)
    if ($plik.Name -match "\.min\.(js|css)$") { return $false }

    # 5. Sprawdź rozmiar pojedynczego pliku (czy to nie jest np. wielki SQL dump)
    if ($plik.Length -gt $limitPojedynczegoPliku) { 
        Write-Host "POMINIĘTO (za duży): $($plik.Name) ($([math]::Round($plik.Length / 1MB, 2)) MB)" -ForegroundColor DarkGray
        return $false 
    }

    return $true
}

if ($null -eq $plikiDoZebrania -or $plikiDoZebrania.Count -eq 0) {
    Write-Host "Brak plików pasujących do kryteriów." -ForegroundColor Red
    return
}

# =============================================================================
# 3. LOGIKA ZAPISU I PODZIAŁU
# =============================================================================

$numerCzesci = 1

function Get-CurrentOutputFile {
    return Join-Path -Path $katalogRoboczy -ChildPath "${nazwaBazowa}_part${numerCzesci}.txt"
}

$aktualnyPlikWyjsciowy = Get-CurrentOutputFile
if (Test-Path $aktualnyPlikWyjsciowy) { Remove-Item $aktualnyPlikWyjsciowy }
New-Item -Path $aktualnyPlikWyjsciowy -ItemType File -Force | Out-Null

Write-Host "Znaleziono $($plikiDoZebrania.Count) istotnych plików. Przetwarzanie..." -ForegroundColor Green

$licznikPlikow = 0

foreach ($plik in $plikiDoZebrania) {
    $licznikPlikow++
    $sciezkaWzgledna = $plik.FullName.Substring($katalogRoboczy.Length + 1)
    
    $naglowek = "`r`n================================================================================`r`n### PLIK: $sciezkaWzgledna`r`n================================================================================`r`n"
    
    $infoPlikWyjsciowy = Get-Item $aktualnyPlikWyjsciowy
    $rozmiarNaDysku = $infoPlikWyjsciowy.Length
    $rozmiarNowegoWsadu = $plik.Length

    # -- LOGIKA PODZIAŁU --
    if (($rozmiarNaDysku + $rozmiarNowegoWsadu + 1024) -gt $maksymalnyRozmiar) {
        Write-Host ">>> Osiągnięto limit w części $numerCzesci ($([math]::Round($rozmiarNaDysku / 1MB, 2)) MB). Tworzę nową część." -ForegroundColor Magenta
        
        $numerCzesci++
        $aktualnyPlikWyjsciowy = Get-CurrentOutputFile
        
        if (Test-Path $aktualnyPlikWyjsciowy) { Remove-Item $aktualnyPlikWyjsciowy }
        New-Item -Path $aktualnyPlikWyjsciowy -ItemType File -Force | Out-Null
        $rozmiarNaDysku = 0
    }

    Add-Content -Path $aktualnyPlikWyjsciowy -Value $naglowek -Encoding UTF8
    Get-Content -Path $plik.FullName -Raw | Add-Content -Path $aktualnyPlikWyjsciowy -Encoding UTF8
    
    # Log co 50 plików
    if ($licznikPlikow % 50 -eq 0) {
        $aktualnyRozmiarMB = [math]::Round(($rozmiarNaDysku + $rozmiarNowegoWsadu) / 1MB, 2)
        Write-Host " [$licznikPlikow/$($plikiDoZebrania.Count)] ... Część $numerCzesci ($aktualnyRozmiarMB MB)" -ForegroundColor Gray
    }
}

Write-Host "--------------------------------------------------------" -ForegroundColor Cyan
Write-Host "ZAKOŃCZONO!"
Write-Host "Utworzono łącznie $numerCzesci części."
Write-Host "--------------------------------------------------------" -ForegroundColor Cyan