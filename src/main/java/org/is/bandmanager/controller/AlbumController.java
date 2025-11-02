package org.is.bandmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.is.bandmanager.dto.AlbumDto;
import org.is.bandmanager.dto.request.AlbumRequest;
import org.is.bandmanager.service.album.AlbumService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping
    @PreAuthorize("@securityService.canReadEntity()")
    public ResponseEntity<List<AlbumDto>> getAllAlbums() {
        return ResponseEntity.ok(albumService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.canReadEntity()")
    public ResponseEntity<AlbumDto> getAlbum(@PathVariable Long id) {
        AlbumDto album = albumService.get(id);
        return ResponseEntity.ok(album);
    }

    @PostMapping
    @PreAuthorize("@securityService.canCreate()")
    public ResponseEntity<AlbumDto> createAlbum(@Valid @RequestBody AlbumRequest request) {
        AlbumDto createdAlbum = albumService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAlbum);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.canUpdateEntity(#id, 'Album')")
    public ResponseEntity<AlbumDto> updateAlbum(@PathVariable Long id, @Valid @RequestBody AlbumRequest request) {
        AlbumDto updatedAlbum = albumService.update(id, request);
        return ResponseEntity.ok(updatedAlbum);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.canDeleteEntity(#id, 'Album')")
    public ResponseEntity<AlbumDto> deleteAlbum(@PathVariable Long id) {
        AlbumDto deletedAlbum = albumService.delete(id);
        return ResponseEntity.ok(deletedAlbum);
    }

}