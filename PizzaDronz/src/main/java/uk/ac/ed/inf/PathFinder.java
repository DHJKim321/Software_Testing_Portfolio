package uk.ac.ed.inf;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * This class deals with calculating a list of moves that will get the drone to visit each node provided by the A-star
 * search algorithm. It also sets the tick duration for every move calculated.
 */
public class PathFinder {
    private long startingTick;
    private LngLat currCoord;

    /**
     * This method initialises the starting tick to a value on the first call (In nanoseconds).
     */
    private void setStartingTick() {
        if (startingTick == 0) {
            startingTick = Clock.tick(Clock.systemDefaultZone(), Duration.ofNanos(1)).instant().getNano();
        }
    }

    /**
     * This method returns the duration elapsed from the starting tick value to the current tick value.
     *
     * @return Elapsed time in nanoseconds since object initialisation.
     */
    private long getTiming() {
        return Clock.tick(Clock.systemDefaultZone(), Duration.ofNanos(1)).instant().getNano() - startingTick;
    }

    /**
     * This method finds the next move that will get this object's currCoord closest to the destination by avoiding
     * no-fly zones and travelling in one of 16 compass directions.
     *
     * @param destination LngLat coordinates of the destination.
     * @param orderNo     Order number of the current delivery
     * @param noFlyZones  List of no-fly zones to avoid.
     * @return A new Move object that contains the current coordinates, the next move, order number and elapsed duration
     * in milliseconds.
     */
    private Move travelNextMove(LngLat destination, String orderNo, List<Polygon> noFlyZones) {
        setStartingTick();
        Double minDist = Double.POSITIVE_INFINITY;
        Direction minDir = null;
        for (var d : Direction.values()) {
            var next = currCoord.nextPosition(d);
            if (noFlyZones != null && !noFlyZones.isEmpty()) {
                if (noFlyZones.stream().anyMatch(nfz -> nfz.isLineIntersectingNfz(currCoord, next))) {
                    continue;
                }
            }
            // We choose the direction that takes the drone closest to the destination.
            Double distance = Math.min(minDist, next.distanceTo(destination));
            if (distance < minDist) {
                minDir = d;
                minDist = distance;
            }
        }
        var coord = new LngLat(currCoord.lng(), currCoord.lat());
        currCoord = currCoord.nextPosition(minDir);
        return new Move(coord, minDir, orderNo, getTiming());
    }

    /**
     * This method repeatedly calls travelNextMove to travel from this object's currCoord to the destination by avoiding
     * no-fly zones.
     *
     * @param destination LngLat coordinates of the destination.
     * @param orderNo     Order number of the current delivery.
     * @param noFlyZones  List of no-fly zones to avoid.
     * @return A list of Move objects that represents the moves needed to get from currCoord to destination.
     */
    private List<Move> travelOneWayPath(LngLat destination, String orderNo,
                                        List<Polygon> noFlyZones) {
        List<Move> moves = new ArrayList<>();
        while (!currCoord.closeTo(destination)) {
            moves.add(travelNextMove(destination, orderNo, noFlyZones));
        }
        return moves;
    }

    /**
     * This method sets the return path of the drone going from the end of the path to the beginning.
     * Note that on the return path, the drone's respective direction will be matched to the drone's previous coordinates.
     * E.g. [(0,0), E] -> [(1,0), null] -> [(1,0), W] -> [(0,0), null]
     * Here, the return direction for [(1,0), W] is the opposite to the previous coordinate's direction [(0,0), E].
     * This method does not return a NullPointerException error, since the list of moves when this method is called will
     * contain at least one hover move. Thus, the for-loop will terminate immediately and move onto the next method.
     *
     * @param moves      List of moves to reverse.
     * @param startCoord The coordinate to return to.
     * @param orderNo    Order number of the current delivery.
     * @return A list of Move objects that represents the moves needed to get from destination to the
     * starting coordinates.
     */
    private List<Move> travelReversePath(List<Move> moves, LngLat startCoord, String orderNo) {
        List<Move> newMoves = new ArrayList<>();
        for (int i = moves.size() - 1; i > 0; i--) {
            var prevCoord = moves.get(i).coordinates();
            var previousDir = moves.get(i - 1).direction();
            var oppositeDir = Direction.reverseDirection(previousDir);
            newMoves.add(new Move(prevCoord, oppositeDir, orderNo, getTiming()));
        }
        currCoord = startCoord;
        return newMoves;
    }

    /**
     * This method returns a hover move which has a null direction.
     *
     * @param orderNo Order number of the current delivery.
     * @return A Move object that contains the current coordinates, a null direction enum, order number and duration
     * elapsed since beginning of route calculation.
     */
    private Move hover(String orderNo) {
        return new Move(currCoord, null, orderNo, getTiming());
    }

    /**
     * This method calculates the moves needed to travel from the startCoord to the destination and then back by
     * avoiding no-fly zones.
     *
     * @param startCoord Starting coordinates.
     * @param nodePath   A list of LngLat coordinates that need to be visited for the shortest path.
     * @param orderNo    Order number of the current delivery.
     * @param noFlyZones List of no-fly zones to avoid.
     * @return A list of Move objects that represents the moves needed to travel from startCoord to the destination
     * and then back.
     */
    public List<Move> travel(LngLat startCoord, List<LngLat> nodePath, String orderNo, List<Polygon> noFlyZones) {
        List<Move> moves = new ArrayList<>();
        currCoord = startCoord;
        for (var coord : nodePath) {
            moves.addAll(travelOneWayPath(coord, orderNo, noFlyZones));
        }
        moves.add(hover(orderNo));
        moves.addAll(travelReversePath(moves, startCoord, orderNo));
        moves.add(hover(orderNo));
        return moves;
    }

    /**
     * This method returns a new list of Move objects updated with a new order number and time elapsed if there
     * already exists a path for a given LngLat coordinate.
     *
     * @param orderNo Order number of the current delivery.
     * @param moves   List of pre-calculated Move objects.
     * @return A list of Move objects that are the same as the input but with updated order number and time elapsed.
     */
    public List<Move> travelPresetRoute(String orderNo, List<Move> moves) {
        var newMoves = new ArrayList<Move>();
        for (var move : moves) {
            newMoves.add(new Move(move.coordinates(), move.direction(), orderNo, getTiming()));
        }
        return newMoves;
    }
}
