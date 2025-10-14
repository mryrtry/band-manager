package org.is.bandmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.BestBandAwardDto;
import org.is.bandmanager.dto.request.BestBandAwardRequest;
import org.is.bandmanager.model.MusicGenre;
import org.is.bandmanager.service.BestBandAwardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/best-band-awards")
@RequiredArgsConstructor
public class BestBandAwardController {

    private final BestBandAwardService bestBandAwardService;

    @GetMapping()
    public ResponseEntity<Page<BestBandAwardDto>> getAllBestBandAwardsFiltered(
            @RequestParam(required = false) MusicGenre genre,
            @RequestParam(required = false) String bandName,
            @RequestParam(required = false) Integer bandId,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Sort pageSort = Sort.by(sortDirection, sort);

        Pageable pageable = PageRequest.of(page, size, pageSort);

        Page<BestBandAwardDto> awards = bestBandAwardService.getAll(
                genre, bandName, bandId, pageable
        );
        return ResponseEntity.ok(awards);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BestBandAwardDto> getBestBandAward(@PathVariable Long id) {
        BestBandAwardDto bestBandAward = bestBandAwardService.get(id);
        return ResponseEntity.ok(bestBandAward);
    }

    @PostMapping
    public ResponseEntity<BestBandAwardDto> createBestBandAward(@Valid @RequestBody BestBandAwardRequest request) {
        BestBandAwardDto createdBestBandAward = bestBandAwardService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBestBandAward);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BestBandAwardDto> updateBestBandAward(
            @PathVariable Long id,
            @Valid @RequestBody BestBandAwardRequest request) {
        BestBandAwardDto updatedBestBandAward = bestBandAwardService.update(id, request);
        return ResponseEntity.ok(updatedBestBandAward);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BestBandAwardDto> deleteBestBandAward(@PathVariable Long id) {
        BestBandAwardDto deletedBestBandAward = bestBandAwardService.delete(id);
        return ResponseEntity.ok(deletedBestBandAward);
    }

}
