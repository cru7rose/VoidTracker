package com.example.danxils_commons.enums;

import java.util.Set;

/**
 * ARCHITEKTURA: Scentralizowana definicja statusów cyklu życia zlecenia.
 * Umieszczona w module 'commons', aby zapewnić spójność w całym ekosystemie.
 * 
 * ENHANCEMENT: Dodano logikę maszyny stanów do walidacji przejść między
 * statusami.
 * Zapobiega to nieprawidłowym zmianom statusu i gwarantuje integralność danych.
 */
public enum OrderStatus {
    /**
     * Oczekuje na weryfikację (np. walidacja adresu)
     */
    PENDING,

    /**
     * Gotowe do przypisania kierowcy
     */
    NEW,

    /**
     * Przypisane do odbioru
     */
    PICKUP,

    /**
     * Odbiór w trakcie (transfer/return)
     */
    PSIP,

    /**
     * Załadowane na pojazd (zeskanowane przez kierowcę)
     */
    LOAD,

    /**
     * Zeskanowane w terminalu/hubie
     */
    TERM,

    /**
     * Dostarczone z ePoD
     */
    POD;

    /**
     * Sprawdza czy dany status jest statusem końcowym (terminal).
     * Zlecenia w statusie końcowym nie powinny już zmieniać statusu.
     *
     * @return true jeśli status jest końcowy
     */
    public boolean isTerminal() {
        return this == POD;
    }

    /**
     * Sprawdza czy przejście z bieżącego statusu do podanego statusu jest
     * dozwolone.
     * Implementuje reguły biznesowe dla workflow zleceń.
     *
     * @param target docelowy status
     * @return true jeśli przejście jest dozwolone
     */
    public boolean canTransitionTo(OrderStatus target) {
        if (target == null) {
            return false;
        }

        // Statusy końcowe nie mogą przejść do innego statusu
        if (this.isTerminal()) {
            return false;
        }

        // Definiuj dozwolone przejścia zgodnie z workflow
        return switch (this) {
            case PENDING -> target == NEW;
            case NEW -> target == PICKUP;
            case PICKUP -> target == PSIP || target == LOAD || target == POD;
            case PSIP -> target == LOAD || target == POD;
            case LOAD -> target == TERM || target == POD;
            case TERM -> target == POD;
            default -> false;
        };
    }

    /**
     * Zwraca zbiór wszystkich dozwolonych następnych statusów dla bieżącego
     * statusu.
     * Przydatne do wyświetlania możliwych akcji w UI.
     *
     * @return zbiór dozwolonych następnych statusów
     */
    public Set<OrderStatus> getAllowedNextStatuses() {
        if (this.isTerminal()) {
            return Set.of();
        }

        return switch (this) {
            case PENDING -> Set.of(NEW);
            case NEW -> Set.of(PICKUP);
            case PICKUP -> Set.of(PSIP, LOAD, POD);
            case PSIP -> Set.of(LOAD, POD);
            case LOAD -> Set.of(TERM, POD);
            case TERM -> Set.of(POD);
            default -> Set.of();
        };
    }

    /**
     * Sprawdza czy status jest statusem aktywnym (zlecenie w trasie).
     * Przydatne do filtrowania aktywnych zleceń.
     *
     * @return true jeśli zlecenie jest aktywne
     */
    public boolean isActive() {
        return this == PICKUP || this == PSIP || this == LOAD || this == TERM;
    }

    /**
     * Sprawdza czy status wymaga akcji kierowcy.
     *
     * @return true jeśli kierowca musi podjąć akcję
     */
    public boolean requiresDriverAction() {
        return this == PICKUP || this == LOAD || this == TERM;
    }
}
