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
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.is.model.AuditableEntity;


@Entity
@Table(name = "best_band_award")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BestBandAward extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Award.Band не может быть пустым")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "band_id", nullable = false)
    private MusicBand band;

    @NotNull(message = "Award.Genre не может быть пустым")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MusicGenre genre;

}