package com.pokevault.domain.enums;

/**
 * Enumeration representing the trade availability and lifecycle state
 * of a PokéVault store game account (GameAccount).
 */
public enum AccountTradeStatus {
    /**
     * Account is available and ready to be matched for fulfilling trade orders.
     */
    READY,

    /**
     * Account is currently in an active trade session with a customer.
     */
    BUSY_TRADING,

    /**
     * Account is on cooldown waiting for Pokémon Pocket trade restrictions/timers to reset.
     */
    COOLDOWN,

    /**
     * Account is temporarily suspended by store admin for maintenance or verification.
     */
    SUSPENDED
}
