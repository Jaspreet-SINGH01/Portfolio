package com.videoflix.content.repositories;

import com.videoflix.content.entities.Content;
import com.videoflix.content.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByStatus(ContentStatus status);
    List<Content> findByGenres_Name(String genreName);
    Optional<Content> findByTitle(String title);
}