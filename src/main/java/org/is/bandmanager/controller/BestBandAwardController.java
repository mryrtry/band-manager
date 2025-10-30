package org.is.bandmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.BestBandAwardDto;
import org.is.bandmanager.dto.request.BestBandAwardRequest;
import org.is.bandmanager.dto.request.BestBandAwardFilter;
import org.is.bandmanager.service.BestBandAwardService;
import org.is.bandmanager.service.pageable.PageableConfig;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/best-band-awards")
@RequiredArgsConstructor
public class BestBandAwardController {

    private final BestBandAwardService bestBandAwardService;

    @GetMapping()
    public ResponseEntity<Page<BestBandAwardDto>> getAllBestBandAwardsFiltered(
            @ModelAttribute BestBandAwardFilter filter,
            @ModelAttribute PageableConfig config) {
        Page<BestBandAwardDto> awards = bestBandAwardService.getAll(filter, config);
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
