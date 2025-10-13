package org.is.bandmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.BestBandAwardDto;
import org.is.bandmanager.dto.request.BestBandAwardRequest;
import org.is.bandmanager.service.BestBandAwardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/best-band-awards")
@RequiredArgsConstructor
public class BestBandAwardController {

    private final BestBandAwardService BestBandAwardService;

    @GetMapping
    public ResponseEntity<List<BestBandAwardDto>> getAllBestBandAwards() {
        return ResponseEntity.ok(BestBandAwardService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BestBandAwardDto> getBestBandAward(@PathVariable Long id) {
        BestBandAwardDto bestBandAward = BestBandAwardService.get(id);
        return ResponseEntity.ok(bestBandAward);
    }

    @PostMapping
    public ResponseEntity<BestBandAwardDto> createBestBandAward(@Valid @RequestBody BestBandAwardRequest request) {
        BestBandAwardDto createdBestBandAward = BestBandAwardService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBestBandAward);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BestBandAwardDto> updateBestBandAward(
            @PathVariable Long id,
            @Valid @RequestBody BestBandAwardRequest request) {
        BestBandAwardDto updatedBestBandAward = BestBandAwardService.update(id, request);
        return ResponseEntity.ok(updatedBestBandAward);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BestBandAwardDto> deleteBestBandAward(@PathVariable Long id) {
        BestBandAwardDto deletedBestBandAward = BestBandAwardService.delete(id);
        return ResponseEntity.ok(deletedBestBandAward);
    }

}
