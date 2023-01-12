package uk.ac.ed.inf;

import com.mapbox.geojson.*;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.junit.Before;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.*;

import static java.util.List.of;

public class GraphTest {
    private int size = 1;

    private WeightedMultigraph<LngLat, DefaultWeightedEdge> generatedGraph;
    private Set<LngLat> coords;
    private Graph graph;
    private List<Polygon> noFlyZones;
    private final List<Long> g1 = new ArrayList<>();
    private final List<Long> g2 = new ArrayList<>();

    public void generateGraphT(int size) {
        // Create the VertexFactory so the generator can create vertices
        Supplier<LngLat> vSupplier = new Supplier<>() {
            @Override
            public LngLat get() {
                return new LngLat(ThreadLocalRandom.current().nextDouble(-3.192473, -3.184319),
                                  ThreadLocalRandom.current().nextDouble(55.942617, 55.946233));
            }
        };

        Supplier<DefaultWeightedEdge> eSupplier = new Supplier<>() {
            @Override
            public DefaultWeightedEdge get() {
                return new DefaultWeightedEdge();
            }
        };

        // Create the graph object
        generatedGraph = new WeightedMultigraph<>(vSupplier, eSupplier);
        // Create the CompleteGraphGenerator object
        CompleteGraphGenerator<LngLat, DefaultWeightedEdge> completeGenerator =
                new CompleteGraphGenerator<>(size);

        // Use the CompleteGraphGenerator object to make completeGraph a
        // complete graph with [size] number of vertices
        addNoFlyZone();
        //generatedGraph.addVertex(LngLat.createAppletonLngLat());
        completeGenerator.generateGraph(generatedGraph);
        setRestaurants();
        generateVisGraph();
    }

    public void generateVisGraph() {
        Set<DefaultWeightedEdge> invisibleEdges = new HashSet<>();
        for (var u : generatedGraph.vertexSet()) {
            for (var e : generatedGraph.incomingEdgesOf(u)) {
                var v = generatedGraph.getEdgeSource(e);
                if (!u.equals(v)) {
                    var mid = LngLat.createLngLatInBetween(u, v, 0.5);
                    boolean isVisible = true;
                    for (Polygon nfz : noFlyZones) {
                        if (nfz.isLineIntersectingNfz(u, v) ||
                                nfz.isInsidePolygon(mid, true)) {
                            invisibleEdges.add(e);
                            isVisible = false;
                            break;
                        }
                    }
                    if (isVisible) {
                        generatedGraph.setEdgeWeight(u, v, u.distanceTo(v));
                    }
                }
            }
        }
        generatedGraph.removeAllEdges(invisibleEdges);
    }

    public void addNoFlyZone() {
        for (var noFlyZone : noFlyZones) {
            for (var u : noFlyZone.coordinates()) {
                generatedGraph.addVertex(u);
            }
        }
    }

    public void setRestaurants() {
        List<LngLat> nfzs = noFlyZones.stream().map(Polygon::coordinates).flatMap(List::stream).toList();
        coords = new HashSet<>(generatedGraph.vertexSet());
        nfzs.forEach(coords::remove);
    }

    public GraphPath<LngLat, DefaultWeightedEdge> getPath(LngLat start, LngLat end) {
        var dijkstra = new DijkstraShortestPath<>(generatedGraph);
        var path = dijkstra.getPaths(start);
        return path.getPath(end);
    }

    public void generateOwnGraph(List<LngLat> restaurants) {
        graph = Graph.createGraph(LngLat.createAppletonLngLat(), noFlyZones, restaurants);
    }

    @Before
    public void initNoFlyZones() {
        noFlyZones = of(Client.createClient("2023-01-01",
                "https://ilp-rest.azurewebsites.net/").getResponse("noFlyZones", Polygon[].class));
    }

    public void makeGeneratedGraph() {
        initNoFlyZones();
        var start = System.currentTimeMillis();
        generateGraphT(size);
        var end = System.currentTimeMillis();
        g1.add(end - start);
        System.out.println("Graph generation for " + size + " restaurants using JGraphT took " + (end - start) + "ms.");
    }

    public void makeOwnGraph() {
        var start = System.currentTimeMillis();
        generateOwnGraph(coords.stream().toList());
        var end = System.currentTimeMillis();
        g2.add(end - start);
        System.out.println("Graph generation for " + size + " restaurants using custom graph took " + (end - start) + "ms.");
    }

    @Test
    public void testAStar() {
        for (var c : coords) {
            System.out.println("Path from Appleton to " + c + " :" + getPath(LngLat.createAppletonLngLat(), c));
        }
    }

    @Test
    public void testGeneratedVisibilityGraph() {
        List<LineString> lineStrings = new ArrayList<>();
        for (var u : generatedGraph.vertexSet()) {
            for (var e : generatedGraph.edgesOf(u)) {
                List<Point> points = new ArrayList<>();
                var v = generatedGraph.getEdgeTarget(e);
                points.add(u.toPoint());
                points.add(v.toPoint());
                lineStrings.add(LineString.fromLngLats(points));
            }
        }
        var multiLineString = MultiLineString.fromLineStrings(lineStrings);
        var feature = Feature.fromGeometry(multiLineString);


        List<Feature> features = new ArrayList<>();
        features.add(feature);
        for (var w : coords) {
            Feature f = Feature.fromGeometry(w.toPoint());
            features.add(f);
        }
        var featureCollection = FeatureCollection.fromFeatures(features);

        try (var visgraph = new FileWriter("generated-vis-graph-.geojson")) {
            visgraph.write(featureCollection.toJson());
        } catch (IOException e) {
            System.err.println();
        }
    }

    @Test
    public void testVisibilityGraph() {
        var allRestaurants = of(Client.createClient("2023-02-02",
                "https://ilp-rest.azurewebsites.net/").getResponse("restaurants", Restaurant[].class));
        var restaurantCoords = allRestaurants.stream().map(Restaurant::coord).toList();
        generateOwnGraph(restaurantCoords);
        List<LineString> lineStrings = new ArrayList<>();
        for (var edges : graph.getNodeToEdges().values()) {
            for (var e : edges) {
                List<Point> points = new ArrayList<>();
                points.add(e.startNode().getCoord().toPoint());
                points.add(e.endNode().getCoord().toPoint());
                lineStrings.add(LineString.fromLngLats(points));
            }
        }
        var multiLineString = MultiLineString.fromLineStrings(lineStrings);
        var feature = Feature.fromGeometry(multiLineString);


        List<Feature> features = new ArrayList<>();
        features.add(feature);
        for (var w : restaurantCoords) {
            Feature f = Feature.fromGeometry(w.toPoint());
            features.add(f);
        }
        var featureCollection = FeatureCollection.fromFeatures(features);

        try (var visGraph = new FileWriter("vis-graph-.geojson")) {
            visGraph.write(featureCollection.toJson());
        } catch (IOException e) {
            System.err.println();
        }
    }

    @Test
    public void make() {
        while (size <= 200) {
            makeGeneratedGraph();
            makeOwnGraph();
            size += 1;
        }
        System.out.println("JGraphT: " + g1);
        System.out.println("Custom: " + g2);
        // Using 'break' in line 207 of Graph made things significantly faster.
    }


    @Test
    public void drawGraphs() {
        makeGeneratedGraph();
        makeOwnGraph();
        testVisibilityGraph();
        testGeneratedVisibilityGraph();
    }
}