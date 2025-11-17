package com.project.cinemabackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Embeddable
public class MovieTagId implements java.io.Serializable {
    private static final long serialVersionUID = 8070828057577045369L;
    @Column(name = "movie_id", nullable = false)
    private UUID movieId;

    @Column(name = "tag_id", nullable = false)
    private UUID tagId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        MovieTagId entity = (MovieTagId) o;
        return Objects.equals(this.tagId, entity.tagId) &&
                Objects.equals(this.movieId, entity.movieId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId, movieId);
    }

}