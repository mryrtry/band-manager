package org.is.bandmanager.service;

import org.is.bandmanager.dto.AlbumDto;
import org.is.bandmanager.dto.request.AlbumRequest;

import java.util.List;

public interface AlbumService {

    AlbumDto create(AlbumRequest request);

    List<AlbumDto> getAll();

    AlbumDto get(Long id);

    AlbumDto update(Long id, AlbumRequest request);

    AlbumDto delete(Long id);

}
