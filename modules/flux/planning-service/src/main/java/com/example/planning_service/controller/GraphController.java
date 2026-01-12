package com.example.planning_service.controller;

import com.example.planning_service.node.LocationNode;
import com.example.planning_service.node.TransportRelation;
import com.example.planning_service.repository.graph.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planning/graph")
@RequiredArgsConstructor
public class GraphController {

    private final LocationRepository locationRepository;
    private final com.example.planning_service.repository.graph.GraphDriverRepository driverRepository;
    private final com.example.planning_service.repository.graph.GraphRouteRepository routeRepository;

    @PostMapping("/init")
    public ResponseEntity<String> initGraph() {
        locationRepository.deleteAll();
        driverRepository.deleteAll();
        routeRepository.deleteAll();

        LocationNode warsaw = new LocationNode("WAW", "Warsaw Hub", "HUB");
        LocationNode wroclaw = new LocationNode("WRO", "Wroclaw Spoke", "SPOKE");
        LocationNode gdansk = new LocationNode("GDN", "Gdansk Spoke", "SPOKE");

        // Relations
        TransportRelation wawToWro = new TransportRelation();
        wawToWro.setTargetLocation(wroclaw);
        wawToWro.setDistance(350.0);
        warsaw.getConnections().add(wawToWro);

        TransportRelation wawToGdn = new TransportRelation();
        wawToGdn.setTargetLocation(gdansk);
        wawToGdn.setDistance(340.0);
        warsaw.getConnections().add(wawToGdn);

        locationRepository.save(warsaw);

        return ResponseEntity.ok("Graph Initialized with Warsaw, Wroclaw, Gdansk");
    }

    @GetMapping("/shortest-path")
    public ResponseEntity<List<LocationNode>> getShortestPath(@RequestParam String start, @RequestParam String end) {
        return ResponseEntity.ok(locationRepository.findShortestPath(start, end));
    }

    // --- NEW: Void-Mesh Expansion ---

    @PostMapping("/drivers")
    public ResponseEntity<com.example.planning_service.node.DriverNode> createDriver(
            @RequestBody com.example.planning_service.node.DriverNode driver) {
        return ResponseEntity.ok(driverRepository.save(driver));
    }

    @PostMapping("/routes")
    public ResponseEntity<com.example.planning_service.node.RouteNode> createRoute(
            @RequestBody com.example.planning_service.node.RouteNode route) {
        // In a real scenario, we would lookup Locations and link them.
        // For MVP, we assume the RequestBody contains the nested structure or we start
        // simple.
        // Simplified: Just save the route node, relationships managed via separate
        // updates or smart DTOs.
        return ResponseEntity.ok(routeRepository.save(route));
    }

    @PostMapping("/routes/{routeId}/assign-driver/{driverId}")
    public ResponseEntity<String> assignDriverToRoute(@PathVariable String routeId, @PathVariable String driverId) {
        var driverOpt = driverRepository.findById(driverId);
        var routeOpt = routeRepository.findById(routeId);

        if (driverOpt.isEmpty() || routeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var driver = driverOpt.get();
        var route = routeOpt.get();

        driver.getRoutes().add(route);
        driverRepository.save(driver);

        return ResponseEntity.ok("Driver " + driverId + " assigned to Route " + routeId);
    }

    @PostMapping("/routes/{routeId}/add-stop")
    public ResponseEntity<String> addStopToRoute(@PathVariable String routeId, @RequestParam String locationId,
            @RequestParam int index) {
        var routeOpt = routeRepository.findById(routeId);
        var locationOpt = locationRepository.findById(locationId);

        if (routeOpt.isEmpty() || locationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var route = routeOpt.get();
        var location = locationOpt.get();

        var servesRelation = new com.example.planning_service.node.ServesRelation(location, index);
        route.getServedLocations().add(servesRelation);
        routeRepository.save(route);

        return ResponseEntity.ok("Location " + locationId + " added to Route " + routeId);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<com.example.planning_service.node.DriverNode> getDriver(@PathVariable String driverId) {
        return driverRepository.findById(driverId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/drivers")
    public ResponseEntity<List<com.example.planning_service.node.DriverNode>> getAllDrivers() {
        return ResponseEntity.ok(driverRepository.findAll());
    }

    @GetMapping("/driver/{driverId}/impact")
    public ResponseEntity<List<com.example.planning_service.node.LocationNode>> getLocationsServedByDriver(
            @PathVariable String driverId) {
        // This requires a custom query in repository, or we can fetch driver and
        // traverse in Java (less efficient but works for MVP)
        // For MVP: Fetch driver, get routes, get locations.
        var driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isEmpty())
            return ResponseEntity.notFound().build();

        var locations = driverOpt.get().getRoutes().stream()
                .flatMap(r -> r.getServedLocations().stream())
                .map(com.example.planning_service.node.ServesRelation::getTargetLocation)
                .toList();

        return ResponseEntity.ok(locations);
    }
}
