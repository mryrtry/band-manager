package org.is.bandmanager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;


@Entity
@Table(name = "music_band")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MusicBand extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "MusicBand.Name не может быть пустым")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "MusicBand.Coordinates не может быть пустым")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "coordinates_id", nullable = false)
    private Coordinates coordinates;

    @NotNull(message = "MusicBand.MusicGenre не может быть пустым")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MusicGenre genre;

    @NotNull(message = "MusicBand.NumberOfParticipants не может быть пустым")
    @DecimalMin(value = "0", inclusive = false, message = "MusicBand.NumberOfParticipants должно быть > 0")
    @Column(name = "number_of_participants", nullable = false)
    private Long numberOfParticipants;

    @NotNull(message = "MusicBand.SinglesCount не может быть пустым")
    @DecimalMin(value = "0", inclusive = false, message = "MusicBand.NumberOfParticipants должно быть > 0")
    @Column(name = "singles_count", nullable = false)
    private Long singlesCount;

    @NotBlank(message = "MusicBand.Description не может быть пустым")
    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @NotNull(message = "MusicBand.BestAlbum не может быть пустым")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "best_album_id", nullable = false)
    private Album bestAlbum;

    @NotNull(message = "MusicBand.AlbumsCount не может быть пустым")
    @Column(name = "albums_count", nullable = false)
    @DecimalMin(value = "0", inclusive = false, message = "MusicBand.AlbumsCount должно быть > 0")
    private Long albumsCount;

    @NotNull(message = "MusicBand.EstablishmentDate не может быть пустым")
    @Column(name = "establishment_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date establishmentDate;

    @NotNull(message = "MusicBand.FrontMan не может быть пустым")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "front_man_id", nullable = false)
    private Person frontMan;

}
