package uk.ac.ed.inf;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PolygonTest {
    
    @Test
    public void threePointsCollinearTest() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(1.0, 1.0);
        LngLat l3 = new LngLat(0.5, 0.5);
        assertEquals(0, Polygon.calcCollinear(l1, l2, l3));
    }

    @Test
    public void threePointsCollinearOneInfinity() {
        Double MAX = 181.0;
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(5.0, 0.0);
        LngLat l3 = new LngLat(MAX, 0.0);
        assertEquals(0, Polygon.calcCollinear(l1, l2, l3));
    }

    @Test
    public void isIntersectingTest() {
        Double MAX = 181.0;
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(1.0, 1.0);
        LngLat l3 = new LngLat(0.0, 5.0);
        LngLat l4 = new LngLat(MAX, 5.0);
        assertFalse(Polygon.areLinesIntersecting(l1, l2, l3, l4));
    }

    @Test
    public void isIntersectingTwoSamePoints() {
        Double MAX = 181.0;
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(1.0, 1.0);
        LngLat l3 = new LngLat (1.0, 1.0);
        LngLat l4 = new LngLat (MAX, 1.0);
        assertTrue(Polygon.areLinesIntersecting(l1, l2, l3, l4));
    }


    @Test
    public void isNotIntersectingPathfinding() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(1.0, 1.0);
        LngLat l3 = new LngLat (0.2, 0.5);
        LngLat l4 = new LngLat (0.2, 1.0);
        assertFalse(Polygon.areLinesIntersectingNonCollinear(l1, l2, l3, l4));
    }

    @Test
    public void isIntersectingNoCollinearTestPathfinding() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(1.0, 1.0);
        LngLat l3 = new LngLat (0.0, 0.5);
        LngLat l4 = new LngLat (1.0, 0.5);
        assertTrue(Polygon.areLinesIntersectingNonCollinear(l1, l2, l3, l4));
    }

    @Test
    public void isOnBoundaryPathfindingIntersection() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(1.0, 1.0);
        LngLat l3 = new LngLat (0.0, 0.5);
        LngLat l4 = new LngLat (1.0, 1.0);
        assertFalse(Polygon.areLinesIntersectingNonCollinear(l1, l2, l3, l4));
    }

    @Test
    public void boundaryInBetweenLinePathfinding() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(1.0, 1.0);
        LngLat l3 = new LngLat (0.5, 0.5);
        LngLat l4 = new LngLat (1.0, 0.0);
        assertFalse(Polygon.areLinesIntersectingNonCollinear(l1, l2, l3, l4));
    }

    @Test
    public void lineDiagonalInsideNoFlyZone() {
        List<LngLat> lst = new ArrayList(Arrays.asList(
                new LngLat(0.0, 0.0),
                new LngLat(0.0, 1.0),
                new LngLat(1.0, 1.0),
                new LngLat(1.0, 0.0)
        ));
        LngLat l1 = new LngLat(0.5, 0.5);
        Polygon nfz = Polygon.fromLngLat(lst);
        System.out.println(nfz.isInsidePolygon(l1, true));
    }

    @Test
    public void test() {
        List<LngLat> lst = new ArrayList(Arrays.asList(
                new LngLat(-3.190578818321228, 55.94402412577528),
                new LngLat(-3.1899887323379517, 55.94284650540911),
                new LngLat(-3.187097311019897, 55.94328811724263),
                new LngLat(-3.187682032585144, 55.944477740393744)
                //new LngLat(-3.190578818321228, 55.944024125775280)
        ));
        var p = new LngLat(-3.190994366168976, 55.94287325270456);
        Polygon nfz = Polygon.fromLngLat(lst);
        assertFalse(nfz.isInsidePolygon(p, true));
        // I was looping one too many times
    }

}
