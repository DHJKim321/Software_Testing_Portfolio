package uk.ac.ed.inf;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * This static class deals with writing out data in JSON/GeoJSON format calculated by the system.
 */
@SuppressWarnings("unchecked")
public class JsonWriter {
    private final String date;

    public JsonWriter(String date) {
        this.date = date;
    }

    /**
     * This method writes out the orders and the corresponding information to a JSON file:
     * orderNo - Order number
     * outcome - An OrderOutcome enum value which describes whether the order was delivered or invalid.
     * costInPence - The total cost of the order, if the total cost was wrong, this value can safely be discarded.
     *
     * @param validatedOrders List of orders to be parsed.
     * @throws IOException This exception is thrown when the file cannot be created.
     */
    public void writeDeliveries(List<ValidatedOrder> validatedOrders) throws IOException {
        var deliveries = new JSONArray();
        for (var order : validatedOrders) {
            deliveries.add(order.toJsonObject());
        }
        try (var deliveriesFile = new FileWriter("deliveries-" + date + ".json")) {
            deliveriesFile.write(deliveries.toJSONString());
        }
    }

    /**
     * This method writes out a flight path containing various information about the plan to a JSON file.
     * orderNo - Order number
     * fromLongitude - Longitude value of the initial point
     * fromLatitude - Latitude value of the initial point
     * angle - Direction in which the drone travelled
     * toLongitude - Longitude value of the next point
     * toLatitude - Latitude value of the next point
     * ticksSinceStartOfCalculation - Time it took for each calculation of a move. (In nanoseconds)
     *
     * @param paths List of Move steps that the drone took.
     * @throws IOException This exception is thrown when the file cannot be created.
     */
    public void writeFlightpath(List<Move> paths) throws IOException {
        var flightPath = new JSONArray();
        for (int i = 0; i < paths.size(); i++) {
            var currPath = paths.get(i);
            Move nextPath;
            if (i < paths.size() - 1) {
                nextPath = paths.get(i + 1);
            } else { // When i == paths.size() - 1, the current direction will be to hover. Thus, the next coordinates
                // will be the same, and we can point nextPath at currPath.
                nextPath = currPath;
            }
            var move = new JSONObject();
            move.put("orderNo", currPath.orderNo());
            move.put("fromLongitude", currPath.coordinates().lng());
            move.put("fromLatitude", currPath.coordinates().lat());
            move.put("angle", currPath.direction() == null ? null : currPath.direction().getAngle());
            move.put("toLongitude", nextPath.coordinates().lng());
            move.put("toLatitude", nextPath.coordinates().lat());
            move.put("ticksSinceStartOfCalculation", currPath.tick());
            flightPath.add(move);
        }
        try (var flightPathFile = new FileWriter("flightpath-" + date + ".json")) {
            flightPathFile.write(flightPath.toJSONString());
        }
    }

    /**
     * This method writes the flight path of the drone in GeoJSON format.
     * See here for detailed GeoJSON specifications: <a href="https://geojson.org/">...</a>
     *
     * @param allPaths List of points which form a path that the drone took while it was operating.
     * @throws IOException This exception is thrown when the file cannot be created.
     */
    public void writeGeoJson(List<Move> allPaths) throws IOException {
        var points = allPaths.stream().map(m -> m.coordinates().toPoint()).toList();
        var lineString = LineString.fromLngLats(points);
        var feature = com.mapbox.geojson.Feature.fromGeometry(lineString);
        var featureCollection = FeatureCollection.fromFeature(feature);
        try (var droneGeoJsonFile = new FileWriter("drone-" + date + ".geojson")) {
            droneGeoJsonFile.write(featureCollection.toJson());
        }
    }

}
