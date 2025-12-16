package com.project.cinemabackend.model;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_embeddings", schema = "public")
public class UserEmbedding {
    @Id
    @GeneratedValue
    @Column(name = "embedding_id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "embedding_vector", columnDefinition = "double precision[]")
    private double[] embeddingVector;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "feature_mapping", columnDefinition = "jsonb")
    private Map<Integer, String> featureMapping;
}