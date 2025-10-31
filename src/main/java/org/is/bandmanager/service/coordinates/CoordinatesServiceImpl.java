package org.is.bandmanager.service.coordinates;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.CoordinatesDto;
import org.is.bandmanager.dto.CoordinatesMapper;
import org.is.bandmanager.dto.request.CoordinatesRequest;
import org.is.bandmanager.event.EntityEvent;
import org.is.exception.ServiceException;
import org.is.bandmanager.model.Coordinates;
import org.is.bandmanager.repository.CoordinatesRepository;
import org.is.bandmanager.repository.MusicBandRepository;
import org.is.bandmanager.service.cleanup.CleanupStrategy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static org.is.bandmanager.event.EventType.CREATED;
import static org.is.bandmanager.event.EventType.DELETED;
import static org.is.bandmanager.event.EventType.UPDATED;
import static org.is.exception.message.BandManagerErrorMessage.ENTITY_IN_USE;
import static org.is.exception.message.BandManagerErrorMessage.ID_MUST_BE_POSITIVE;
import static org.is.exception.message.BandManagerErrorMessage.MUST_BE_NOT_NULL;
import static org.is.exception.message.BandManagerErrorMessage.SOURCE_NOT_FOUND;


@Service
@Validated
@RequiredArgsConstructor
public class CoordinatesServiceImpl implements CoordinatesService, CleanupStrategy<Coordinates, CoordinatesDto> {

    private final CoordinatesRepository coordinatesRepository;

    private final MusicBandRepository musicBandRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final CoordinatesMapper mapper;

    private Coordinates findById(Long id) {
        if (id == null) {
            throw new ServiceException(MUST_BE_NOT_NULL, "Coordinates.id");
        }
        if (id <= 0) {
            throw new ServiceException(ID_MUST_BE_POSITIVE, "Coordinates.id");
        }
        return coordinatesRepository.findById(id).orElseThrow(() -> new ServiceException(SOURCE_NOT_FOUND, "Coordinates", id));
    }

    @Transactional
    protected void checkDependencies(Coordinates coordinates) {
        Long coordinatesId = coordinates.getId();
        if (musicBandRepository.existsByCoordinatesId(coordinatesId)) {
            throw new ServiceException(ENTITY_IN_USE, "Coordinates", coordinatesId, "MusicBand");
        }
    }

    @Override
    @Transactional
    public CoordinatesDto create(CoordinatesRequest request) {
        Coordinates coordinates = mapper.toEntity(request);
        CoordinatesDto createdCoordinates = mapper.toDto(coordinatesRepository.save(coordinates));
        eventPublisher.publishEvent(new EntityEvent<>(CREATED, createdCoordinates));
        return createdCoordinates;
    }

    @Override
    public List<CoordinatesDto> getAll() {
        return coordinatesRepository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public CoordinatesDto get(Long id) {
        return mapper.toDto(findById(id));
    }

    @Override
    public Coordinates getEntity(Long id) {
        return findById(id);
    }

    @Override
    @Transactional
    public CoordinatesDto update(Long id, CoordinatesRequest request) {
        Coordinates updatingCoordinates = findById(id);
        mapper.updateEntityFromRequest(request, updatingCoordinates);
        CoordinatesDto updatedCoordinates = mapper.toDto(coordinatesRepository.save(updatingCoordinates));
        eventPublisher.publishEvent(new EntityEvent<>(UPDATED, updatedCoordinates));
        return updatedCoordinates;
    }

    @Override
    @Transactional
    public CoordinatesDto delete(Long id) {
        Coordinates coordinates = findById(id);
        checkDependencies(coordinates);
        coordinatesRepository.delete(coordinates);
        CoordinatesDto deletedCoordinates = mapper.toDto(coordinates);
        eventPublisher.publishEvent(new EntityEvent<>(DELETED, deletedCoordinates));
        return deletedCoordinates;
    }

    @Override
    public List<Coordinates> findUnusedEntities() {
        return coordinatesRepository.findUnusedCoordinates();
    }

    @Override
    public void deleteEntities(List<Coordinates> entities) {
        coordinatesRepository.deleteAll(entities);
    }

    @Override
    public List<CoordinatesDto> convertToDto(List<Coordinates> entities) {
        return entities.stream().map(mapper::toDto).toList();
    }

    @Override
    public String getEntityName() {
        return "Coordinates";
    }

}
