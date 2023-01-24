package uk.ac.ed.inf;

import java.util.*;

/**
 * This class deals with initialising the visibility graph of the map and contains the Node and Edge inner class
 * which are connected to form the overall graph.
 */
public class Graph {


    /**
     * This is a record which contains information about the node at each end of the edge, as well as its weight, which
     * is calculated by taking the Euclidean distance from one node to the other.
     */
    public record Edge(Node startNode, Node endNode, Double weight) {

        @Override
        public String toString() {
            return "Edge{" +
                    "weight=" + weight +
                    ", start node=" + startNode +
                    ", end node=" + endNode +
                    '}';
        }
    }


    /**
     * This class contains information about the various heuristics used in the search algorithms, as well as its
     * coordinates in LngLat form and a prev attribute which references the previous node that the algorithm had to take
     * to get to this node.
     */
    public static class Node implements Comparable<Node> {

        private Node prev;
        private Double f;
        // Cost to get to this node from the starting node.
        private Double g;
        // Estimated cost to get to the end node from this node.
        private Double h;
        private final LngLat coord;

        public Node(LngLat lngLat) {
            prev = null;
            f = Double.POSITIVE_INFINITY;
            g = Double.POSITIVE_INFINITY;
            h = 0.0;
            coord = lngLat;
        }

        @Override
        public int compareTo(Node o) {
            return Double.compare(f, o.f);
        }

        /**
         * This method calculates the f value of a node given g and h by taking the weighted sum.
         * Reference: <a href="http://theory.stanford.edu/~amitp/GameProgramming/Variations.html">...</a>
         *
         * @param g Cost of getting from the start node to current node.
         * @param h Estimated cost of getting from the current node to the end node.
         */
        private void setF(Double g, Double h) {
            f = g + WEIGHT * h;
        }

        public Double calculateHeuristic(Node target) {
            return coord.distanceTo(target.coord);
        }

        public LngLat getCoord() {
            return coord;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "prev=" + prev +
                    ", f=" + f +
                    ", g=" + g +
                    ", h=" + h +
                    ", coord=" + coord +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Node node = (Node) o;

            return coord.equals(node.coord);
        }

        @Override
        public int hashCode() {
            return coord.hashCode();
        }
    }


    private static final Double WEIGHT = 1.0;

    private Map<Node, List<Edge>> nodeToEdges;

    /**
     * This method initialises all the vertices and edges of the graph.
     *
     * @param appletonCoord    Appleton Tower coordinates in LngLat format.
     * @param noFlyZones       List of no-fly zones.
     * @param restaurantCoords List of restaurants.
     */
    private Graph(LngLat appletonCoord, List<Polygon> noFlyZones, List<LngLat> restaurantCoords) {
        initNodes(appletonCoord, noFlyZones, restaurantCoords);
        initEdges();
        initVisibilityGraph(noFlyZones);
    }

    /**
     * This is a static factory method which creates a new Graph object.
     *
     * @param appletonCoord    Coordinates of Appleton Tower
     * @param noFlyZones       List of no-fly zones.
     * @param restaurantCoords List of restaurants' coordinates.
     * @return A new Graph object.
     */
    public static Graph createGraph(LngLat appletonCoord, List<Polygon> noFlyZones, List<LngLat> restaurantCoords) {
        return new Graph(appletonCoord, noFlyZones, restaurantCoords);
    }

    /**
     * This method goes through the list of restaurant coordinates, no-fly zones and the appleton coordinates
     * to initialise them as nodes.
     *
     * @param appletonCoord    Appleton Tower coordinates in LngLat format.
     * @param noFlyZones       List of no-fly zones.
     * @param restaurantCoords List of restaurants' coordinates.
     */
    private void initNodes(LngLat appletonCoord, List<Polygon> noFlyZones, List<LngLat> restaurantCoords) {
        nodeToEdges = new HashMap<>();
        if (appletonCoord == null) {
            System.err.println("The Appleton Tower coordinates must not be null.");
            System.exit(1);
        }
        nodeToEdges.putIfAbsent(new Node(appletonCoord), new ArrayList<>());
        if (restaurantCoords == null || restaurantCoords.size() == 0) {
            System.err.println("There must be at least one restaurant.");
            System.exit(1);
        }
        for (var r : restaurantCoords) {
            nodeToEdges.putIfAbsent(new Node(r), new ArrayList<>());
        }
        for (var nfz : noFlyZones) {
            for (var c : nfz.coordinates()) { // For each vertex of a no-fly zone.
                nodeToEdges.putIfAbsent(new Node(c), new ArrayList<>());
            }
        }
    }


    /**
     * This method initialises the edges of a node that is not a no-fly zone by connecting it to all the other nodes.
     * Therefore, if there are n nodes, each node will have O(n - 1) edges.
     */
    private void initEdges() {
        for (var u : nodeToEdges.keySet()) {
            for (var v : nodeToEdges.keySet()) {
                if (!u.equals(v)) {
                    var edge = new Edge(u, v, u.coord.distanceTo(v.coord));
                    nodeToEdges.get(u).add(edge);
                }
            }
        }
    }


    /**
     * This method goes through all the edges of each node, and checks whether it intersects any no-fly zone boundaries.
     * If it does, then the weight of that edge will be deleted so that the algorithm does not travel along it.
     * Reference: <a href="http://www.science.smith.edu/~istreinu/Teaching/Courses/274/Spring98/Projects/Philip/fp/visibility.htm">...</a>
     * To check for the edge case outlined in the report, I check whether any one of the three points along the edge
     * is inside a no-fly zone.
     * q1 refers to the point resting at 1/4 of the distance of the full line.
     * q2 refers to the point resting at 1/2 of the distance of the full line. (Thus the middle of the line).
     * q3 refers to the point resting at 3/4 of the distance of the full line.
     *
     * @param noFlyZones List of no-fly zones to avoid.
     */
    private void initVisibilityGraph(List<Polygon> noFlyZones) {
        if (noFlyZones.isEmpty()) { // End early if there are no no-fly zones to save computational resources.
            return;
        }
        for (var node : nodeToEdges.keySet()) {
            List<Edge> visibleEdges = new ArrayList<>();
            for (var edge : nodeToEdges.get(node)) {
                boolean isVisible = true;
                var start = edge.startNode().coord;
                var end = edge.endNode().coord;
                var q1 = LngLat.createLngLatInBetween(start, end, 0.25); // 1/4 of the way
                var q2 = LngLat.createLngLatInBetween(start, end, 0.5); // 1/2 of the way
                var q3 = LngLat.createLngLatInBetween(start, end, 0.75); // 3/4 of the way
                for (var nfz : noFlyZones) {
                    if (nfz.isLineIntersectingNfz(start, end) ||
                            nfz.isInsidePolygon(q1, true) ||
                            nfz.isInsidePolygon(q2, true) ||
                            nfz.isInsidePolygon(q3, true)) {
                        isVisible = false;
                        break;
                    }
                }
                if (isVisible) {
                    visibleEdges.add(edge);
                }
            }
            nodeToEdges.replace(node, visibleEdges);
        }
    }

    /**
     * This method initialises the heuristic attribute 'h' for all nodes.
     * It currently uses the Euclidean straight-line distance as it is an admissible heuristic.
     *
     * @param destination Coordinates of the destination.
     */
    private void initHeuristic(LngLat destination) {
        nodeToEdges.keySet().forEach(n -> n.h = n.coord.distanceTo(destination));
    }

    /**
     * This method is an A-Star search algorithm that approximates the shortest path between start and finish.
     * This method turns into Dijkstra's algorithm when the heuristic h is set to 0 for all nodes.
     *
     * @param start Starting node.
     * @param end   Destination node.
     * @return The final node which has pointers to previous nodes that the drone needs to visit first.
     * Reference: <a href="https://en.wikipedia.org/wiki/A*_search_algorithm">...</a>
     */
    private Node AStar(Node start, Node end) {
        resetCost();
        initHeuristic(end.coord);
        PriorityQueue<Node> visited = new PriorityQueue<>();
        start.g = 0.0;
        start.setF(start.g, start.h);
        visited.add(start);

        while (!visited.isEmpty()) {
            var n = visited.peek();
            if (n.equals(end)) {
                return n;
            }
            for (var e : getEdges(n.coord)) {
                var m = e.endNode();
                Double currWeight = n.g + e.weight;
                if (currWeight < m.g) {
                    m.prev = n;
                    m.g = currWeight;
                    m.f = m.g + m.h;
                    if (!visited.contains(m)) {
                        visited.add(m);
                    }
                }
            }
            visited.remove(n);
        }
        return null;
    }

    /**
     * This method performs a search algorithm and returns iteratively the order of the nodes which the drone needs to
     * visit.
     *
     * @param start       Starting coordinates.
     * @param destination Final coordinates.
     * @return List of coordinates in LngLat format, which lays out a path from beginning -> end.
     * If there is no valid path, it returns a Node object with a null LngLat attribute.
     * If the destination is close to the start, this returns an empty list.
     */
    public List<LngLat> getPath(LngLat start, LngLat destination) {
        var end = AStar(toNode(start), toNode(destination));
        if (end == null) {
            System.err.println("There is no path from the start to the destination coordinates.");
            return null;
        }
        List<LngLat> path = new ArrayList<>();
        if (start.equals(destination)) {
            return path;
        }
        while (end != null) {
            path.add(0, end.coord);
            end = end.prev;
        }
        return path;
    }


    /**
     * After the algorithm finishes the search algorithm , this method goes through the nodes and reset the cost values,
     * distance, and prev values for the next graph calculation.
     */
    private void resetCost() {
        for (var node : nodeToEdges.keySet()) {
            node.prev = null;
            node.g = Double.POSITIVE_INFINITY;
            node.f = Double.POSITIVE_INFINITY;
            node.h = 0.0;
        }
    }

    /**
     * This method returns the edges of a given LngLat.
     *
     * @param lngLat coordinates to which the edges are mapped to.
     * @return List of edges with the LngLat coordinate as the starting point.
     */
    private List<Edge> getEdges(LngLat lngLat) {
        if (toNode(lngLat) != null) {
            return nodeToEdges.get(toNode(lngLat));
        }
        System.err.println("No node with coordinates: " + lngLat);
        return new ArrayList<>();
    }

    public Map<Node, List<Edge>> getNodeToEdges() {
        return nodeToEdges;
    }

    /**
     * This method turns a LngLat coordinate into its corresponding node.
     *
     * @param lngLat LngLat coordinate to be converted.
     * @return LngLat coordinate in Node format.
     */
    private Node toNode(LngLat lngLat) {
        for (var node : nodeToEdges.keySet()) {
            if (node.coord.equals(lngLat)) {
                return node;
            }
        }
        System.err.println("There are no nodes associated with the coordinates: " + lngLat.toString() + ".");
        return null;
    }

    @Override
    public String toString() {
        return "Graph{" +
                "map=" + nodeToEdges +
                '}';
    }
}
