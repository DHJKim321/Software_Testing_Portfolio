package uk.ac.ed.inf;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;


/**
 * This record contains the necessary order details such as order number, card details, total price, and list of items.
 */
@JsonDeserialize(using = Order.OrderDeserializer.class)
public record Order(String orderNo, Card card, int priceTotalInPence, String[] orderItems) {


    /**
     * This is a custom deserializer which takes in a Json string from the order endpoint, and turns it into an Order object.
     * Reference: <a href="https://www.baeldung.com/jackson-deserialization">...</a>
     */
    static class OrderDeserializer extends StdDeserializer<Order> {

        public OrderDeserializer() {
            this(null);
        }

        public OrderDeserializer(Class<?> vc) {
            super(vc);
        }

        /**
         * This method deserializes a json string into Order objects by taking the order number, price, order items, and
         * card details, which it converts into a Card object.
         *
         * @param jp The json string.
         * @return Returns a new Order object by using a static factory method.
         * @throws IOException If there is an input error regarding the JsonParser.
         */
        @Override
        public Order deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);
            var orderNo = node.get("orderNo").asText();
            int priceTotalInPence = node.get("priceTotalInPence").asInt();
            var orderItems = new ObjectMapper().convertValue(node.get("orderItems"), String[].class);
            var creditCardNumber = node.get("creditCardNumber").asText();
            var creditCardExpiry = node.get("creditCardExpiry").asText();
            var cvv = node.get("cvv").asText();

            return new Order(orderNo,
                    Card.createCard(creditCardNumber, creditCardExpiry, cvv),
                    priceTotalInPence,
                    orderItems);
        }
    }


    private static final int DELIVERY_FEE = 100;

    public static int getDeliveryFee() {
        return DELIVERY_FEE;
    }

}
