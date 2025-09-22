package org.is.bandmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "music_band")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MusicBand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "name не может быть пустым")
    @Column(nullable = false)
    private String name;

    @NotNull
    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "coordinates_id", nullable = false)
    private Coordinates coordinates;

    @NotNull
    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDate creationDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MusicGenre genre;

    @Min(value = 1, message = "numberOfParticipants должно быть > 0")
    @Column(name = "number_of_participants", nullable = false)
    private long numberOfParticipants;

    @Min(value = 1, message = "singlesCount должно быть > 0")
    @Column(name = "singles_count", nullable = false)
    private long singlesCount;

    @NotBlank(message = "description не может быть пустым")
    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @NotNull
    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "best_album_id", nullable = false)
    private Album bestAlbum;

    @NotNull
    @Min(value = 1, message = "albumsCount должно быть > 0")
    @Column(name = "albums_count", nullable = false)
    private Long albumsCount;

    @NotNull
    @Column(name = "establishment_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date establishmentDate;

    @NotNull
    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "front_man_id", nullable = false)
    private Person frontMan;

    @PrePersist
    protected void onCreate() {
        if (this.creationDate == null) {
            this.creationDate = LocalDate.now();
        }
    }

}
