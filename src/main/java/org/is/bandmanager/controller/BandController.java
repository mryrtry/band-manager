package org.is.bandmanager.controller;

import lombok.RequiredArgsConstructor;
import org.is.bandmanager.model.MusicBand;
import org.is.bandmanager.service.BandService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BandController {

    private final BandService bandService;

    @GetMapping
    public List<MusicBand> getBands() {
        return bandService.getBands();
    }

}
