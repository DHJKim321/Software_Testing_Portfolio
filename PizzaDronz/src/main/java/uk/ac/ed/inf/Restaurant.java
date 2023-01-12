package uk.ac.ed.inf;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Arrays;


/**
 * This record represents a restaurant, with its coordinates in longitude and latitude and a list of its menus.
 */
@JsonDeserialize(using = Restaurant.RestaurantDeserializer.class)
public record Restaurant(String name, LngLat coord, Menu[] menu) {


    /**
     * This is a custom deserializer which takes in a Json string from the order endpoint, and turns it into a Restaurant
     * object.
     * Reference: <a href="https://www.baeldung.com/jackson-deserialization">...</a>
     */
    @SuppressWarnings("serial")
    static class RestaurantDeserializer extends StdDeserializer<Restaurant> {

        public RestaurantDeserializer() {
            this(null);
        }

        public RestaurantDeserializer(Class<?> vc) {
            super(vc);
        }

        /**
         * This method deserializes a json string into Order objects by taking restaurant name, coordinates, and menu.
         *
         * @param jp The json string.
         * @return Returns a new Restaurant object by using a static factory method.
         * @throws IOException If there is an input error regarding the JsonParser.
         */
        @Override
        public Restaurant deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JacksonException {
            JsonNode node = jp.getCodec().readTree(jp);
            var name = node.get("name").asText();
            Double lng = node.get("longitude").asDouble();
            Double lat = node.get("latitude").asDouble();
            var menu = new ObjectMapper().convertValue(node.get("menu"), Menu[].class);

            return new Restaurant(name, new LngLat(lng, lat), menu);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Restaurant that = (Restaurant) o;

        return coord.equals(that.coord);
    }

    @Override
    public int hashCode() {
        return coord.hashCode();
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "name=" + name +
                ", coord=" + coord +
                ", menu=" + Arrays.toString(menu) +
                '}';
    }
}
