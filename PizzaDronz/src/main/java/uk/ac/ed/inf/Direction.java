package uk.ac.ed.inf;

import java.util.Arrays;

/**
 * This enum contains the 16 compass direction and their respective angle in degrees.
 * It also has methods to verify that a Double angel corresponds to a direction and to get the opposite direction of a
 * given input.
 */

public enum Direction {

    E(0.0),
    ENE(22.5),
    NE(45.0),
    NNE(67.5),
    N(90.0),
    NNW(112.5),
    NW(135.0),
    WNW(157.5),
    W(180.0),
    WSW(202.5),
    SW(225.0),
    SSW(247.5),
    S(270.0),
    SSE(292.5),
    SE(315.0),
    ESE(337.5);

    private final Double angle;

    Direction(Double angle) {
        this.angle = angle;
    }

    public Double getAngle() {
        return angle;
    }

    /**
     * This method returns the angle's respective direction if it exists.
     * If no such direction exists, it prints out an error and exits the program as the drone is no longer able to fly
     * in that direction.
     *
     * @param angle The double value of the angle.
     * @return The direction enum value.
     */
    public static Direction getDirection(Double angle) {
        var direction = Arrays.stream(values()).filter(d -> d.angle.equals(angle)).findAny();
        if (direction.isEmpty()) {
            System.err.println("The drone cannot travel in the opposite direction of: " + angle + " degrees.");
            System.exit(1);
        }
        return direction.get();
    }

    /**
     * This method returns the direction that is opposite to the given direction value.
     *
     * @param d The direction for which we want the opposite of.
     * @return The opposite direction.
     */
    public static Direction reverseDirection(Direction d) {
        return getDirection((d.angle + 180) % 360);
    }
}
