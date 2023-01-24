package uk.ac.ed.inf;

/**
 * This record contains the coordinates of the drone, the direction of the next move, its current order number, and
 * how long it has elapsed since the start of the initial path calculation (In nanoseconds).
 */
public record Move(LngLat coordinates, Direction direction, String orderNo, long tick) {
    @Override
    public String toString() {
        return "Move{" +
                "coordinates=" + coordinates +
                ", direction=" + direction +
                ", order=" + orderNo +
                ", tick=" + tick +
                '}';
    }
}
