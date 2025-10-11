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

import java.util.List;

import static org.is.bandmanager.exception.message.ServiceErrorMessage.MUST_BE_NOT_NULL;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.SOURCE_NOT_FOUND;

@Service
@Validated
@RequiredArgsConstructor
public class MusicBandServiceImpl implements MusicBandService {

    private final MusicBandRepository musicBandRepository;
    private final MusicBandMapper mapper;

    private MusicBand findById(Integer id) {
        if (id == null) {
            throw new ServiceException(MUST_BE_NOT_NULL, "MusicBand.id");
        }
        return musicBandRepository.findById(id)
                .orElseThrow(() -> new ServiceException(SOURCE_NOT_FOUND, "MusicBand", id));
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
        return mapper.toDto(musicBand);
    }

}
