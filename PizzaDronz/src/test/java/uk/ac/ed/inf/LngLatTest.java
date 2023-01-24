package uk.ac.ed.inf;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for LngLat class.
 */
public class LngLatTest {

    @Test
    public void createLngLatAppletonTower() {
        LngLat appleton = LngLat.createAppletonLngLat();
        assertEquals(-3.186874, appleton.lng(), 0.001);
        assertEquals(55.944494, appleton.lat(), 0.001);
    }

    @Test
    public void createLngLatAtStartPoint() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(0.0, 1.0);
        assertEquals(new LngLat(0.0, 0.0), LngLat.createLngLatInBetween(l1, l2, 0.0));
    }

    @Test
    public void createLngLatAtEndPoint() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(0.0, 1.0);
        assertEquals(new LngLat(0.0, 1.0), LngLat.createLngLatInBetween(l1, l2, 1.0));
    }

    @Test
    public void createLngLatInBetweenTwoPoints() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(0.0, 1.0);
        assertEquals(new LngLat(0.0, 0.5), LngLat.createLngLatInBetween(l1, l2, 0.5));
    }

    @Test
    public void createLngLatOutsideTwoPoints() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(0.0, 1.0);
        assertNull(LngLat.createLngLatInBetween(l1, l2, 1.5));
    }

    @Test
    public void distanceToAnotherLngLat() {
        double epsilon = 0.0000001d;
        LngLat lngLat1 = new LngLat(3.0, 4.0);
        LngLat lngLat2 = new LngLat(5.0, 10.0);
        assertEquals(6.324555320336759, lngLat1.distanceTo(lngLat2), epsilon);
    }

    @Test
    public void distanceToItself() {
        LngLat lngLat1 = new LngLat(3.0, 4.0);
        assertEquals(0.0, lngLat1.distanceTo(lngLat1), 0.0);
    }

    @Test
    public void distanceToNull() {
        LngLat lngLat1 = new LngLat(3.0, 4.0);
        assertEquals(-1, lngLat1.distanceTo(null), 0);
    }

    @Test
    public void closeToAnotherLngLat() {
        LngLat lngLat1 = new LngLat(0.0, 0.0);
        LngLat lngLat2 = new LngLat(0.0, 0.000149);
        assertTrue(lngLat1.closeTo(lngLat2));
    }

    @Test
    public void notCloseToAnotherLngLat() {
         LngLat lngLat1 = new LngLat(0.0, 0.0);
         LngLat lngLat2 = new LngLat(0.0, 0.00016);
         assertFalse(lngLat1.closeTo(lngLat2));
    }

    @Test
    public void closeToNull() {
        LngLat lngLat1 = new LngLat(3.0, 4.0);
        assertFalse(lngLat1.closeTo(null));
    }

    @Test
    public void closeToAccuracyError() {
         LngLat lngLat1 = new LngLat(0.0, 0.0);
         LngLat lngLat2 = new LngLat(0.00015 + Math.pow(10, -13), 0.0);
         assertTrue(lngLat1.closeTo(lngLat2));
    }

    @Test
    public void nextPositionNull() {
        LngLat lngLat = new LngLat(0.0, 0.0);
        assertEquals(lngLat.nextPosition(null).lng(), lngLat.lng(), 0.0);
        assertEquals(lngLat.nextPosition(null).lat(), lngLat.lat(), 0.0);
    }

    @Test
    public void nextPositionNotNull() {
        LngLat lngLat1 = new LngLat(0.0, 0.0);
        double epsilon = 0.0000001d;
        assertEquals(0.000106, lngLat1.nextPosition(Direction.SE).lng(), epsilon);
        assertEquals(-0.000106, lngLat1.nextPosition(Direction.SE).lat(), epsilon);
    }

}
