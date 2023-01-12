package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mapbox.geojson.Point;

/**
 * This record represents a point on a map, taking a longitude and latitude as doubles.
 *
 * @param lng Longitude of the point in double.
 * @param lat Latitude of the point in double.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LngLat(@JsonProperty("longitude") Double lng, @JsonProperty("latitude") Double lat) {

    private static final Double MOVE_DISTANCE = 0.00015;
    private static final Double CLOSE_THRESHOLD = 0.00015;
    private static final Double APPLETON_LNG = -3.186874;
    private static final Double APPLETON_LAT = 55.944494;

    /**
     * This is a static factory method that creates a LngLat object 'decimal' along the line p->q.
     * E.g. if decimal == 0.5, then this method will return a LngLat that is halfway between line p->q.
     *
     * @param p       One end of the line p->q
     * @param q       Other end of the line p->q
     * @param decimal Decimal value of how far along the point should be on the line p->q.
     * @return A LngLat object which lies some distance along the line p->q.
     */
    public static LngLat createLngLatInBetween(LngLat p, LngLat q, Double decimal) {
        if (decimal == 0.0) {
            return p;
        }
        if (decimal == 1.0) {
            return q;
        }
        if (decimal < 0.0 || decimal > 1.0) {
            System.err.println("Invalid decimal value: " + decimal);
        }
        Double lng = (p.lng + q.lng) * decimal;
        Double lat = (p.lat + q.lat) * decimal;
        return new LngLat(lng, lat);
    }

    /**
     * This method creates a new LngLat at the specified Appleton Tower coordinates.
     *
     * @return Appleton Tower LngLat object.
     */
    public static LngLat createAppletonLngLat() {
        return new LngLat(APPLETON_LNG, APPLETON_LAT);
    }

    /**
     * This method returns a double precision value of the pythagorean distance between two LngLat objects.
     *
     * @param lngLat The object to which the distance is calculated.
     * @return The distance between the two objects.
     */
    public Double distanceTo(LngLat lngLat) {
        if (lngLat == null) {
            System.err.println("The LngLat object provided was null");
            return -1.0;
        }
        return Math.sqrt(
                Math.pow(lat - lngLat.lat, 2) + Math.pow(lng - lngLat.lng, 2)
        );
    }

    /**
     * This method returns a boolean value to see if two objects are close to each other (0.00015 degrees).
     *
     * @param lngLat The object to see if it is close to the current object.
     * @return The boolean value of whether the two objects are close to each other.
     */
    public boolean closeTo(LngLat lngLat) {
        if (lngLat == null) {
            System.err.println("The LngLat object provided was null");
            return false;
        }
        return distanceTo(lngLat) < (CLOSE_THRESHOLD + Math.pow(10, -12));
    }

    /**
     * This method returns a new LngLat object with the updated position after it has moved in a certain direction.
     * It returns its original coordinates if d is null, i.e. the drone is hovering.
     *
     * @param d The double compass direction in degrees in which the object will move.
     * @return A new LngLat with the updated coordinates.
     */
    public LngLat nextPosition(Direction d) {
        if (d == null) {
            return this;
        }
        Double rad = d.getAngle() * (Math.PI / 180.0);
        return new LngLat(lng + MOVE_DISTANCE * Math.cos(rad),
                (lat + MOVE_DISTANCE * Math.sin(rad)));
    }

    /**
     * This method turns the LngLat object into a Point object in the GeoJSON library.
     * @return A new Point object.
     */
    public Point toPoint() {
        return Point.fromLngLat(lng, lat);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LngLat lngLat = (LngLat) o;

        if (Double.compare(lngLat.lng, lng) != 0) return false;
        return Double.compare(lngLat.lat, lat) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(lng);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lat);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "LngLat{" +
                "lng=" + lng +
                ", lat=" + lat +
                '}';
    }
}
