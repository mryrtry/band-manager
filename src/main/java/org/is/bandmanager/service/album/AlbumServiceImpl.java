package org.is.bandmanager.service.album;

import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.AlbumDto;
import org.is.bandmanager.dto.AlbumMapper;
import org.is.bandmanager.dto.request.AlbumRequest;
import org.is.event.EntityEvent;
import org.is.exception.ServiceException;
import org.is.bandmanager.model.Album;
import org.is.bandmanager.repository.AlbumRepository;
import org.is.bandmanager.repository.MusicBandRepository;
import org.is.bandmanager.service.cleanup.CleanupStrategy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static org.is.event.EventType.CREATED;
import static org.is.event.EventType.DELETED;
import static org.is.event.EventType.UPDATED;
import static org.is.bandmanager.exception.message.BandManagerErrorMessage.ENTITY_IN_USE;
import static org.is.bandmanager.exception.message.BandManagerErrorMessage.ID_MUST_BE_POSITIVE;
import static org.is.bandmanager.exception.message.BandManagerErrorMessage.MUST_BE_NOT_NULL;
import static org.is.bandmanager.exception.message.BandManagerErrorMessage.SOURCE_WITH_ID_NOT_FOUND;


@Service
@Validated
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService, CleanupStrategy<Album, AlbumDto> {

    private final AlbumRepository albumRepository;

    private final MusicBandRepository musicBandRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final AlbumMapper mapper;

    private Album findById(Long id) {
        if (id == null) {
            throw new ServiceException(MUST_BE_NOT_NULL, "Album.id");
        }
        if (id <= 0) {
            throw new ServiceException(ID_MUST_BE_POSITIVE, "Album.id");
        }
        return albumRepository.findById(id).orElseThrow(() -> new ServiceException(SOURCE_WITH_ID_NOT_FOUND, "Album", id));
    }

    @Transactional
    protected void checkDependencies(Album album) {
        Long albumId = album.getId();
        if (musicBandRepository.existsByBestAlbumId(albumId)) {
            throw new ServiceException(ENTITY_IN_USE, "Album", albumId, "MusicBand");
        }
    }

    @Override
    @Transactional
    public AlbumDto create(AlbumRequest request) {
        Album album = mapper.toEntity(request);
        AlbumDto createdAlbum = mapper.toDto(albumRepository.save(album));
        eventPublisher.publishEvent(new EntityEvent<>(CREATED, createdAlbum));
        return createdAlbum;
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
    public AlbumDto update(Long id, AlbumRequest request) {
        Album updatingAlbum = findById(id);
        mapper.updateEntityFromRequest(request, updatingAlbum);
        AlbumDto updatedAlbum = mapper.toDto(albumRepository.save(updatingAlbum));
        eventPublisher.publishEvent(new EntityEvent<>(UPDATED, updatedAlbum));
        return updatedAlbum;
    }

    @Override
    @Transactional
    public AlbumDto delete(Long id) {
        Album album = findById(id);
        checkDependencies(album);
        albumRepository.delete(album);
        AlbumDto deletedAlbum = mapper.toDto(album);
        eventPublisher.publishEvent(new EntityEvent<>(DELETED, deletedAlbum));
        return deletedAlbum;
    }


    @Override
    public List<Album> findUnusedEntities() {
        return albumRepository.findUnusedAlbum();
    }

    @Override
    public void deleteEntities(List<Album> entities) {
        albumRepository.deleteAll(entities);
    }

    @Override
    public List<AlbumDto> convertToDto(List<Album> entities) {
        return entities.stream().map(mapper::toDto).toList();
    }

    @Override
    public String getEntityName() {
        return "Album";
    }

}
