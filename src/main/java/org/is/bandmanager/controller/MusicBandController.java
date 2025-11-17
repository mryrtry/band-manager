package org.is.bandmanager.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.MusicBandDto;
import org.is.bandmanager.dto.request.MusicBandRequest;
import org.is.bandmanager.repository.filter.MusicBandFilter;
import org.is.bandmanager.service.musicBand.MusicBandService;
import org.is.util.pageable.PageableRequest;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/music-bands")
@RequiredArgsConstructor
public class MusicBandController {

    private final MusicBandService musicBandService;

    @GetMapping("/all")
    @PreAuthorize("@securityService.canReadEntity()")
    public ResponseEntity<List<MusicBandDto>> getAllMusicBands() {
        List<MusicBandDto> bands = musicBandService.getAll();
        return ResponseEntity.ok(bands);
    }

    @GetMapping()
    @PreAuthorize("@securityService.canReadEntity()")
    public ResponseEntity<Page<MusicBandDto>> getAllMusicBands(
            @ModelAttribute @Valid MusicBandFilter filter,
            @ModelAttribute PageableRequest config) {
        Page<MusicBandDto> bands = musicBandService.getAll(filter, config);
        return ResponseEntity.ok(bands);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.canReadEntity()")
    public ResponseEntity<MusicBandDto> getMusicBand(@PathVariable Long id) {
        MusicBandDto musicBand = musicBandService.get(id);
        return ResponseEntity.ok(musicBand);
    }

    @GetMapping("/max-coordinates")
    @PreAuthorize("@securityService.canReadEntity()")
    public ResponseEntity<MusicBandDto> getMaxCoordinates() {
        MusicBandDto musicBand = musicBandService.getWithMaxCoordinates();
        return ResponseEntity.ok(musicBand);
    }

    @GetMapping("/established-before")
    @PreAuthorize("@securityService.canReadEntity()")
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
    @PreAuthorize("@securityService.canReadEntity()")
    public ResponseEntity<List<Long>> getUniqueAlbumsCount() {
        List<Long> uniqueCounts = musicBandService.getDistinctAlbumsCount();
        return ResponseEntity.ok(uniqueCounts);
    }

    @PostMapping
    @PreAuthorize("@securityService.canCreate()")
    public ResponseEntity<MusicBandDto> createMusicBand(@Valid @RequestBody MusicBandRequest request) {
        MusicBandDto createdMusicBand = musicBandService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMusicBand);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.canUpdateEntity(#id, 'MusicBand')")
    public ResponseEntity<MusicBandDto> updateMusicBand(
            @PathVariable Long id,
            @Valid @RequestBody MusicBandRequest request) {
        MusicBandDto updatedMusicBand = musicBandService.update(id, request);
        return ResponseEntity.ok(updatedMusicBand);
    }

    @PutMapping("/{id}/remove-participant")
    @PreAuthorize("@securityService.canUpdateEntity(#id, 'MusicBand')")
    public ResponseEntity<MusicBandDto> removeParticipant(@PathVariable Long id) {
        MusicBandDto updatedBand = musicBandService.removeParticipant(id);
        return ResponseEntity.ok(updatedBand);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.canDeleteEntity(#id, 'MusicBand')")
    public ResponseEntity<MusicBandDto> deleteMusicBand(@PathVariable Long id) {
        MusicBandDto deletedMusicBand = musicBandService.delete(id);
        return ResponseEntity.ok(deletedMusicBand);
    }

    @DeleteMapping()
    @PreAuthorize("@securityService.canBulkDelete('MusicBand')")
    public ResponseEntity<List<MusicBandDto>> deleteMusicBands(@RequestBody List<Long> ids) {
        List<MusicBandDto> deletedMusicBands = musicBandService.delete(ids);
        return ResponseEntity.ok(deletedMusicBands);
    }

}