package org.is.bandmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "album")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Album.name не может быть пустым")
    @Column(nullable = false)
    private String name;

    @NotNull
    @Min(value = 1, message = "Tracks должно быть > 0")
    @Column(nullable = false)
    private Long tracks;

    @Min(value = 1, message = "Sales должно быть > 0")
    @Column(nullable = false)
    private int sales;

}
