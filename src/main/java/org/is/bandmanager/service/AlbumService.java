package org.is.bandmanager.service;

import jakarta.validation.Valid;
import org.is.bandmanager.dto.AlbumDto;
import org.is.bandmanager.dto.request.AlbumRequest;
import org.is.bandmanager.model.Album;

import java.util.List;


public interface AlbumService {

    AlbumDto create(@Valid AlbumRequest request);

    List<AlbumDto> getAll();

    AlbumDto get(Long id);

    Album getEntity(Long id);

    AlbumDto update(Long id, @Valid AlbumRequest request);

    AlbumDto delete(Long id);

}
