package uk.ac.ed.inf;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Component testing for pizza order validation.
 * If we carried out the full combinatorial testing method, then we would have far too many things to test.
 * I.e. we would need to test each OrderOutcome value, and all combinations of them for 1-4 pizzas.
 * Therefore, we will only be testing each error value for one number of pizzas.
 */

public class OrderPizzaTest {

    public static final List<Restaurant> restaurants = new ArrayList<>(Arrays.asList(
            new Restaurant("",
                    new LngLat(0.0, 0.0), new Menu[]{
                    Menu.createMenu("Shroom", 1000),
                    Menu.createMenu("Meat", 1100)
            }),
            new Restaurant("",
                    new LngLat(0.0, 1.0), new Menu[]{
                    Menu.createMenu("Margherita", 700),
                    Menu.createMenu("Pumpkin", 1200)
            }),
            new Restaurant("",
                    new LngLat(1.0, 0.0), new Menu[]{
                    Menu.createMenu("Pesto", 800),
                    Menu.createMenu("Calzone", 1100)
            }),
            new Restaurant("",
                    new LngLat(1.0, 1.0), new Menu[]{
                    Menu.createMenu("Gold", 11000),
                    Menu.createMenu("Pepperoni", 3200)
            })
    ));

    public Order makeBlankOrder() {
        return new Order("55724045", Card.createCard("4649386874496963", "05/27",
                "011"), 1100, new String[]{});
    }

    public Order makeOrderWithOnePizza() {
        return new Order("55724045", Card.createCard("4649386874496963", "05/27",
                "011"), 900, new String[]{"Pesto"});
    }

    public Order makeOrderWithTwoPizzas() {
        return new Order("55724045", Card.createCard("4649386874496963", "05/27",
                "011"), 2000, new String[]{"Pesto", "Calzone"});
    }

    public Order makeOrderWithThreePizzas() {
        return new Order("55724045",  Card.createCard("4649386874496963", "05/27",
                "011"), 2200, new String[]{"Margherita", "Margherita", "Margherita"});
    }

    public Order makeOrderWithFourPizzas() {
        return new Order("55724045", Card.createCard("4649386874496963", "05/27",
                "011"), 4300, new String[]{"Shroom", "Meat", "Shroom", "Meat"});
    }

    public Order makeOrderWithFivePizzas() {
        return new Order("55724045", Card.createCard("4649386874496963", "05/27",
                "011"), 55100, new String[]{"Gold", "Gold", "Gold", "Gold", "Gold"});
    }

    public Order makeOrderWithDifferentRestaurantPizzas() {
        return new Order("55724045", Card.createCard("4649386874496963", "05/27",
                "011"), 1900, new String[]{"Shroom", "Pesto"});
    }

    public Order makeOrderWithInvalidCardNumber() {
        return new Order("55724045", Card.createCard("1", "05/27",
                "011"), 900, new String[]{"Pesto"});
    }

    public Order makeOrderWithInvalidCvv() {
        return new Order("55724045", Card.createCard("4649386874496963", "05/27",
                "10101"), 900, new String[]{"Pesto"});
    }

    public Order makeOrderWithInvalidExpiryDate() {
        return new Order("55724045", Card.createCard("4649386874496963", "09/10",
                "011"), 900, new String[]{"Pesto"});
    }

    public Order makeOrderWithInvalidOrderNumber() {
        return new Order("1", Card.createCard("4649386874496963", "05/27",
                "011"), 900, new String[]{"Pesto"});
    }

    public Order makeOrderWithInvalidOrderNumberNull() {
        return new Order("", Card.createCard("4649386874496963", "05/27",
                "011"), 900, new String[]{"Pesto"});
    }

    public Order makeOrderWithInvalidPizza() {
        return new Order("55724045", Card.createCard("4649386874496963", "05/27",
                "011"), 1000, new String[]{"John"});
    }

    public Order makeOrderWithInvalidTotal() {
        return new Order("55724045", Card.createCard("4649386874496963", "05/27",
                "011"), 1000, new String[]{"Pesto"});
    }

    @Test
    public void orderNoPizzas() {
        Order order = makeBlankOrder();
        var validatedOrder = OrderChecker.validateOrder(restaurants, order);
        assertEquals(OrderOutcome.INVALID_PIZZA_COUNT, validatedOrder.getOrderOutcome());
    }

    @Test
    public void orderOnePizza() {
        Order order = makeOrderWithOnePizza();
        var validatedOrder = OrderChecker.validateOrder(restaurants, order);
        assertTrue(OrderChecker.checkValid(validatedOrder));
    }

    @Test
    public void orderTwoPizzas() {
        Order order = makeOrderWithTwoPizzas();
        var validatedOrder = OrderChecker.validateOrder(restaurants, order);
        assertTrue(OrderChecker.checkValid(validatedOrder));
    }

    @Test
    public void orderThreePizzas() {
        Order order = makeOrderWithThreePizzas();
        var validatedOrder = OrderChecker.validateOrder(restaurants, order);
        assertTrue(OrderChecker.checkValid(validatedOrder));
    }

    @Test
    public void orderFourPizzas() {
        Order order = makeOrderWithFourPizzas();
        var validatedOrder = OrderChecker.validateOrder(restaurants, order);
        assertTrue(OrderChecker.checkValid(validatedOrder));
    }

    @Test
    public void orderFivePizzas() {
        Order order = makeOrderWithFivePizzas();
        var validatedOrder = OrderChecker.validateOrder(restaurants, order);
        assertEquals(OrderOutcome.INVALID_PIZZA_COUNT, validatedOrder.getOrderOutcome());
    }

    @Test
    public void orderPizzasFromDifferentRestaurants() {
        Order order = makeOrderWithDifferentRestaurantPizzas();
        var validatedOrder = OrderChecker.validateOrder(restaurants, order);
        assertEquals(OrderOutcome.INVALID_PIZZA_COMBINATION_MULTIPLE_SUPPLIERS, validatedOrder.getOrderOutcome());
    }

    @Test
    public void orderPizzaWithInvalidCardNumber() {
        Order order = makeOrderWithInvalidCardNumber();
        var validatedOrder = OrderChecker.validateOrder(restaurants, order);
        assertEquals(OrderOutcome.INVALID_CARD_NUMBER, validatedOrder.getOrderOutcome());
    }

    @Test
    public void orderPizzaWithInvalidCvv() {
        Order order = makeOrderWithInvalidCvv();
        var validatedOrder = OrderChecker.validateOrder(restaurants, order);
        assertEquals(OrderOutcome.INVALID_CVV, validatedOrder.getOrderOutcome());
    }

    @Test
    public void orderPizzaWithInvalidExpiryDate() {
        Order order = makeOrderWithInvalidExpiryDate();
        var validatedOrder = OrderChecker.validateOrder(restaurants, order);
        assertEquals(OrderOutcome.INVALID_EXPIRY_DATE, validatedOrder.getOrderOutcome());
    }

    @Test
    public void orderPizzaWithInvalidOrderNumber() {
        Order order = makeOrderWithInvalidOrderNumber();
        var validatedOrder = OrderChecker.validateOrder(restaurants, order);
        assertEquals(OrderOutcome.INVALID_ORDER_NUMBER, validatedOrder.getOrderOutcome());
    }

    @Test
    public void orderPizzaWithInvalidOrderNumberThrowException() {
        Order order = makeOrderWithInvalidOrderNumberNull();
        var validatedOrder = OrderChecker.validateOrder(restaurants, order);
        assertEquals(OrderOutcome.INVALID_ORDER_NUMBER, validatedOrder.getOrderOutcome());
    }

    @Test
    public void orderPizzaWithInvalidPizza() {
        Order order = makeOrderWithInvalidPizza();
        var validatedOrder = OrderChecker.validateOrder(restaurants, order);
        assertEquals(OrderOutcome.INVALID_PIZZA_NOT_DEFINED, validatedOrder.getOrderOutcome());
    }

    @Test
    public void orderPizzaWithInvalidTotal() {
        Order order = makeOrderWithInvalidTotal();
        var validatedOrder = OrderChecker.validateOrder(restaurants, order);
        assertEquals(OrderOutcome.INVALID_TOTAL, validatedOrder.getOrderOutcome());
    }
}
