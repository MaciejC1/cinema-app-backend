package com.project.cinemabackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "movie_embeddings", schema = "public")
public class MovieEmbedding {
    @Id
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "embedding_id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

/*
 TODO [Reverse Engineering] create field to map the 'embedding_vector' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "embedding_vector", columnDefinition = "vector(512)")
    private Object embeddingVector;
*/
}