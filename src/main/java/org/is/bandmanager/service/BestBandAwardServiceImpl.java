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
            throw new ServiceException(MUST_BE_NOT_NULL, "Coordinates.id");
        }
        return bestBandAwardRepository.findById(id)
                .orElseThrow(() -> new ServiceException(SOURCE_NOT_FOUND, "Coordinates", id));
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
    public List<BestBandAwardDto> getAll() {
        return bestBandAwardRepository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public BestBandAwardDto get(Long id) {
        return mapper.toDto(findById(id));
    }

    @Override
    @Transactional
    public BestBandAwardDto update(Long id, BestBandAwardRequest request) {
        findById(id);
        BestBandAward updatedBestBandAward = mapper.toEntity(request);
        updatedBestBandAward.setBand(fetchMusicBandById(request.getMusicBandId()));
        updatedBestBandAward.setId(id);
        return mapper.toDto(bestBandAwardRepository.save(updatedBestBandAward));
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
