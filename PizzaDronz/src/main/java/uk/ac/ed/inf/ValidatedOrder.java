package uk.ac.ed.inf;

import org.json.simple.JSONObject;

/**
 * This class contains an order, its order outcome, the number of moves it takes for a delivery, and the corresponding
 * restaurant.
 */
@SuppressWarnings("unchecked")
public class ValidatedOrder {
    private final Order order;
    private OrderOutcome orderOutcome;
    private Double distance;
    private Restaurant restaurant;

    /**
     * This is a static factory method to create new ValidatedOrder objects.
     *
     * @param order The order to attach as an attribute.
     * @return A new ValidatedOrder object.
     */
    public static ValidatedOrder createValidatedOrder(Order order) {
        return new ValidatedOrder(order);
    }

    protected ValidatedOrder(Order order) {
        this.order = order;
    }

    public void setOrderOutcome(OrderOutcome orderOutcome) {
        this.orderOutcome = orderOutcome;
    }

    public OrderOutcome getOrderOutcome() {
        return orderOutcome;
    }

    public void setDistance(LngLat droneCoord) {
        distance = droneCoord == null ? Double.POSITIVE_INFINITY : restaurant.coord().distanceTo(droneCoord);
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public Double getDistance() {
        return distance;
    }

    public Order getOrder() {
        return order;
    }

    public int getOrderLength() {
        return order.orderItems().length;
    }

    /**
     * This method turns the ValidatedOrder object into a JSON string containing the following:
     *      orderNo: 8-digit hexadecimal order number.
     *      outcome: An OrderOutcome enum value.
     *      costInPence: Price of the validated order plus 100 pence for delivery.
     * @return The JSON string of the validated order.
     */
    public JSONObject toJsonObject() {
        var delivery = new JSONObject();
        delivery.put("orderNo", order.orderNo());
        delivery.put("outcome", orderOutcome.toString());
        delivery.put("costInPence", order.priceTotalInPence());
        return delivery;
    }

    @Override
    public String toString() {
        return "ValidatedOrder{" +
                "order=" + order +
                ", orderOutcome=" + orderOutcome +
                ", distance=" + distance +
                ", restaurant=" + restaurant +
                '}';
    }
}
