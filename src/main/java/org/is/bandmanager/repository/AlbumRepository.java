package org.is.bandmanager.repository;

import org.is.bandmanager.model.Album;
import org.is.bandmanager.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    @Query("SELECT p FROM Album p WHERE NOT EXISTS (SELECT 1 FROM MusicBand m WHERE m.bestAlbum.id = p.id)")
    List<Album> findUnusedAlbum();
    
}
