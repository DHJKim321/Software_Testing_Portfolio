package uk.ac.ed.inf;

import java.util.Comparator;
import java.util.List;

/**
 * This class contains different static heuristic methods to reorder the list of valid orders.
 */
public class OrderShuffler {
    // Types of heuristics we could use:
    // Shortest distance
    // Maximum number of pizzas
    // 01 Knapsack problem (?)

    /**
     * This method shuffles the orders such that they are in ascending order of distance to the restaurant.
     *
     * @param validatedOrders Validated orders.
     */
    public static void shuffleOrdersByDistance(List<ValidatedOrder> validatedOrders) {
        validatedOrders.sort(Comparator.comparing(ValidatedOrder::getDistance));
    }

    /**
     * This method shuffles the orders such that they are in descending order of number of order items.
     *
     * @param validatedOrders Validated orders.
     */
    public static void shuffleOrdersByPizzaCount(List<ValidatedOrder> validatedOrders) {
        validatedOrders.sort(Comparator.comparing(ValidatedOrder::getOrderLength).reversed());
    }
}
