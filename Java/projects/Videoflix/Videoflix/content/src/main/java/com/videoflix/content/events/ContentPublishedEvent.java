package com.videoflix.content.events;

import com.videoflix.content.entities.Content;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ContentPublishedEvent extends ApplicationEvent {
    private final Long contentId;
    private final String title;
    private final String contentType; // MOVIE, SERIES, etc.

    public ContentPublishedEvent(Object source, Content content) {
        super(source);
        this.contentId = content.getId();
        this.title = content.getTitle();
        this.contentType = content.getType().getName();
    }
}