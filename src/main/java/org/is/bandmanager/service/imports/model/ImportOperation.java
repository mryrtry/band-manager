package org.is.bandmanager.service.imports.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.is.auth.model.User;
import org.is.model.AuditableEntity;

import java.time.LocalDateTime;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "import_operations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportOperation extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private String filename;

	@Column(name = "staging_object_key")
	private String stagingObjectKey;

	@Column(name = "storage_object_key")
	private String storageObjectKey;

	@Column(name = "content_type")
	private String contentType;

	@Column(name = "file_size")
	private Long fileSize;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ImportStatus status;

	@Column(name = "created_entities_count")
	private Integer createdEntitiesCount;

	private String errorMessage;

	@Column(name = "started_at")
	private LocalDateTime startedAt;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;
}
