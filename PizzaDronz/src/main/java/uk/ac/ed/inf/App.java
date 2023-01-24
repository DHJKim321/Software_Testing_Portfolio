package uk.ac.ed.inf;


/**
 * App for drone pizza delivery system.
 */
public class App {
    /**
     * Main method for starting the application.
     *
     * @param args
     *      args[0] = Date in YYYY-MM-dd format
     *      args[1] = REST server URL
     *      args[2] = random seed (Not used)
     */
    public static void main(String[] args) {
        var controller = new Controller();
        controller.startApp(args);
    }
}
