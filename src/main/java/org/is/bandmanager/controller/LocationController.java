package org.is.bandmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.model.dto.LocationDto;
import org.is.bandmanager.model.request.LocationRequest;
import org.is.bandmanager.service.LocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<List<LocationDto>> getAllLocations() {
        return ResponseEntity.ok(locationService.getLocations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getLocation(@PathVariable Long id) {
        LocationDto location = locationService.getLocation(id);
        return ResponseEntity.ok(location);
    }

    @PostMapping
    public ResponseEntity<LocationDto> createLocation(@Valid @RequestBody LocationRequest request) {
        LocationDto createdLocation = locationService.createLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLocation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationDto> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody LocationRequest request) {
        LocationDto updatedLocation = locationService.updateLocation(id, request);
        return ResponseEntity.ok(updatedLocation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<LocationDto> deleteLocation(@PathVariable Long id) {
        LocationDto deletedLocation = locationService.deleteLocation(id);
        return ResponseEntity.ok(deletedLocation);
    }

}