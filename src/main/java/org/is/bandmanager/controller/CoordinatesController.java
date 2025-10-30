package org.is.bandmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.CoordinatesDto;
import org.is.bandmanager.dto.request.CoordinatesRequest;
import org.is.bandmanager.service.CoordinatesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/coordinates")
@RequiredArgsConstructor
public class CoordinatesController {

    private final CoordinatesService coordinatesService;

    @GetMapping
    public ResponseEntity<List<CoordinatesDto>> getAllCoordinates() {
        return ResponseEntity.ok(coordinatesService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoordinatesDto> getCoordinates(@PathVariable Long id) {
        CoordinatesDto coordinates = coordinatesService.get(id);
        return ResponseEntity.ok(coordinates);
    }

    @PostMapping
    public ResponseEntity<CoordinatesDto> createCoordinates(@Valid @RequestBody CoordinatesRequest request) {
        CoordinatesDto createdCoordinates = coordinatesService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCoordinates);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CoordinatesDto> updateCoordinates(@PathVariable Long id, @Valid @RequestBody CoordinatesRequest request) {
        CoordinatesDto updatedCoordinates = coordinatesService.update(id, request);
        return ResponseEntity.ok(updatedCoordinates);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CoordinatesDto> deleteCoordinates(@PathVariable Long id) {
        CoordinatesDto deletedCoordinates = coordinatesService.delete(id);
        return ResponseEntity.ok(deletedCoordinates);
    }

}
