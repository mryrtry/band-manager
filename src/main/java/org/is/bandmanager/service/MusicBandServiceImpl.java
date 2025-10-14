package org.is.bandmanager.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.MusicBandDto;
import org.is.bandmanager.dto.MusicBandMapper;
import org.is.bandmanager.dto.request.MusicBandRequest;
import org.is.bandmanager.exception.ServiceException;
import org.is.bandmanager.model.MusicBand;
import org.is.bandmanager.repository.MusicBandRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Date;
import java.util.List;

import static org.is.bandmanager.exception.message.ServiceErrorMessage.*;

@Service
@Validated
@RequiredArgsConstructor
public class MusicBandServiceImpl implements MusicBandService {

    private final MusicBandRepository musicBandRepository;
    private final AlbumService albumService;
    private final CoordinatesService coordinatesService;
    private final PersonService personService;
    private final BestBandAwardService bestBandAwardService;
    private final MusicBandMapper mapper;

    private MusicBand findById(Integer id) {
        if (id == null) {
            throw new ServiceException(MUST_BE_NOT_NULL, "MusicBand.id");
        }
        return musicBandRepository.findById(id)
                .orElseThrow(() -> new ServiceException(SOURCE_NOT_FOUND, "MusicBand", id));
    }

    private void handleDependencies(MusicBand musicBand) {
        Long coordinatesId = musicBand.getCoordinates().getId();
        if (musicBandRepository.countByCoordinatesId(coordinatesId) <= 1) {
            coordinatesService.delete(coordinatesId);
        }

        Long frontManId = musicBand.getFrontMan().getId();
        if (musicBandRepository.countByFrontManId(frontManId) <= 1) {
            personService.delete(frontManId);
        }

        Long bestAlbumId = musicBand.getBestAlbum().getId();
        if (musicBandRepository.countByBestAlbumId(bestAlbumId) <= 1) {
            albumService.delete(bestAlbumId);
        }

        bestBandAwardService.deleteAllByBandId(musicBand.getId());
    }

    @Override
    @Transactional
    public MusicBandDto create(MusicBandRequest request) {
        MusicBand musicBand = musicBandRepository.save(mapper.toEntity(request));
        return mapper.toDto(musicBand);
    }

    @Override
    public List<MusicBandDto> getAll() {
        return musicBandRepository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public MusicBandDto get(Integer id) {
        return mapper.toDto(findById(id));
    }

    @Override
    public MusicBand getEntity(Integer id) {
        return findById(id);
    }

    @Override
    public MusicBandDto getWithMaxCoordinates() {
        MusicBand musicBand = musicBandRepository.findBandWithMaxCoordinates().orElseThrow(() -> new ServiceException(SOURCE_NOT_FOUND, "MusicBand.MaxCoordinates"));
        return mapper.toDto(musicBand);
    }

    @Override
    public List<MusicBandDto> getByEstablishmentDateBefore(Date date) {
        return musicBandRepository.findByEstablishmentDateBefore(date).stream().map(mapper::toDto).toList();
    }

    @Override
    public List<Long> getDistinctAlbumsCount() {
        return musicBandRepository.findDistinctAlbumsCount();
    }

    @Override
    @Transactional
    public MusicBandDto update(Integer id, MusicBandRequest request) {
        findById(id);
        MusicBand updatedMusicBand = mapper.toEntity(request);
        updatedMusicBand.setId(id);
        return mapper.toDto(musicBandRepository.save(updatedMusicBand));
    }

    @Override
    @Transactional
    public MusicBandDto delete(Integer id) {
        MusicBand musicBand = findById(id);
        musicBandRepository.delete(musicBand);
        handleDependencies(musicBand);
        return mapper.toDto(musicBand);
    }

    @Override
    public MusicBandDto removeParticipant(Integer id) {
        MusicBand musicBand = findById(id);
        if (musicBand.getNumberOfParticipants() <= 1) {
            throw new ServiceException(CANNOT_REMOVE_LAST_PARTICIPANT);
        }
        musicBand.setNumberOfParticipants(musicBand.getNumberOfParticipants() - 1);
        return mapper.toDto(musicBandRepository.save(musicBand));
    }

}
