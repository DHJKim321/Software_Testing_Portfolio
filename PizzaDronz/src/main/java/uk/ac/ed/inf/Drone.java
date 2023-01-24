package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the drone's attributes such as battery level, as well as a method to follow a given list of moves.
 */
public class Drone {
    private static final int MOVE_COST = 1;
    private static final int MAX_BATTERY = 2000;

    private int battery;
    private LngLat currCoord;
    private String orderNo;
    private List<Move> path;

    protected Drone(LngLat currCoord) {
        battery = MAX_BATTERY;
        this.currCoord = currCoord;
        orderNo = "no-order";
        path = new ArrayList<>();

    }

    /**
     * Static factory method to create new Drone objects.
     *
     * @return A new Drone object.
     */
    public static Drone createDrone(LngLat currCoord) {
        return new Drone(currCoord);
    }

    /**
     * This method resets the drone's list of path as well as resetting its position to the Appleton Tower.
     *
     * @param appletonCoord LngLat coordinates of Appleton Tower.
     */
    public void reset(LngLat appletonCoord) {
        currCoord = appletonCoord;
        path = new ArrayList<>();
        orderNo = "no-order";
    }

    /**
     * This method has the drone follow a computed flight path, appending the moves to the drone's path and updating
     * its battery and current coordinates..
     *
     * @param orderNo Order number of the current delivery.
     * @param moves   List of moves to deliver the order.
     */
    public void followPath(String orderNo, List<Move> moves) {
        this.orderNo = orderNo;
        for (var move : moves) {
            path.add(move);
            currCoord = move.coordinates().nextPosition(move.direction());
            this.battery -= MOVE_COST;
        }
    }

    /**
     * This method checks if the drone has enough battery to carry out a flight path.
     *
     * @param moves Number of moves within a flight path.
     * @return A boolean value on whether the drone will be able to carry out the flight path.
     */
    public boolean hasEnoughBattery(int moves) {
        return battery >= moves;
    }

    public LngLat getCurrCoord() {
        return currCoord;
    }

    public List<Move> getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "Drone{" +
                "battery=" + battery +
                ", currCoord=" + currCoord +
                ", orderNo='" + orderNo + '\'' +
                ", path=" + path +
                '}';
    }
}
