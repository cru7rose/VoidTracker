Oczekuję zmiany trybu pracy. Zamiast standardowych rozwiązań (MVP), przechodzimy w tryb **"Hyper-Innovation"** (zgodnie z `MANIFEST.md`).

Twoim celem jest zbudowanie systemu, który w 2026 roku będzie wyznaczał standardy branżowe.
Zasady nowego trybu:
1.  **Challenge Everything:** Zanim napiszesz linijkę kodu, zadaj sobie pytanie: "Czy istnieje nowsza, szybsza lub bardziej efektowna biblioteka/metoda, żeby to zrobić?". Jeśli tak – zaproponuj ją.
2.  **UX Obsession (Void-Flow):** Interfejs ma być płynny (60fps), reaktywny i piękny. Żadnych przeładowań strony. Używaj animacji, aby tłumaczyć zmiany stanu.
3.  **Performance First:** Backend (Spring Boot 21) ma używać Virtual Threads (Project Loom) i natywnych zapytań tam, gdzie to możliwe. Obsługujemy 10k+ paczek dziennie – wydajność jest kluczowa.

**ZADANIE:**
Przeanalizuj obecny stack (szczególnie frontend Vue 3 i PWA) i zaproponuj 3 "Killer Features" lub wymiany bibliotek na nowocześniejsze (Open Source), które dadzą nam efekt "Wow" u klienta i drastycznie poprawią wydajność. Uzasadnij wybór.