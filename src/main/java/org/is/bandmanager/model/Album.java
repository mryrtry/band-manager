package org.is.bandmanager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


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

	@NotBlank(message = "Album.Name не может быть пустым")
	@Column(nullable = false)
	private String name;

	@NotNull(message = "Album.Tracks не может быть пустым")
	@DecimalMin(value = "0", inclusive = false, message = "Album.Tracks должно быть > 0")
	@Column(nullable = false)
	private Long tracks;

	@DecimalMin(value = "0", inclusive = false, message = "Album.Sales должно быть > 0")
	@Column(nullable = false)
	private Integer sales;

}
