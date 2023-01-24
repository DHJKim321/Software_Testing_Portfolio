package uk.ac.ed.inf;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;


/**
 * This class automatically reads and deserialises the relevant data from the REST server into different objects.
 */
public class Client {

    private final String date;
    private final String baseURL;

    protected Client(String date, String baseURL) {
        this.date = initialiseDate(date);
        this.baseURL = initialiseBaseURL(baseURL);
    }

    /**
     * This is a static factory method to create new Client objects
     *
     * @param date    Date in "YYYY/mm/dd" format.
     * @param baseURL Base URL of the REST server.
     * @return A new IClient object.
     */
    public static Client createClient(String date, String baseURL) {
        return new Client(date, baseURL);
    }


    /**
     * This method initialises the base URL and checks that it is valid.
     * A valid URL should be in the form
     *
     * @param baseURL baseURL of the REST service.
     */
    public String initialiseBaseURL(String baseURL) {
        try {
            var url = new URL(baseURL);
            url.toURI();
            if (!baseURL.endsWith("/")) {
                baseURL += "/";
            }
        } catch (MalformedURLException e) {
            System.err.println("Invalid URL format: " + baseURL);
            System.exit(1);
        } catch (URISyntaxException e) {
            System.err.println("The URL could not be parsed as a URI reference: " + baseURL);
            System.exit(1);
        }
        return baseURL;
    }

    /**
     * This method initialises and validates the date that is passed in.
     *
     * @param date The specific date for the orders in the format YYYY-mm-dd.
     */
    public String initialiseDate(String date) {
        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format: " + date);
            System.exit(1);
        }
        return date;
    }

    /**
     * This method receives the json string from the REST server and deserialises it to an object of the given class.
     *
     * @param endPoint The endpoint of the ILP REST server (orders, restaurants, noFlyZones, centralArea)
     * @param klass    Respective class of the deserialized json fields.
     * @return Returns an (array) of objects received from the endpoint.
     */
    public <T> T getResponse(String endPoint, Class<T> klass) {
        if (endPoint.equals("orders")) {
            endPoint += "/" + date;
        }
        if (!endPoint.startsWith("/")) {
            endPoint = "/" + endPoint;
        }
        URL url = null;
        try {
            url = new URL(baseURL + endPoint);
            url.toURI();
        } catch (MalformedURLException e) {
            System.err.println("Invalid final URL: " + baseURL + endPoint);
            System.exit(1);
        } catch (URISyntaxException e) {
            System.err.println("The URL could not be parsed as a URI reference: " + baseURL);
            System.exit(1);
        }
        T response = null;
        try {
            response = new ObjectMapper().readValue(url, klass);
        } catch (StreamReadException e) {
            System.err.println("Unable to process or parse JSON stream");
            System.exit(1);
        } catch (DatabindException e) {
            System.err.println("Unable to convert JSON to POJO.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Unable to read from REST server.");
            System.exit(1);
        }
        return response;
    }

}
