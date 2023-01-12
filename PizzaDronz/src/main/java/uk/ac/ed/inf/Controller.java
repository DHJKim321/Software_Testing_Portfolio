package uk.ac.ed.inf;

import java.io.IOException;
import java.util.*;

import static java.util.List.of;


/**
 * This class deals with managing the control flow of the whole application
 * It is divided into four parts:
 * Part 1: Initialising and getting relevant pieces of data from the REST server.
 * Part 2: Validate the orders and attach relevant OrderOutcome enum values.
 * Part 3: Run the drone pathfinding algorithm.
 * Part 4: Write the calculated drone paths to a GeoJSON file.
 */

public class Controller {
    // Number of delivered orders for a given day.
    private int deliveredOrders;

    /**
     * This method checks that there is at least one order per day.
     * If empty, it prints out an error message. However, this does not termiante the program early.
     *
     * @param allOrders List of orders for that date.
     * @param date      Date in YYYY-MM-dd format.
     */
    private void checkOrderNotEmpty(List<Order> allOrders, String date) {
        if (allOrders.isEmpty()) {
            System.err.println("There are no orders for this day: " + date);
        }
    }


    /**
     * This method checks that there is at least one restaurant if there are orders in the system.
     *
     * @param restaurants List of all restaurants (Could be empty).
     * @param allOrders   List of all orders (Could be empty)
     */
    private void checkRestaurantsNonNull(List<Restaurant> restaurants, List<Order> allOrders) {
        if (restaurants.isEmpty() && !allOrders.isEmpty()) {
            System.err.println("There are no restaurants but there are orders.");
            System.exit(1);
        }
    }

    /**
     * This method validates that there are three arguments when the program is run.
     *      arg[0]: Date in YYYY-MM-dd format.
     *      args[1]: Base URL of the REST server.
     *      args[2]: Seed to be used for randomness (Not used).
     * @param args Array of program arguments.
     */
    private void checkProgramArguments(String[] args) {
        if (args.length != 3) {
            System.err.println("There must be three arguments passed into the program.");
            System.exit(1);
        }
    }

    /**
     * This method checks that there is a central area registered in the system.
     *
     * @param centralArea Central area (Could be null).
     */
    private void checkCentralAreaNonNull(Polygon centralArea) {
        if (centralArea == null) {
            System.err.println("There is no central area.");
            System.exit(1);
        }
    }


    /**
     * This method controls the flow of the program.
     * It is split into four main parts:
     * Part 1 - Initialising data from the REST server.
     * Part 2 - Validating the orders.
     * Part 3 - Building the visibility graph and running the pathfinding algorithm.
     * Part 4 - Writing the output to JSON/GeoJSON files.
     *
     * @param args Taken in from the main method. args[0] = Date in YYYY-MM-dd format, args[1] = REST server URL.
     */
    public void startApp(String[] args) {
        var start = System.currentTimeMillis();
        checkProgramArguments(args);

        // Part 1 - Initialise all the relevant data from REST server

        var c = Client.createClient(args[0], args[1]);
        var date = args[0];

        // Hard-coded Appleton Tower coordinates
        var appletonCoord = LngLat.createAppletonLngLat();

        // Data pulled from REST servers
        var centralArea = Polygon.fromLngLat(of(c.getResponse("centralArea", LngLat[].class)));
        var allOrders = of(c.getResponse("orders", Order[].class));
        var allRestaurants = of(c.getResponse("restaurants", Restaurant[].class));
        var noFlyZones = of(c.getResponse("noFlyZones", Polygon[].class));

        // Temporary data structures
        Map<Restaurant, List<Move>> restaurantToMoves = new HashMap<>();
        List<Move> allPaths = new ArrayList<>();
        Map<Restaurant, List<LngLat>> restaurantToNodes = new HashMap<>();


        // Part 1.1 - Validating REST server data

        checkCentralAreaNonNull(centralArea);
        checkRestaurantsNonNull(allRestaurants, allOrders);
        checkOrderNotEmpty(allOrders, date);


        // Part 2 - Validate orders

        List<ValidatedOrder> validatedOrders = OrderChecker.validateAllOrders(allRestaurants, allOrders);


        // Part 3.1 - Building graph and drone

        var drone = Drone.createDrone(appletonCoord);
        var graph = Graph.createGraph(appletonCoord, noFlyZones,
                allRestaurants.stream().map(Restaurant::coord).toList());


        // Part 3.2 - Simulating moves required and reordering the orders based on number of moves.

        for (var validatedOrder : validatedOrders) {
            var restaurant = validatedOrder.getRestaurant();
            if (OrderChecker.checkValid(validatedOrder)) {
                // List of nodes approximating the shortest path that the drone should visit.
                var nodePath = restaurantToNodes.getOrDefault(restaurant,
                        graph.getPath(drone.getCurrCoord(), restaurant.coord()));
                if (nodePath != null) {
                    validatedOrder.setDistance(drone.getCurrCoord());
                    restaurantToNodes.putIfAbsent(restaurant, nodePath);
                } else {
                    // If there is no valid set of nodes from Appleton Tower to the restaurant, make it unable to travel.
                    validatedOrder.setDistance(null);
                }
            } else {
                validatedOrder.setDistance(null); // If order is invalid, make it unable to travel.
            }
        }

        // Ordering heuristics - see OrderShuffler class for more.
        OrderShuffler.shuffleOrdersByDistance(validatedOrders);


        // Part 3.3 - Moving the drone and delivering the orders

        var pathFinder = new PathFinder();
        for (var validatedOrder : validatedOrders) {
            if (validatedOrder.getDistance() != Double.POSITIVE_INFINITY) { // For any valid orders:
                var orderNo = validatedOrder.getOrder().orderNo();
                List<Move> moves;

                if (restaurantToMoves.get(validatedOrder.getRestaurant()) != null) { // If there is a cached flight-path
                    var presetRoute = restaurantToMoves.get(validatedOrder.getRestaurant());
                    moves = pathFinder.travelPresetRoute(orderNo, presetRoute);
                } else { // Otherwise, calculate the path
                    var nodePath = restaurantToNodes.get(validatedOrder.getRestaurant());
                    moves = pathFinder.travel(drone.getCurrCoord(), nodePath, orderNo, noFlyZones);
                    restaurantToMoves.putIfAbsent(validatedOrder.getRestaurant(), moves); // Only need to put once
                }
                if (drone.hasEnoughBattery(moves.size())) {
                    drone.followPath(orderNo, moves);

                    allPaths.addAll(drone.getPath()); // Add to list of all flight paths that the drone took that day.
                    OrderChecker.setAsDelivered(validatedOrder);
                    deliveredOrders++;
                    drone.reset(appletonCoord); // Reset drone's location to Appleton Tower.
                } else {
                    // Break early as drone will not have enough battery to deliver any subsequent orders.
                    // Change this to 'continue' if using other heuristics not based on distance/number of moves.
                    break;
                }
            }
        }


        // Part 4 - Start file writing program

        
        JsonWriter jsonWriter = new JsonWriter(date);
        try {
            jsonWriter.writeFlightpath(allPaths);
            jsonWriter.writeGeoJson(allPaths);
            jsonWriter.writeDeliveries(validatedOrders);
        } catch (IOException e) {
            System.err.println("The named file exists but is a directory rather than a regular file, does not exist " +
                    "but cannot be created, or cannot be opened for any other reason.");
            System.exit(1);
        }


        var end = System.currentTimeMillis();
        System.out.println("The app took " + (end - start) + " ms to complete.");
        System.out.println("There were " + deliveredOrders + " orders delivered on " + date + " out of " +
                validatedOrders.size() + " total orders.");
    }

    public int getDeliveredOrders() {
        return deliveredOrders;
    }
}
