package org.is.bandmanager.service.bestBandAward;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.BestBandAwardDto;
import org.is.bandmanager.dto.BestBandAwardMapper;
import org.is.bandmanager.dto.request.BestBandAwardRequest;
import org.is.bandmanager.model.BestBandAward;
import org.is.bandmanager.repository.BestBandAwardRepository;
import org.is.bandmanager.repository.filter.BestBandAwardFilter;
import org.is.bandmanager.service.musicBand.MusicBandService;
import org.is.event.EntityEvent;
import org.is.exception.ServiceException;
import org.is.util.pageable.PageableFactory;
import org.is.util.pageable.PageableRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static org.is.bandmanager.exception.message.BandManagerErrorMessage.ID_MUST_BE_POSITIVE;
import static org.is.bandmanager.exception.message.BandManagerErrorMessage.MUST_BE_NOT_NULL;
import static org.is.bandmanager.exception.message.BandManagerErrorMessage.SOURCE_WITH_ID_NOT_FOUND;
import static org.is.event.EventType.BULK_DELETED;
import static org.is.event.EventType.CREATED;
import static org.is.event.EventType.UPDATED;


@Service
@Validated
@RequiredArgsConstructor
public class BestBandAwardServiceImpl implements BestBandAwardService {

    private final BestBandAwardRepository bestBandAwardRepository;

    private final MusicBandService musicBandService;

    private final ApplicationEventPublisher eventPublisher;

    private final BestBandAwardMapper mapper;

    private final PageableFactory pageableFactory;

    private BestBandAward findById(Long id) {
        if (id == null) {
            throw new ServiceException(MUST_BE_NOT_NULL, "BestBandAward.id");
        }
        if (id <= 0) {
            throw new ServiceException(ID_MUST_BE_POSITIVE, "BestBandAward.id");
        }
        return bestBandAwardRepository.findById(id).orElseThrow(() -> new ServiceException(SOURCE_WITH_ID_NOT_FOUND, "BestBandAward", id));
    }

    private void handleDependencies(BestBandAwardRequest request, BestBandAward entity) {
        entity.setBand(musicBandService.getEntity(request.getMusicBandId()));
    }

    @Override
    @Transactional
    public BestBandAwardDto create(BestBandAwardRequest request) {
        BestBandAward bestBandAward = mapper.toEntity(request);
        handleDependencies(request, bestBandAward);
        BestBandAwardDto createdAward = mapper.toDto(bestBandAwardRepository.save(bestBandAward));
        eventPublisher.publishEvent(new EntityEvent<>(CREATED, createdAward));
        return createdAward;
    }

    @Override
    public Page<BestBandAwardDto> getAll(BestBandAwardFilter filter, PageableRequest config) {
        Pageable pageable = pageableFactory.create(config, BestBandAward.class);
        Page<BestBandAward> awards = bestBandAwardRepository.findWithFilter(filter, pageable);
        return awards.map(mapper::toDto);
    }

    @Override
    public BestBandAwardDto get(Long id) {
        return mapper.toDto(findById(id));
    }

    @Override
    @Transactional
    public BestBandAwardDto update(Long id, BestBandAwardRequest request) {
        BestBandAward updatingAward = findById(id);
        mapper.updateEntityFromRequest(request, updatingAward);
        handleDependencies(request, updatingAward);
        BestBandAwardDto updatedAward = mapper.toDto(bestBandAwardRepository.save(updatingAward));
        eventPublisher.publishEvent(new EntityEvent<>(UPDATED, updatedAward));
        return updatedAward;
    }

    @Override
    @Transactional
    public BestBandAwardDto delete(Long id) {
        BestBandAward bestBandAward = findById(id);
        bestBandAwardRepository.delete(bestBandAward);
        return mapper.toDto(bestBandAward);
    }

    @Override
    @Transactional
    public List<BestBandAwardDto> delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<BestBandAward> awards = bestBandAwardRepository.findAllById(ids);
        bestBandAwardRepository.deleteAll(awards);
        List<BestBandAwardDto> deletedAwards = awards.stream().map(mapper::toDto).toList();
        eventPublisher.publishEvent(new EntityEvent<>(BULK_DELETED, deletedAwards));
        return deletedAwards;
    }

}
