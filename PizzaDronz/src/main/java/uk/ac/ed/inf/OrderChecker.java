package uk.ac.ed.inf;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.List.of;

/**
 * This static class deals with validating orders and attaches an OrderOutcome enum value based on the outcome.
 */
public class OrderChecker {

    /**
     * This method sets the order outcome to DELIVERED.
     *
     * @param validatedOrder Order to be set as delivered.
     */
    public static void setAsDelivered(ValidatedOrder validatedOrder) {
        validatedOrder.setOrderOutcome(OrderOutcome.DELIVERED);
    }

    /**
     * This is a helper method returning all pizza names in all the restaurants.
     *
     * @param restaurants List of all restaurants on the system.
     * @return A list of valid pizza names.
     */
    private static List<String> getAllPizzaNames(List<Restaurant> restaurants) {
        List<String> pizzaNames = new ArrayList<>();
        for (var restaurant : restaurants) {
            pizzaNames.addAll(Arrays.stream(restaurant.menu()).map(Menu::name).toList());
        }
        return pizzaNames;
    }

    /**
     * This method checks whether the order number is valid (hexadecimal).
     *
     * @param orderNo Order number.
     * @return Boolean value of whether the order number is valid.
     */
    private static boolean checkValidOrderNumber(String orderNo) {
        try {
            Long.parseLong(orderNo, 16);
        } catch (NumberFormatException e) {
            return false;
        }
        return (orderNo.length() == 8);
    }

    /**
     * This method checks that the pizzas in the order are pizzas that are present in a restaurant's menu.
     *
     * @param orderItems List of pizzas.
     * @param pizzaNames List of all pizza names.
     * @return Boolean value on whether the pizza exists.
     */
    private static boolean checkOnlyValidPizzas(List<String> orderItems, List<String> pizzaNames) {
        for (var name : orderItems) {
            if (!pizzaNames.contains(name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method checks that the number of pizzas in the order is between 1-4 inclusive.
     *
     * @param orderItems List of pizzas.
     * @return Returns true if the number of pizza items is between 1 and 4.
     */
    private static boolean checkPizzaCount(List<String> orderItems) {
        return orderItems.size() != 0 && orderItems.size() <= 4;
    }

    /**
     * This method checks that the pizza items in the order all come from one single restaurant.
     *
     * @param restaurants    List of all restaurants in the system.
     * @param validatedOrder Order that we are checking for.
     * @return Returns true if all pizzas in the order come from one single restaurant.
     */
    private static boolean checkValidCombination(List<Restaurant> restaurants, ValidatedOrder validatedOrder) {
        for (var r : restaurants) {
            Set<String> pizzaNames = new HashSet<>();
            Arrays.stream(r.menu()).forEach(m -> pizzaNames.add(m.name()));
            // If all pizzas in the order come from the same restaurant
            if (pizzaNames.containsAll(of(validatedOrder.getOrder().orderItems()))) {
                validatedOrder.setRestaurant(r);
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks that the order's priceInPence value is the same as its actual cost.
     *
     * @param validatedOrder Order that we are checking for.
     * @return Returns true if the order's actual cost matches with its priceInPence value.
     */
    private static boolean checkDeliveryCost(ValidatedOrder validatedOrder) {
        var cost = Order.getDeliveryFee();
        var nameToPrice = Arrays.stream(validatedOrder.getRestaurant().menu()).collect(
                Collectors.toMap(Menu::name, Menu::priceInPence)
        );
        for (var item : validatedOrder.getOrder().orderItems()) {
            cost += nameToPrice.get(item);
        }
        return (validatedOrder.getOrder().priceTotalInPence() == cost);
    }

    /**
     * This method checks that the order has been validated but not delivered.
     *
     * @param validatedOrder Order to check its order outcome.
     * @return Boolean value of whether the order is valid but not delivered or not.
     */
    public static boolean checkValid(ValidatedOrder validatedOrder) {
        return validatedOrder.getOrderOutcome().equals(OrderOutcome.VALID_BUT_NOT_DELIVERED);
    }


    /**
     * This method attaches an OrderOutcome enum value to the order depending on whether it is valid or not.
     * It would be a private method if not for testing purposes.
     *
     * @param restaurants A list of all the restaurants in the system.
     * @param order       Order that we are validating.
     */
    static ValidatedOrder validateOrder(List<Restaurant> restaurants, Order order) {
        var validatedOrder = ValidatedOrder.createValidatedOrder(order);
        var orderItems = of(validatedOrder.getOrder().orderItems());
        if (!validatedOrder.getOrder().card().checkCvv()) {
            System.err.println("Invalid CVV.");
            validatedOrder.setOrderOutcome(OrderOutcome.INVALID_CVV);
        } else if (!validatedOrder.getOrder().card().checkDate()) {
            System.err.println("Invalid expiry date.");
            validatedOrder.setOrderOutcome(OrderOutcome.INVALID_EXPIRY_DATE);
        } else if (!validatedOrder.getOrder().card().checkCardNumber()) {
            System.err.println("Invalid credit card number.");
            validatedOrder.setOrderOutcome(OrderOutcome.INVALID_CARD_NUMBER);
        } else if (!checkValidOrderNumber(validatedOrder.getOrder().orderNo())) {
            System.err.println("The order number is invalid.");
            validatedOrder.setOrderOutcome(OrderOutcome.INVALID_ORDER_NUMBER);
        } else if (!checkOnlyValidPizzas(of(validatedOrder.getOrder().orderItems()), getAllPizzaNames(restaurants))) {
            System.err.println("This pizza is not defined in any of the restaurants' menus.");
            validatedOrder.setOrderOutcome(OrderOutcome.INVALID_PIZZA_NOT_DEFINED);
        } else if (!checkPizzaCount(orderItems)) {
            System.err.println("Invalid number of pizzas in order (Must be between 1 and 4 inclusive).");
            validatedOrder.setOrderOutcome(OrderOutcome.INVALID_PIZZA_COUNT);
        }
        // This check is required, otherwise all orders will be classified as valid but not delivered.
        if (validatedOrder.getOrderOutcome() != null) {
            return validatedOrder;
        }
        if (!checkValidCombination(restaurants, validatedOrder)) {
            validatedOrder.setOrderOutcome(OrderOutcome.INVALID_PIZZA_COMBINATION_MULTIPLE_SUPPLIERS);
            System.err.println("Invalid pizza combination.");
        } else if (!checkDeliveryCost(validatedOrder)) {
            validatedOrder.setOrderOutcome(OrderOutcome.INVALID_TOTAL);
            System.err.println("The total cost of this order is wrong.");
        } else if (checkDeliveryCost(validatedOrder)) {
            validatedOrder.setOrderOutcome(OrderOutcome.VALID_BUT_NOT_DELIVERED);
        } else { // Any other edge cases that I could have missed is automatically classified as INVALID.
            validatedOrder.setOrderOutcome(OrderOutcome.INVALID);
            System.err.println("There was something else wrong with this order.");
        }
        return validatedOrder;
    }

    /**
     * This method calls the validateOrder method on a list of orders, and returns a new list of ValidatedOrders.
     *
     * @param restaurants List of all restaurants.
     * @param orders      List of all orders.
     * @return List of validated orders with an OrderOutcome enum value attached.
     */
    public static List<ValidatedOrder> validateAllOrders(List<Restaurant> restaurants, List<Order> orders) {
        List<ValidatedOrder> validatedOrders = new ArrayList<>();
        for (var order : orders) {
            var validatedOrder = OrderChecker.validateOrder(restaurants, order);
            validatedOrders.add(validatedOrder);
        }
        return validatedOrders;
    }
}
