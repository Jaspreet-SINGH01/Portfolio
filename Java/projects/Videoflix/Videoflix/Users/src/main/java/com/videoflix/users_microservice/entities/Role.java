package com.videoflix.users_microservice.entities;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Role {
    USER(Stream.of(
            Permission.VIDEO_VIEW,
            Permission.VIDEO_LIKE_DISLIKE,
            Permission.PLAYLIST_ADD_VIDEO).collect(Collectors.toSet())),
    ADMIN(Stream.of(
            Permission.VIDEO_VIEW,
            Permission.VIDEO_LIKE_DISLIKE,
            Permission.PLAYLIST_ADD_VIDEO,
            Permission.VIDEO_ADD,
            Permission.VIDEO_UPDATE,
            Permission.VIDEO_DELETE,
            Permission.VIDEO_ADD_DESCRIPTION).collect(Collectors.toSet()));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }
}