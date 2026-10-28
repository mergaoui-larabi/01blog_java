package com.blog.media.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.blog.media.application.dto.request.UpdateMediaRequest;
import com.blog.media.application.dto.response.MediaResponse;
import com.blog.media.constant.SecurityConstants;
import com.blog.media.domain.entity.Media;
import com.blog.media.service.MediaService;

@RestController
@RequestMapping("/api/v1/medias")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    // upload an avatar image
    @PostMapping(value = "/avatars", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaResponse> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) {
        Media media = mediaService.createAvatar(userId(jwt), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(media));
    }

    // upload a post image or video (purpose derived from the content type)
    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<MediaResponse>> uploadPostMedia(
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal Jwt jwt) {

        List<MediaResponse> responses = files.stream()
                .map(file -> mediaService.createPostMedia(userId(jwt), file))
                .map(this::toResponse)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    // validate that the media exists and the given user is the owner (owner or
    // admin)
    // @GetMapping("/{mediaId}")
    // public ResponseEntity<Void> validateMediaOwnership(
    //         @PathVariable UUID mediaId,
    //         @AuthenticationPrincipal Jwt jwt) {

    //     mediaService.validateMediaOwnership(mediaId, userId(jwt));

    //     return ResponseEntity.noContent().build();
    // }

    // avatar bytes (owner only)
    @GetMapping("/{mediaId}/avatar")
    public ResponseEntity<org.springframework.core.io.Resource> getAvatar(
            @PathVariable UUID mediaId,
            @AuthenticationPrincipal Jwt jwt) {
        MediaService.MediaContent content = mediaService.getContent(mediaId, userId(jwt));

        ContentDisposition disposition = ContentDisposition.inline()
                .filename(content.filename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(content.resource());
    }

    // media bytes (owner only)
    @GetMapping("/{mediaId}/content")
    public ResponseEntity<org.springframework.core.io.Resource> downloadMedia(
            @PathVariable UUID mediaId,
            @AuthenticationPrincipal Jwt jwt) {
        MediaService.MediaContent content = mediaService.getContent(mediaId, userId(jwt));

        ContentDisposition disposition = ContentDisposition.inline()
                .filename(content.filename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(content.resource());
    }

    // replace the file and/or change the purpose (owner or admin)
    @PutMapping("/{mediaId}")
    public ResponseEntity<MediaResponse> updateMedia(
            @PathVariable UUID mediaId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "purpose", required = false) String purpose,
            @AuthenticationPrincipal Jwt jwt) {
        Media media = mediaService.updateMedia(
                mediaId,
                userId(jwt),
                isAdmin(jwt),
                new UpdateMediaRequest(file, purpose));
        return ResponseEntity.ok(toResponse(media));
    }

    // delete metadata and stored bytes (owner or admin)
    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable UUID mediaId,
            @AuthenticationPrincipal Jwt jwt) {
        mediaService.deleteMedia(mediaId, userId(jwt), isAdmin(jwt));
        return ResponseEntity.noContent().build();
    }

    // test if the service is up
    @GetMapping("ping")
    public String ping() {
        return "pong";
    }

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private boolean isAdmin(Jwt jwt) {
        return SecurityConstants.ADMIN_ROLE.equals(jwt.getClaimAsString(SecurityConstants.ROLE_CLAIM));
    }

    private MediaResponse toResponse(Media media) {
        return new MediaResponse(
                media.getId(),
                media.getOwnerId(),
                media.getPurpose(),
                media.getStatus(),
                media.getOriginalFilename(),
                media.getDetectedContentType(),
                media.getSizeBytes(),
                media.getWidth(),
                media.getHeight(),
                media.getDurationMs(),
                media.getCreatedAt(),
                media.getUpdatedAt());
    }
}
