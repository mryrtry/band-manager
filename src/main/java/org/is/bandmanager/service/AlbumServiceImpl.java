package org.is.bandmanager.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.AlbumDto;
import org.is.bandmanager.dto.AlbumMapper;
import org.is.bandmanager.dto.request.AlbumRequest;
import org.is.bandmanager.exception.ServiceException;
import org.is.bandmanager.model.Album;
import org.is.bandmanager.repository.AlbumRepository;
import org.is.bandmanager.repository.MusicBandRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static org.is.bandmanager.exception.message.ServiceErrorMessage.*;

@Service
@Validated
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final MusicBandRepository musicBandRepository;
    private final AlbumMapper mapper;

    private Album findById(Long id) {
        if (id == null) {
            throw new ServiceException(MUST_BE_NOT_NULL, "Album.id");
        }
        return albumRepository.findById(id)
                .orElseThrow(() -> new ServiceException(SOURCE_NOT_FOUND, "Album", id));
    }

    private void checkDependencies(Album album) {
        Long albumId = album.getId();
        if (musicBandRepository.existsByBestAlbumId(albumId))
            throw new ServiceException(ENTITY_IN_USE, "Album", albumId, "MusicBand");
    }

    @Override
    @Transactional
    public AlbumDto create(AlbumRequest request) {
        Album album = albumRepository.save(mapper.toEntity(request));
        return mapper.toDto(album);
    }

    @Override
    public List<AlbumDto> getAll() {
        return albumRepository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public AlbumDto get(Long id) {
        return mapper.toDto(findById(id));
    }

    @Override
    public Album getEntity(Long id) {
        return findById(id);
    }

    @Override
    @Transactional
    public AlbumDto update(Long id, AlbumRequest request) {
        findById(id);
        Album updatedAlbum = mapper.toEntity(request);
        updatedAlbum.setId(id);
        return mapper.toDto(albumRepository.save(updatedAlbum));
    }

    @Override
    @Transactional
    public AlbumDto delete(Long id) {
        Album album = findById(id);
        checkDependencies(album);
        albumRepository.delete(album);
        return mapper.toDto(album);
    }

    @Async("cleanupTaskExecutor")
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void cleanupUnusedAlbums() {
        List<Album> unusedAlbums = albumRepository.findUnusedAlbum();
        if (!unusedAlbums.isEmpty()) {
            albumRepository.deleteAll(unusedAlbums);
        }
    }

}
