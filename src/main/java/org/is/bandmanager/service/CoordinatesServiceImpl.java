package org.is.bandmanager.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.CoordinatesDto;
import org.is.bandmanager.dto.CoordinatesMapper;
import org.is.bandmanager.dto.request.CoordinatesRequest;
import org.is.bandmanager.exception.ServiceException;
import org.is.bandmanager.model.Coordinates;
import org.is.bandmanager.repository.CoordinatesRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static org.is.bandmanager.exception.message.ServiceErrorMessage.MUST_BE_NOT_NULL;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.SOURCE_NOT_FOUND;

@Service
@Validated
@RequiredArgsConstructor
public class CoordinatesServiceImpl implements CoordinatesService {

    private final CoordinatesRepository coordinatesRepository;
    private final CoordinatesMapper mapper;

    private Coordinates findById(Long id) {
        if (id == null) {
            throw new ServiceException(MUST_BE_NOT_NULL, "Coordinates.id");
        }
        return coordinatesRepository.findById(id)
                .orElseThrow(() -> new ServiceException(SOURCE_NOT_FOUND, "Coordinates", id));
    }

    @Override
    @Transactional
    public CoordinatesDto create(CoordinatesRequest request) {
        Coordinates coordinates = coordinatesRepository.save(mapper.toEntity(request));
        return mapper.toDto(coordinates);
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
    @Transactional
    public CoordinatesDto update(Long id, CoordinatesRequest request) {
        findById(id);
        Coordinates updatedCoordinates = mapper.toEntity(request);
        updatedCoordinates.setId(id);
        return mapper.toDto(coordinatesRepository.save(updatedCoordinates));
    }

    @Override
    @Transactional
    public CoordinatesDto delete(Long id) {
        Coordinates coordinates = findById(id);
        coordinatesRepository.delete(coordinates);
        return mapper.toDto(coordinates);
    }

}
