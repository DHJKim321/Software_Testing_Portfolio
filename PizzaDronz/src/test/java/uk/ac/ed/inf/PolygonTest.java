package uk.ac.ed.inf;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PolygonTest {

    Polygon centralArea;
    Polygon unitArea;

    @Before
    public void initialiseURL() {
        Client c = Client.createClient("2023-01-01", "https://ilp-rest.azurewebsites.net/");
        centralArea = Polygon.fromLngLat(List.of(c.getResponse("centralArea", LngLat[].class)));
    }

    @Before
    public void initialiseUnitArea() {
        List<LngLat> lngLatList = new ArrayList<>();
        lngLatList.add(new LngLat(0.0, 0.0));
        lngLatList.add(new LngLat(0.0, 1.0));
        lngLatList.add(new LngLat(1.0, 0.0));
        lngLatList.add(new LngLat(1.0, 1.0));
        unitArea = Polygon.fromLngLat(lngLatList);
    }
    
    @Test
    public void pointsCollinear() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(1.0, 1.0);
        LngLat l3 = new LngLat(0.5, 0.5);
        assertEquals(0, Polygon.calcCollinear(l1, l2, l3));
    }

    @Test
    public void pointsCollinearOneInfinityLng() {
        Double MAX = 181.0;
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(5.0, 0.0);
        LngLat l3 = new LngLat(MAX, 0.0);
        assertEquals(0, Polygon.calcCollinear(l1, l2, l3));
    }

    @Test
    public void pointsCollinearOneInfinityLat() {
        Double MAX = 181.0;
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(0.0, 3.0);
        LngLat l3 = new LngLat(0.0, MAX);
        assertEquals(0, Polygon.calcCollinear(l1, l2, l3));
    }

    @Test
    public void pointsNotCollinearClockwise() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(0.0, 3.0);
        LngLat l3 = new LngLat(1.0, 2.0);
        assertEquals(2, Polygon.calcCollinear(l1, l2, l3));
    }

    @Test
    public void pointsNotCollinearAnticlockwise() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(0.0, 3.0);
        LngLat l3 = new LngLat(-1.0, 2.0);
        assertEquals(1, Polygon.calcCollinear(l1, l2, l3));
    }

    @Test
    public void linesNotIntersecting() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(1.0, 1.0);
        LngLat l3 = new LngLat(0.0, 5.0);
        LngLat l4 = new LngLat(6.0, 5.0);
        assertFalse(Polygon.areLinesIntersecting(l1, l2, l3, l4));
    }

    @Test
    public void linesIntersectingTwoSamePoints() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(1.0, 1.0);
        LngLat l3 = new LngLat (1.0, 1.0);
        LngLat l4 = new LngLat (3.0, 1.0);
        assertTrue(Polygon.areLinesIntersecting(l1, l2, l3, l4));
    }

    @Test
    public void isIntersectingThreePointsCollinear() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(1.0, 1.0);
        LngLat l3 = new LngLat(0.5, 0.5);
        LngLat l4 = new LngLat(2.0, 0.5);
        assertTrue(Polygon.areLinesIntersecting(l1, l2, l3, l4));
    }

    @Test
    public void linesIntersecting() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(1.0, 1.0);
        LngLat l3 = new LngLat (-1.0, 0.5);
        LngLat l4 = new LngLat (1.0, 0.5);
        assertTrue(Polygon.areLinesIntersecting(l1, l2, l3, l4));
    }

    @Test
    public void linesIntersectingTwoSamePointsNonCollinear() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(1.0, 1.0);
        LngLat l3 = new LngLat (1.0, 1.0);
        LngLat l4 = new LngLat (3.0, 1.0);
        assertFalse(Polygon.areLinesIntersectingNonCollinear(l1, l2, l3, l4));
    }

    @Test
    public void linesIntersectingNonCollinear() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(1.0, 1.0);
        LngLat l3 = new LngLat (-1.0, 0.5);
        LngLat l4 = new LngLat (1.0, 0.5);
        assertTrue(Polygon.areLinesIntersecting(l1, l2, l3, l4));
    }

    @Test
    public void isInsideNotAllowBoundaries() {
        LngLat lngLat = new LngLat(-3.190, 55.943);
        assertTrue(centralArea.isInsidePolygon(lngLat, false));
    }

    @Test
    public void isInsideOnEdgeNotAllowBoundaries() {
        LngLat lngLat = new LngLat(-3.19, 55.946233);
        assertTrue(centralArea.isInsidePolygon(lngLat, false));
    }

    @Test
    public void isOutsideNotAllowBoundaries() {
        LngLat lngLat = new LngLat(3.190, 55.92);
        assertFalse(centralArea.isInsidePolygon(lngLat, false));
    }

    @Test
    public void isInsideAllowBoundaries() {
        LngLat lngLat = new LngLat(-3.190, 55.943);
        assertTrue(centralArea.isInsidePolygon(lngLat, true));
    }

    @Test
    public void lineOnVertexNotIntersectingPolygon() {
        LngLat l1 = new LngLat(-1.0, 0.0);
        LngLat l2 = new LngLat(2.0, 3.0);
        assertFalse(unitArea.isLineIntersectingNfz(l1, l2));
    }

    @Test
    public void lineIntersectingPolygon() {
        LngLat l1 = new LngLat(-1.0, 0.5);
        LngLat l2 = new LngLat(2.0, 0.5);
        assertTrue(unitArea.isLineIntersectingNfz(l1, l2));
    }

    @Test
    public void lineInsidePolygonisIntersecting() {
        LngLat l1 = new LngLat(0.0, 0.0);
        LngLat l2 = new LngLat(1.0, 1.0);
        assertTrue(unitArea.isLineIntersectingNfz(l1, l2));
    }
}
