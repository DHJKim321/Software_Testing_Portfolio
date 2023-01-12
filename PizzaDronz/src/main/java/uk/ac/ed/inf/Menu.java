package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This record is mapped from the list of menu items received from the REST server.
 */
public record Menu(@JsonProperty String name, @JsonProperty int priceInPence) {

    /**
     * This is a static factory method used for creating Menu objects.
     *
     * @param name         Name of the menu entry.
     * @param priceInPence Price of the menu entry.
     * @return A Menu object.
     */
    public static Menu createMenu(String name, int priceInPence) {
        return new Menu(name, priceInPence);
    }

    @Override
    public String toString() {
        return "Menu{" +
                "name='" + name + '\'' +
                ", priceInPence=" + priceInPence +
                '}';
    }
}
