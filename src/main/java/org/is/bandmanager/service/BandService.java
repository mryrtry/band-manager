package org.is.bandmanager.service;

import lombok.RequiredArgsConstructor;
import org.is.bandmanager.model.MusicBand;
import org.is.bandmanager.repo.MusicBandRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BandService {

    private final MusicBandRepository repository;

    public List<MusicBand> getBands() {
        return repository.findAll();
    }

}
