package com.blog.media.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.blog.media.application.dto.request.UpdateMediaRequest;
import com.blog.media.constant.ValidationConstants;
import com.blog.media.domain.entity.Media;
import com.blog.media.domain.model.MediaPurpose;
import com.blog.media.domain.model.MediaStatus;
import com.blog.media.domain.valueobject.DetectedContentType;
import com.blog.media.domain.valueobject.DurationMs;
import com.blog.media.domain.valueobject.Height;
import com.blog.media.domain.valueobject.OriginalFilename;
import com.blog.media.domain.valueobject.Purpose;
import com.blog.media.domain.valueobject.SizeBytes;
import com.blog.media.domain.valueobject.Status;
import com.blog.media.domain.valueobject.StorageKey;
import com.blog.media.domain.valueobject.Width;
import com.blog.media.exception.InvalidValueException;
import com.blog.media.exception.MediaAccessDeniedException;
import com.blog.media.exception.MediaNotFoundException;
import com.blog.media.repository.MediaRepository;

@Service
public class MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaService.class);

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/gif", "gif",
            "image/webp", "webp",
            "image/avif", "avif",
            "video/mp4", "mp4",
            "video/webm", "webm",
            "video/ogg", "ogg");

    private final MediaRepository mediaRepository;
    private final Path storageRoot;

    public MediaService(
            MediaRepository mediaRepository,
            @Value("${media.storage.location}") String storageLocation) {
        this.mediaRepository = mediaRepository;
        this.storageRoot = Paths.get(storageLocation);
    }

    /** Download payload: bytes plus the metadata needed to serve them. */
    public record MediaContent(Resource resource, String contentType, String filename) {
    }

    // ------------------------------------------------------------------
    // Create
    // ------------------------------------------------------------------

    @Transactional
    public Media createAvatar(UUID ownerId, MultipartFile file) {
        return create(ownerId, file, new Purpose(MediaPurpose.AVATAR_IMAGE));
    }

    @Transactional
    public Media createPostMedia(UUID ownerId, MultipartFile file) {
        return create(ownerId, file, null);
    }

    private Media create(UUID ownerId, MultipartFile file, Purpose fixedPurpose) {
        assertUploadable(file);

        Path temp = null;
        Path target = null;
        try {
            Files.createDirectories(storageRoot);
            temp = Files.createTempFile(storageRoot, "upload-", ".tmp");
            file.transferTo(temp);

            DetectedContentType detectedContentType = new DetectedContentType(detectContentType(temp, file));
            Purpose purpose = fixedPurpose != null ? fixedPurpose : derivePurpose(detectedContentType.value());
            requireCompatible(purpose.value(), detectedContentType.value());

            StorageKey storageKey = buildStorageKey(purpose.value(), ownerId, detectedContentType.value());
            target = resolve(storageKey);
            Files.createDirectories(target.getParent());
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            temp = null;

            Dimensions dimensions = readDimensions(target, detectedContentType.value());

            Media media = new Media(
                    ownerId,
                    purpose,
                    new Status(MediaStatus.ACCEPTED),
                    new OriginalFilename(sanitizeFilename(file.getOriginalFilename())),
                    storageKey,
                    detectedContentType,
                    new SizeBytes(file.getSize()),
                    new Width(dimensions.width()),
                    new Height(dimensions.height()),
                    new DurationMs(null));
            try {
                return mediaRepository.save(media);
            } catch (RuntimeException exception) {
                Files.deleteIfExists(target);
                throw exception;
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to store media file", exception);
        } finally {
            deleteQuietly(temp);
        }
    }

    // ------------------------------------------------------------------
    // Read
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Media getMedia(UUID mediaId, UUID requesterId) {
        Media media = findMedia(mediaId);
        assertOwner(media, requesterId);
        return media;
    }

    @Transactional(readOnly = true)
    public MediaContent getContent(UUID mediaId, UUID requesterId) {
        Media media = getMedia(mediaId, requesterId);
        Path path = resolve(new StorageKey(media.getStorageKey()));
        if (!Files.isRegularFile(path)) {
            throw new MediaNotFoundException("Media content not found");
        }
        return new MediaContent(
                new FileSystemResource(path),
                media.getDetectedContentType(),
                media.getOriginalFilename());
    }

    // ------------------------------------------------------------------
    // Update
    // ------------------------------------------------------------------

    @Transactional
    public Media updateMedia(UUID mediaId, UUID requesterId, boolean isAdmin, UpdateMediaRequest request) {
        Media media = findMedia(mediaId);
        assertWriteAccess(media, requesterId, isAdmin);

        boolean hasFile = request.file() != null && !request.file().isEmpty();
        boolean hasPurpose = request.purpose() != null && !request.purpose().isBlank();

        if (!hasFile && !hasPurpose) {
            throw new InvalidValueException("Update must include a file or a purpose");
        }

        if (!hasFile) {
            Purpose purpose = new Purpose(request.purpose().trim());
            requireCompatible(purpose.value(), media.getDetectedContentType());
            media.changePurpose(purpose);
            media.touch();
            return mediaRepository.save(media);
        }

        MultipartFile file = request.file();
        assertUploadable(file);

        Path temp = null;
        Path target = null;
        try {
            Files.createDirectories(storageRoot);
            temp = Files.createTempFile(storageRoot, "upload-", ".tmp");
            file.transferTo(temp);

            DetectedContentType detectedContentType = new DetectedContentType(detectContentType(temp, file));
            // keep the current purpose unless the caller explicitly changes it
            Purpose effectivePurpose = hasPurpose
                    ? new Purpose(request.purpose().trim())
                    : new Purpose(media.getPurpose());
            requireCompatible(effectivePurpose.value(), detectedContentType.value());

            StorageKey newStorageKey = buildStorageKey(effectivePurpose.value(), media.getOwnerId(),
                    detectedContentType.value());
            target = resolve(newStorageKey);
            Files.createDirectories(target.getParent());
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            temp = null;

            Dimensions dimensions = readDimensions(target, detectedContentType.value());

            String oldStorageKey = media.getStorageKey();
            media.changePurpose(effectivePurpose);
            media.replaceFile(
                    new OriginalFilename(sanitizeFilename(file.getOriginalFilename())),
                    newStorageKey,
                    detectedContentType,
                    new SizeBytes(file.getSize()),
                    new Width(dimensions.width()),
                    new Height(dimensions.height()),
                    new DurationMs(null));

            Media saved = mediaRepository.save(media);
            deleteQuietly(resolve(new StorageKey(oldStorageKey)));
            return saved;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to store media file", exception);
        } finally {
            deleteQuietly(temp);
        }
    }

    // ------------------------------------------------------------------
    // Delete
    // ------------------------------------------------------------------

    @Transactional
    public void deleteMedia(UUID mediaId, UUID requesterId, boolean isAdmin) {
        Media media = findMedia(mediaId);
        assertWriteAccess(media, requesterId, isAdmin);

        String storageKey = media.getStorageKey();
        mediaRepository.delete(media);
        deleteQuietly(resolve(new StorageKey(storageKey)));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Media findMedia(UUID mediaId) {
        return mediaRepository.findById(mediaId)
                .orElseThrow(() -> new MediaNotFoundException("Media not found: " + mediaId));
    }

    private void assertOwner(Media media, UUID requesterId) {
        if (!media.getOwnerId().equals(requesterId)) {
            throw new MediaAccessDeniedException("You do not have access to this media");
        }
    }

    private void assertWriteAccess(Media media, UUID requesterId, boolean isAdmin) {
        if (!media.getOwnerId().equals(requesterId) && !isAdmin) {
            throw new MediaAccessDeniedException("Only the owner or an admin can modify this media");
        }
    }

    private void assertUploadable(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidValueException("File cannot be empty");
        }
        if (file.getSize() > ValidationConstants.MAX_UPLOAD_SIZE_BYTES) {
            throw new InvalidValueException(
                    "File must not exceed " + ValidationConstants.MAX_UPLOAD_SIZE_BYTES + " bytes");
        }
    }

    private String detectContentType(Path path, MultipartFile file) throws IOException {
        String detected = Files.probeContentType(path);
        if (detected == null || detected.isBlank()) {
            detected = file.getContentType();
        }
        if (detected == null || detected.isBlank()) {
            throw new InvalidValueException("Could not detect the content type of the uploaded file");
        }
        int semicolon = detected.indexOf(';');
        if (semicolon >= 0) {
            detected = detected.substring(0, semicolon);
        }
        return detected.trim().toLowerCase(Locale.ROOT);
    }

    private Purpose derivePurpose(String contentType) {
        return contentType.startsWith("image/")
                ? new Purpose(MediaPurpose.POST_IMAGE)
                : new Purpose(MediaPurpose.POST_VIDEO);
    }

    private void requireCompatible(MediaPurpose purpose, String contentType) {
        boolean isImage = contentType.startsWith("image/");
        switch (purpose) {
            case AVATAR_IMAGE, POST_IMAGE -> {
                if (!isImage) {
                    throw new InvalidValueException(purpose + " media must be an image");
                }
            }
            case POST_VIDEO -> {
                if (isImage) {
                    throw new InvalidValueException("POST_VIDEO media must be a video");
                }
            }
        }
    }

    private StorageKey buildStorageKey(MediaPurpose purpose, UUID ownerId, String contentType) {
        String extension = EXTENSIONS.getOrDefault(contentType, "bin");
        String directory = switch (purpose) {
            case AVATAR_IMAGE -> "avatars";
            case POST_IMAGE, POST_VIDEO -> "posts";
        };
        return new StorageKey(directory + "/" + ownerId + "/" + UUID.randomUUID() + "." + extension);
    }

    private Path resolve(StorageKey storageKey) {
        return storageRoot.resolve(storageKey.value()).normalize();
    }

    private String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "upload";
        }
        String name = originalFilename.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        name = name.trim();
        if (name.isBlank() || name.equals(".") || name.equals("..")) {
            return "upload";
        }
        return name;
    }

    private record Dimensions(Integer width, Integer height) {
    }

    private Dimensions readDimensions(Path path, String contentType) {
        if (!contentType.startsWith("image/")) {
            return new Dimensions(null, null);
        }
        try {
            BufferedImage image = ImageIO.read(path.toFile());
            if (image == null) {
                return new Dimensions(null, null);
            }
            return new Dimensions(image.getWidth(), image.getHeight());
        } catch (IOException exception) {
            log.warn("Could not read image dimensions from {}", path, exception);
            return new Dimensions(null, null);
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            log.warn("Could not delete file {}", path, exception);
        }
    }
}
