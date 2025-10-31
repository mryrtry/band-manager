package org.is.bandmanager.service.musicBand;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.MusicBandDto;
import org.is.bandmanager.dto.MusicBandMapper;
import org.is.bandmanager.repository.filter.MusicBandFilter;
import org.is.bandmanager.dto.request.MusicBandRequest;
import org.is.bandmanager.event.EntityEvent;
import org.is.bandmanager.exception.ServiceException;
import org.is.bandmanager.model.MusicBand;
import org.is.bandmanager.repository.BestBandAwardRepository;
import org.is.bandmanager.repository.MusicBandRepository;
import org.is.bandmanager.service.person.PersonService;
import org.is.bandmanager.service.album.AlbumService;
import org.is.bandmanager.service.coordinates.CoordinatesService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.is.bandmanager.util.pageable.PageableConfig;
import org.is.bandmanager.util.pageable.PageableCreator;
import org.is.bandmanager.util.pageable.PageableType;

import java.util.Date;
import java.util.List;

import static org.is.bandmanager.event.EventType.BULK_DELETED;
import static org.is.bandmanager.event.EventType.CREATED;
import static org.is.bandmanager.event.EventType.DELETED;
import static org.is.bandmanager.event.EventType.UPDATED;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.CANNOT_REMOVE_LAST_PARTICIPANT;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.ID_MUST_BE_POSITIVE;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.MUST_BE_NOT_NULL;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.SOURCE_NOT_FOUND;


@Service
@Validated
@RequiredArgsConstructor
public class MusicBandServiceImpl implements MusicBandService {

    private final MusicBandRepository musicBandRepository;

    private final BestBandAwardRepository bestBandAwardRepository;

    private final PersonService personService;

    private final AlbumService albumService;

    private final CoordinatesService coordinatesService;

    private final ApplicationEventPublisher eventPublisher;

    private final MusicBandMapper mapper;

    private MusicBand findById(Integer id) {
        if (id == null) {
            throw new ServiceException(MUST_BE_NOT_NULL, "MusicBand.id");
        }
        if (id <= 0) {
            throw new ServiceException(ID_MUST_BE_POSITIVE, "MusicBand.id");
        }
        return musicBandRepository.findById(id).orElseThrow(() -> new ServiceException(SOURCE_NOT_FOUND, "MusicBand", id));
    }

    @Transactional
    protected void handleDependencies(MusicBandRequest request, MusicBand entity) {
        entity.setFrontMan(personService.getEntity(request.getFrontManId()));
        entity.setBestAlbum(albumService.getEntity(request.getBestAlbumId()));
        entity.setCoordinates(coordinatesService.getEntity(request.getCoordinatesId()));
    }

    @Transactional
    protected void checkDependencies(List<MusicBand> musicBands) {
        List<Integer> bandIds = musicBands.stream().map(MusicBand::getId).toList();
        bestBandAwardRepository.deleteAllByBandIdIn(bandIds);
    }

    @Override
    @Transactional
    public MusicBandDto create(MusicBandRequest request) {
        MusicBand musicBand = mapper.toEntity(request);
        handleDependencies(request, musicBand);
        MusicBandDto createdMusicBand = mapper.toDto(musicBandRepository.save(musicBand));
        eventPublisher.publishEvent(new EntityEvent<>(CREATED, createdMusicBand));
        return createdMusicBand;
    }

    @Override
    public Page<MusicBandDto> getAll(MusicBandFilter filter, PageableConfig config) {
        Pageable pageable = PageableCreator.create(config, PageableType.MUSIC_BANDS);
        Page<MusicBand> bands = musicBandRepository.findWithFilter(filter, pageable);
        return bands.map(mapper::toDto);
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
        MusicBand band = musicBandRepository.findBandWithMaxCoordinates().orElseThrow(() -> new ServiceException(SOURCE_NOT_FOUND, "MusicBand.MaxCoordinates"));
        return mapper.toDto(band);
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
        MusicBand updatingBand = findById(id);
        mapper.updateEntityFromRequest(request, updatingBand);
        handleDependencies(request, updatingBand);
        MusicBandDto updatedBand = mapper.toDto(musicBandRepository.save(updatingBand));
        eventPublisher.publishEvent(new EntityEvent<>(UPDATED, updatedBand));
        return updatedBand;
    }

    @Override
    @Transactional
    public MusicBandDto delete(Integer id) {
        MusicBand musicBand = findById(id);
        checkDependencies(List.of(musicBand));
        musicBandRepository.deleteById(id);
        MusicBandDto deletedBand = mapper.toDto(musicBand);
        eventPublisher.publishEvent(new EntityEvent<>(DELETED, deletedBand));
        return deletedBand;
    }

    @Override
    @Transactional
    public List<MusicBandDto> delete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<MusicBand> bands = musicBandRepository.findAllById(ids);
        checkDependencies(bands);
        musicBandRepository.deleteAll(bands);
        List<MusicBandDto> deletedBands = bands.stream().map(mapper::toDto).toList();
        eventPublisher.publishEvent(new EntityEvent<>(BULK_DELETED, deletedBands));
        return deletedBands;
    }

    @Override
    @Transactional
    public MusicBandDto removeParticipant(Integer id) {
        MusicBand musicBand = findById(id);
        if (musicBand.getNumberOfParticipants() <= 1) {
            throw new ServiceException(CANNOT_REMOVE_LAST_PARTICIPANT);
        }
        musicBand.setNumberOfParticipants(musicBand.getNumberOfParticipants() - 1);
        MusicBandDto updatedBand = mapper.toDto(musicBandRepository.save(musicBand));
        eventPublisher.publishEvent(new EntityEvent<>(UPDATED, updatedBand));
        return updatedBand;
    }

}
