package org.is.bandmanager.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.MusicBandDto;
import org.is.bandmanager.dto.request.MusicBandRequest;
import org.is.bandmanager.service.MusicBandService;
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

    @GetMapping
    public ResponseEntity<List<MusicBandDto>> getAllMusicBands() {
        return ResponseEntity.ok(musicBandService.getAll());
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

    @DeleteMapping("/{id}")
    public ResponseEntity<MusicBandDto> deleteMusicBand(@PathVariable Integer id) {
        MusicBandDto deletedMusicBand = musicBandService.delete(id);
        return ResponseEntity.ok(deletedMusicBand);
    }

}
