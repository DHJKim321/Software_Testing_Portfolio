package uk.ac.ed.inf;


import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Random;

/**
 * Unit test for simple App.
 */
public class AppTest {


    @Test
    public void randomSample7Days() {
        Random random = new Random();
        double sum = 0;
        for (int i = 0; i < 7; i++) {
            int minDay = (int) LocalDate.of(2023, 1, 1).toEpochDay();
            int maxDay = (int) LocalDate.of(2023, 5, 31).toEpochDay();
            long randomDay = minDay + random.nextInt(maxDay - minDay);

            String date = LocalDate.ofEpochDay(randomDay).toString();
            String url = "https://ilp-rest.azurewebsites.net/";
            Controller controller = new Controller();
            controller.startApp(new String[]{date, url, "cabbage"});
            sum += controller.getDeliveredOrders();
        }
        System.out.println("Out of 7 days, the average number of pizzas delivered was: " + sum / 7.0);
    }

    @Test
    public void randomSample10Days() {
        Random random = new Random();
        double sum = 0;
        for (int i = 0; i < 10; i++) {
            int minDay = (int) LocalDate.of(2023, 1, 1).toEpochDay();
            int maxDay = (int) LocalDate.of(2023, 5, 31).toEpochDay();
            long randomDay = minDay + random.nextInt(maxDay - minDay);

            String date = LocalDate.ofEpochDay(randomDay).toString();
            String url = "https://ilp-rest.azurewebsites.net/";
            Controller controller = new Controller();
            controller.startApp(new String[]{date, url, "cabbage"});
            sum += controller.getDeliveredOrders();
        }
        System.out.println("Out of 10 days, the average number of pizzas delivered was: " + sum / 10.0);
    }

    @Test
    public void randomSample12Days() {
        Random random = new Random();
        double sum = 0;
        for (int i = 0; i < 12; i++) {
            int minDay = (int) LocalDate.of(2023, 1, 1).toEpochDay();
            int maxDay = (int) LocalDate.of(2023, 5, 31).toEpochDay();
            long randomDay = minDay + random.nextInt(maxDay - minDay);

            String date = LocalDate.ofEpochDay(randomDay).toString();
            String url = "https://ilp-rest.azurewebsites.net/";
            Controller controller = new Controller();
            controller.startApp(new String[]{date, url, "cabbage"});
            sum += controller.getDeliveredOrders();
        }
        System.out.println("Out of 12 days, the average number of pizzas delivered was: " + sum / 12.0);
    }

    @Test
    public void randomSample30Days() {
        Random random = new Random();
        double sum = 0;
        for (int i = 0; i < 30; i++) {
            int minDay = (int) LocalDate.of(2023, 1, 1).toEpochDay();
            int maxDay = (int) LocalDate.of(2023, 5, 31).toEpochDay();
            long randomDay = minDay + random.nextInt(maxDay - minDay);

            String date = LocalDate.ofEpochDay(randomDay).toString();
            String url = "https://ilp-rest.azurewebsites.net/";
            Controller controller = new Controller();
            controller.startApp(new String[]{date, url, "cabbage"});
            sum += controller.getDeliveredOrders();
        }
        System.out.println("Out of 30 days, the average number of pizzas delivered was: " + sum / 30.0);
    }

    @Test
    public void calculateAllOrders() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = formatter.parse("2023-01-01");
        Date endDate = formatter.parse("2023-05-31");
        LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
            String url = "https://ilp-rest.azurewebsites.net/";
            Controller controller = new Controller();
            controller.startApp(new String[]{date.toString(), url, "cabbage"});
        }
    }
}
