package org.is.bandmanager.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.BestBandAwardDto;
import org.is.bandmanager.dto.BestBandAwardMapper;
import org.is.bandmanager.dto.request.BestBandAwardRequest;
import org.is.bandmanager.exception.ServiceException;
import org.is.bandmanager.model.BestBandAward;
import org.is.bandmanager.model.MusicBand;
import org.is.bandmanager.repository.BestBandAwardRepository;
import org.is.bandmanager.repository.MusicBandRepository;
import org.is.bandmanager.repository.filter.BestBandAwardFilter;
import org.is.bandmanager.repository.specifications.BestBandAwardSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static org.is.bandmanager.exception.message.ServiceErrorMessage.MUST_BE_NOT_NULL;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.SOURCE_NOT_FOUND;

@Service
@Validated
@RequiredArgsConstructor
public class BestBandAwardServiceImpl implements BestBandAwardService {

    private final BestBandAwardRepository bestBandAwardRepository;
    private final MusicBandRepository musicBandRepository;
    private final BestBandAwardMapper mapper;

    private BestBandAward findById(Long id) {
        if (id == null) {
            throw new ServiceException(MUST_BE_NOT_NULL, "BestBandAward.id");
        }
        return bestBandAwardRepository.findById(id)
                .orElseThrow(() -> new ServiceException(SOURCE_NOT_FOUND, "BestBandAward", id));
    }

    private MusicBand fetchMusicBandById(Integer id) {
        if (id == null) {
            throw new ServiceException(MUST_BE_NOT_NULL, "MusicBand.id");
        }
        return musicBandRepository.findById(id)
                .orElseThrow(() -> new ServiceException(SOURCE_NOT_FOUND, "MusicBand", id));
    }

    @Override
    @Transactional
    public BestBandAwardDto create(BestBandAwardRequest request) {
        BestBandAward bestBandAward = mapper.toEntity(request);
        bestBandAward.setBand(fetchMusicBandById(request.getMusicBandId()));
        return mapper.toDto(bestBandAwardRepository.save(bestBandAward));
    }

    @Override
    public Page<BestBandAwardDto> getAll(BestBandAwardFilter filter, Pageable pageable) {
        Specification<BestBandAward> specification = BestBandAwardSpecifications.withFilter(filter);
        Page<BestBandAward> awards = bestBandAwardRepository.findAll(specification, pageable);
        return awards.map(mapper::toDto);
    }

    @Override
    public BestBandAwardDto get(Long id) {
        return mapper.toDto(findById(id));
    }

    @Override
    @Transactional
    public BestBandAwardDto update(Long id, BestBandAwardRequest request) {
        BestBandAward existingAward = findById(id);
        if (request.getMusicBandId() != null &&
                !request.getMusicBandId().equals(existingAward.getBand().getId())) {
            MusicBand newBand = fetchMusicBandById(request.getMusicBandId());
            existingAward.setBand(newBand);
        }
        existingAward.setGenre(request.getGenre());
        return mapper.toDto(bestBandAwardRepository.save(existingAward));
    }

    @Override
    @Transactional
    public BestBandAwardDto delete(Long id) {
        BestBandAward bestBandAward = findById(id);
        bestBandAwardRepository.delete(bestBandAward);
        return mapper.toDto(bestBandAward);
    }

    @Override
    public void deleteAllByBandId(Integer bandId) {
        List<BestBandAward> bestBandAwards = bestBandAwardRepository.findAllByBandId(bandId);
        bestBandAwardRepository.deleteAll(bestBandAwards);
    }

}
