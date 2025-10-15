package org.is.bandmanager.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.MusicBandDto;
import org.is.bandmanager.dto.request.MusicBandRequest;
import org.is.bandmanager.model.MusicGenre;
import org.is.bandmanager.service.MusicBandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/music-bands")
@RequiredArgsConstructor
public class MusicBandController {

    private final MusicBandService musicBandService;

    @GetMapping()
    public ResponseEntity<Page<MusicBandDto>> getAllMusicBands(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MusicGenre genre,
            @RequestParam(required = false) String frontManName,
            @RequestParam(required = false) String bestAlbumName,

            @RequestParam(required = false) Long minParticipants,
            @RequestParam(required = false) Long maxParticipants,
            @RequestParam(required = false) Long minSingles,
            @RequestParam(required = false) Long maxSingles,
            @RequestParam(required = false) Long minAlbumsCount,
            @RequestParam(required = false) Long maxAlbumsCount,
            @RequestParam(required = false) Integer minCoordinateX,
            @RequestParam(required = false) Integer maxCoordinateX,
            @RequestParam(required = false) Float minCoordinateY,
            @RequestParam(required = false) Float maxCoordinateY,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Sort pageSort = Sort.by(sortDirection, sort);

        Pageable pageable = PageRequest.of(page, size, pageSort);

        Page<MusicBandDto> bands = musicBandService.getAll(
                name, description, genre, frontManName, bestAlbumName,
                minParticipants, maxParticipants, minSingles, maxSingles,
                minAlbumsCount, maxAlbumsCount, minCoordinateX, maxCoordinateX,
                minCoordinateY, maxCoordinateY, pageable
        );

        return ResponseEntity.ok(bands);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MusicBandDto> getMusicBand(@PathVariable Integer id) {
        MusicBandDto musicBand = musicBandService.get(id);
        return ResponseEntity.ok(musicBand);
    }

    @GetMapping("/max-coordinates")
    public ResponseEntity<MusicBandDto> getMaxCoordinates() {
        MusicBandDto musicBand = musicBandService.getWithMaxCoordinates();
        return ResponseEntity.ok(musicBand);
    }

    @GetMapping("/established-before")
    public ResponseEntity<List<MusicBandDto>> getBandsEstablishedBefore(
            @RequestParam
            @NotNull(message = "Date parameter is required")
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            @PastOrPresent(message = "Date cannot be in the future")
            Date date) {

        List<MusicBandDto> bands = musicBandService.getByEstablishmentDateBefore(date);
        return ResponseEntity.ok(bands);
    }

    @GetMapping("/unique-albums-count")
    public ResponseEntity<List<Long>> getUniqueAlbumsCount() {
        List<Long> uniqueCounts = musicBandService.getDistinctAlbumsCount();
        return ResponseEntity.ok(uniqueCounts);
    }

    @PostMapping
    public ResponseEntity<MusicBandDto> createMusicBand(@Valid @RequestBody MusicBandRequest request) {
        MusicBandDto createdMusicBand = musicBandService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMusicBand);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MusicBandDto> updateMusicBand(
            @PathVariable Integer id,
            @Valid @RequestBody MusicBandRequest request) {
        MusicBandDto updatedMusicBand = musicBandService.update(id, request);
        return ResponseEntity.ok(updatedMusicBand);
    }

    @PutMapping("/{id}/remove-participant")
    public ResponseEntity<MusicBandDto> removeParticipant(@PathVariable Integer id) {
        MusicBandDto updatedBand = musicBandService.removeParticipant(id);
        return ResponseEntity.ok(updatedBand);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MusicBandDto> deleteMusicBand(@PathVariable Integer id) {
        MusicBandDto deletedMusicBand = musicBandService.delete(id);
        return ResponseEntity.ok(deletedMusicBand);
    }

    @DeleteMapping()
    public ResponseEntity<List<MusicBandDto>> deleteMusicBands(@RequestBody List<Integer> ids) {
        List<MusicBandDto> deletedMusicBands = musicBandService.delete(ids);
        return ResponseEntity.ok(deletedMusicBands);
    }

}
