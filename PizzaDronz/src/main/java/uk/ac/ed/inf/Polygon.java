package uk.ac.ed.inf;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This record contains a list of LngLat objects as coordinates of its vertices, and has methods to determine if a point
 * is inside a Polygon, which depends on whether it is a central area or a no-fly zone.
 */
@JsonDeserialize(using = Polygon.PolygonDeserializer.class)
public record Polygon(List<LngLat> coordinates) {


    /**
     * This is a custom deserializer which takes in a Json string from the order endpoint, and turns it into a Polygon object.
     * Reference: <a href="https://www.baeldung.com/jackson-deserialization">...</a>
     */
    @SuppressWarnings("serial")
    protected static class PolygonDeserializer extends StdDeserializer<Polygon> {

        public PolygonDeserializer() {
            this(null);
        }

        public PolygonDeserializer(Class<?> vc) {
            super(vc);
        }

        /**
         * This method deserializes a json string into Polygon objects by taking their coordinates.
         *
         * @param jp The json string.
         * @return Returns a new Polygon object by using a static factory method.
         * @throws IOException If there is an input error regarding the JsonParser.
         */
        @Override
        public Polygon deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);
            if (node.get("longitude") != null && node.get("latitude") != null) {
                List<LngLat> lngLatList = new ArrayList<>();
                Double lng = node.get("longitude").asDouble();
                Double lat = node.get("latitude").asDouble();
                lngLatList.add(new LngLat(lng, lat));
                return Polygon.fromLngLat(lngLatList);
            } else if (node.get("coordinates") != null) {
                Double[][] coordinates = new ObjectMapper().convertValue(node.get("coordinates"), Double[][].class);
                return Polygon.fromDoubleArray(coordinates);
            }
            return null;
        }
    }

    /**
     * This is a static factory method to convert a central area received from the REST server into a Polygon object.
     *
     * @param coordinates List of vertices in LngLat format.
     * @return A new Polygon object.
     */
    public static Polygon fromLngLat(List<LngLat> coordinates) {
        if (coordinates.size() < 3) {
            System.err.println("A polygon must have at least three vertices.");
            System.exit(1);
        }
        return new Polygon(coordinates);
    }

    /**
     * This is a static factory method to convert no-fly zone data received from the REST server into a Polygon object.
     *
     * @param doubleArray Array of coordinates of each vertex.
     * @return A new Polygon object.
     */
    public static Polygon fromDoubleArray(Double[][] doubleArray) {
        if (doubleArray.length < 3) {
            System.err.println("A polygon must have at least three vertices.");
            System.exit(1);
        }
        List<LngLat> vertices = new ArrayList<>();
        for (Double[] doubles : doubleArray) {
            Double lng = doubles[0];
            Double lat = doubles[1];
            vertices.add(new LngLat(lng, lat));
        }
        return fromLngLat(vertices);
    }

    /**
     * Returns true if the point l3 is on the line l1->l2.
     * We can use this check as we will already have checked that l3 is collinear with line l1->l2.
     *
     * @param l1 One vertex of the edge on the polygon.
     * @param l2 The other vertex of the edge on the polygon.
     * @param l3 The point to check if it is on the edge l1->l2.
     * @return Boolean value to check if the point is on the edge.
     */
    static boolean onEdge(LngLat l1, LngLat l2, LngLat l3) {
        return (l3.lng() <= Math.max(l1.lng(), l2.lng()) &&
                l3.lng() >= Math.min(l1.lng(), l2.lng()) &&
                l3.lat() <= Math.max(l1.lat(), l2.lat()) &&
                l3.lat() >= Math.min(l1.lat(), l2.lat()));
    }

    /**
     * This method calculates whether line l1->l2 is collinear with point l3 by calculating whether the gradient
     * of l1->l2 is equal to that of l2->l3.
     *
     * @param l1 Point 1.
     * @param l2 Point 2.
     * @param l3 Point 3.
     * @return An integer:
     * 0 if l3 is collinear with line l1->l2.
     * 1 if l2->l3 is anticlockwise to l1->l2.
     * 2 if l2->l3 is clockwise to l1->l2.
     */
    static int calcCollinear(LngLat l1, LngLat l2, LngLat l3) {
        Double ans = ((l2.lat() - l1.lat()) * (l3.lng() - l2.lng())) - ((l3.lat() - l2.lat()) * (l2.lng() - l1.lng()));
        if (ans == 0) {
            return 0;
        } else if (ans < 0) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * This method calculates whether the line l1->l2 intersects with the line l3->l4.
     *
     * @param l1 Point 1.
     * @param l2 Point 2.
     * @param l3 Point 3.
     * @param l4 Point 4.
     * @return Boolean value of whether the two lines intersect with each other.
     */
    static boolean areLinesIntersecting(LngLat l1, LngLat l2, LngLat l3, LngLat l4) {
        int d1 = calcCollinear(l1, l2, l3); // Check if l3 is collinear with l1->l2
        int d2 = calcCollinear(l1, l2, l4); // Check if l4 is collinear with l1->l2
        int d3 = calcCollinear(l3, l4, l1); // Check if l1 is collinear with l3->l4
        int d4 = calcCollinear(l3, l4, l2); // Check if l2 is collinear with l3->l4

        if ((d1 != d2) && (d3 != d4)) { // When the two lines intersect each other without being collinear.
            return true;
        }

        if (d1 == 0 && onEdge(l1, l2, l3)) { // If l1, l2 and l3 are collinear and l3 is on l1->l2
            return true;
        }
        if (d2 == 0 && onEdge(l1, l2, l4)) { // If l1, l2 and l4 are collinear and l4 is on l1->l2
            return true;
        }
        if (d3 == 0 && onEdge(l3, l4, l1)) { // If l3, l4 and l1 are collinear and l1 is on l3->l4
            return true;
        }
        if (d4 == 0 && onEdge(l3, l4, l2)) { // If l3, l4 and l2 are collinear and l2 is on l3->l4
            return true;
        }
        return false;
    }

    /**
     * This method calculates whether the line l1->l2 is intersecting l3->l4. Intersecting in this method does not refer
     * to one end of the line lying on another.
     *
     * @param l1 Coordinates for line l1->l2.
     * @param l2 Coordinates for line l1->l2.
     * @param l3 Coordinates for line l3->l4.
     * @param l4 Coordinates for line l3->l4.
     * @return A boolean value on whether line l1->l2 intersects line l3->l4.
     */
    static boolean areLinesIntersectingNonCollinear(LngLat l1, LngLat l2, LngLat l3, LngLat l4) {
        if (l1.equals(l3) || l1.equals(l4) || l2.equals(l3) || l2.equals(l4)) {
            return false;
        }

        int d1 = calcCollinear(l1, l2, l3); // Check if l3 is collinear with l1->l2
        int d2 = calcCollinear(l1, l2, l4); // Check if l4 is collinear with l1->l2
        int d3 = calcCollinear(l3, l4, l1); // Check if l1 is collinear with l3->l4
        int d4 = calcCollinear(l3, l4, l2); // Check if l2 is collinear with l3->l4

        if (d1 == 0 || d2 == 0 || d3 == 0 || d4 == 0) {
            return false;
        }

        if ((d1 != d2) && (d3 != d4)) { // When the two lines intersect each other without being collinear.
            return true;
        }
        return false; // If one point is collinear with a line, return false as travelling along the boundary is fine.
    }

    /**
     * This method checks if the line l1->l2 intersects fully with an edge of this polygon. Intersecting in this method
     * does not refer to one end of the line lying on another.
     *
     * @param l1 Coordinates for line l1->l2.
     * @param l2 Coordinates for line l1->l2.
     * @return A boolean value on whether the line intersects with an edge of a polygon.
     */
    boolean isLineIntersectingNfz(LngLat l1, LngLat l2) {
        var n = coordinates.size();
        for (int i = 0; i < n; i++) {
            var v1 = coordinates.get(i);
            // This will not give NullPointerException as 0 <= (i + 1) % n <= n - 1
            var v2 = coordinates.get((i + 1) % n);
            if (areLinesIntersectingNonCollinear(l1, l2, v1, v2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method determines whether the given point is inside the polygon which is constructed from the list of
     * coordinates, and its output varies depending on the context.
     *
     * @param p               Point in question.
     * @param allowBoundaries If true, this method returns false if the point is on the boundary. If false, it returns
     *                        true if the point is on the boundary.
     * @return Boolean value of whether the point is inside the polygon.
     * Reference: <a href="https://www.tutorialspoint.com/Check-if-a-given-point-lies-inside-a-Polygon">...</a>
     */
    boolean isInsidePolygon(LngLat p, boolean allowBoundaries) {
        Double MAX = 181.0; // Largest longitude value on Earth is 180
        var n = coordinates.size();

        if (n < 3) {
            return false;
        }
        var maxLngPoint = new LngLat(MAX, p.lat());

        // Since we are calculating how many times the point intersects the edges, if the point intersects a vertex,
        // it will count as an intersection twice for the two neighbouring edges of the vertex.
        var count = 0;
        var edge = 0; // Current edge of the polygon
        do {
            var v1 = coordinates.get(edge % n); // Vertex 1
            var v2 = coordinates.get((edge + 1) % n); // Vertex 2

            if (allowBoundaries) {
                if (areLinesIntersectingNonCollinear(v1, v2, p, maxLngPoint)) {
                    // We do not need to check calcCollinear == 0 since the above method checks that anyway.
                    count++;
                }
            } else {
                if (areLinesIntersecting(v1, v2, p, maxLngPoint)) {
                    if (calcCollinear(v1, v2, p) == 0) {
                        return onEdge(v1, v2, p); // Return true since point is on the edge (thus in the area)
                    }
                    count++;
                }
            }
            edge++;
        } while (edge % n != 0); // Iterate through all edges of the polygon.
        // Point is inside polygon if the line extending its longitude to +infinity intersects it an odd number of times.
        return (count % 2 == 1);
    }

    @Override
    public String toString() {
        return "Polygon{" +
                "vertices=" + coordinates +
                '}';
    }
}
