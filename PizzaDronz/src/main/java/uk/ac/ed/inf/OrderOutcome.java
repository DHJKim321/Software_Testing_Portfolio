package uk.ac.ed.inf;

/**
 * This enum value details all the different outcomes of an order (valid or invalid).
 */
public enum OrderOutcome {
    DELIVERED,
    VALID_BUT_NOT_DELIVERED,
    INVALID_CARD_NUMBER,
    INVALID_EXPIRY_DATE,
    INVALID_CVV,
    INVALID_TOTAL,
    INVALID_PIZZA_NOT_DEFINED,
    INVALID_PIZZA_COUNT,
    INVALID_PIZZA_COMBINATION_MULTIPLE_SUPPLIERS,
    INVALID_ORDER_NUMBER,
    INVALID
}
