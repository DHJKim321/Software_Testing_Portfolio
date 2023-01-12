package uk.ac.ed.inf;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit test for LngLat class.
 */
public class LngLatTest {
    Polygon centralArea;

     @Before
     public void initialiseURL() {
         Client c = Client.createClient("2023-01-01", "https://ilp-rest.azurewebsites.net/");
         centralArea = Polygon.fromLngLat(List.of(c.getResponse("centralArea", LngLat[].class)));
     }

    @Test
    public void initialiseLngLat() {
        LngLat lngLat = new LngLat(3.0, 4.0);
        assertTrue(lngLat.lng() == 3.0 && lngLat.lat() == 4.0);
    }

    @Test
    public void lngLatInCentralArea() {
        LngLat lngLat = new LngLat(-3.190, 55.943);
        assertTrue(centralArea.isInsidePolygon(lngLat, false));
    }

    @Test
    public void lngLatParallelToCentralAreaEdgeAndNotInArea() {
        LngLat lngLat = new LngLat(0.0, 55.946233);
        assertFalse(centralArea.isInsidePolygon(lngLat, false));
    }

    @Test
    public void lngLatParallelToCentralAreaEdgeAndOnEdge() {
        LngLat lngLat = new LngLat(-3.19, 55.946233);
        assertTrue(centralArea.isInsidePolygon(lngLat, false));
    }

    @Test
    public void lngLatOnVertexOfArea() {
        LngLat lngLat = new LngLat(-3.192473, 55.946233);
        assertTrue(centralArea.isInsidePolygon(lngLat, false));
    }

    @Test
    public void lngLatNotInCentralArea() {
        LngLat lngLat = new LngLat(3.190, 55.92);
        assertFalse(centralArea.isInsidePolygon(lngLat, false));
    }

    @Test
    public void testDistance1() {
        double epsilon = 0.0000001d;
        LngLat lngLat1 = new LngLat(3.0, 4.0);
        LngLat lngLat2 = new LngLat(5.0, 10.0);
        assertEquals(6.324555320336759, lngLat1.distanceTo(lngLat2), epsilon);
    }

    @Test
    public void testDistance2() {
        LngLat lngLat1 = new LngLat(3.0, 4.0);
        assertEquals(0.0, lngLat1.distanceTo(lngLat1), 0.0);
    }

    @Test
    public void testDistance3() {
        double epsilon = 0.0000001d;
        LngLat lngLat1 = new LngLat(3.0, 4.0);
        LngLat lngLat2 = new LngLat(3.0001, 4.00006);
        assertEquals(0.0001166, lngLat1.distanceTo(lngLat2), epsilon);
    }

    @Test
    public void testDistance4() {
        double epsilon = 0.0000001d;
        LngLat lngLat1 = new LngLat(3.0, 4.0);
        LngLat lngLat2 = new LngLat(3.0001, 4.00006);
        assertEquals(lngLat1.distanceTo(lngLat2), lngLat2.distanceTo(lngLat1), epsilon);
    }

    @Test
    public void testDistanceNull() {
        LngLat lngLat1 = new LngLat(3.0, 4.0);
        assertEquals(-1, lngLat1.distanceTo(null), 0);
    }

    @Test
    public void testCloseTo1() {
        LngLat lngLat1 = new LngLat(0.0, 0.0);
        LngLat lngLat2 = new LngLat(0.0, 0.000149);
        assertTrue(lngLat1.closeTo(lngLat2));
    }

    @Test
    public void testCloseTo2() {
        LngLat lngLat1 = new LngLat(3.0, 4.0);
        LngLat lngLat2 = new LngLat(3.0001, 4.00006);
        assertTrue(lngLat1.closeTo(lngLat2));
        assertTrue(lngLat2.closeTo(lngLat1));
    }

    @Test
    public void testNotCloseTo() {
         LngLat lngLat1 = new LngLat(0.0, 0.0);
         LngLat lngLat2 = new LngLat(0.0, 0.00016);
         assertFalse(lngLat1.closeTo(lngLat2));
    }

    @Test
    public void testCloseToNull() {
        LngLat lngLat1 = new LngLat(3.0, 4.0);
        assertFalse(lngLat1.closeTo(null));
    }

    @Test
    public void testAccuracyErrorCloseTo() {
         LngLat lngLat1 = new LngLat(0.0, 0.0);
         LngLat lngLat2 = new LngLat(0.0 + Math.pow(10, -13), 0.0);
         assertTrue(lngLat1.closeTo(lngLat2));
    }

    @Test
    public void testNextPositionNull() {
        LngLat lngLat = new LngLat(0.0, 0.0);
        assertEquals(lngLat.nextPosition(null).lng(), lngLat.lng(), 0.0);
        assertEquals(lngLat.nextPosition(null).lat(), lngLat.lat(), 0.0);
    }

    @Test
    public void testNextPositionHorizontal() {
        LngLat lngLat1 = new LngLat(0.0, 0.0);
        assertEquals(0.00015, lngLat1.nextPosition(Direction.E).lng(), 0.0);
        assertEquals(0, lngLat1.nextPosition(Direction.E).lat(), 0.0);
    }

    @Test
    public void testNextPositionVertical() {
        double epsilon = 0.0000001d;
        LngLat lngLat1 = new LngLat(0.0, 0.0);
        assertEquals(0.0, lngLat1.nextPosition(Direction.S).lng(), epsilon);
        assertEquals(-0.00015, lngLat1.nextPosition(Direction.S).lat(), epsilon);
    }

    @Test
    public void testNextPositionCombination() {
        LngLat lngLat1 = new LngLat(0.0, 0.0);
        double epsilon = 0.0000001d;
        assertEquals(0.000106, lngLat1.nextPosition(Direction.SE).lng(), epsilon);
        assertEquals(-0.000106, lngLat1.nextPosition(Direction.SE).lat(), epsilon);
    }

}
