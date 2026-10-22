package com.blog.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.blog.post.application.dto.post.PageResponse;
import com.blog.post.application.dto.post.PostResponse;
import com.blog.post.application.dto.request.CreatePostRequest;
import com.blog.post.application.dto.request.ReplacePostRequest;
import com.blog.post.application.dto.request.UpdatePostRequest;
import com.blog.post.client.MediaServiceClient;
import com.blog.post.client.UserServiceClient;
import com.blog.post.domain.entity.Post;
import com.blog.post.domain.valueobject.PostContent;
import com.blog.post.exception.InvalidMediaException;
import com.blog.post.exception.PostNotOwnedException;
import com.blog.post.exception.UserServiceUnavailableException;
import com.blog.post.repository.CommentRepository;
import com.blog.post.repository.LikeRepository;
import com.blog.post.repository.PostRepository;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private MediaServiceClient mediaServiceClient;

    @InjectMocks
    private PostService postService;

    private UUID currentUserId;
    private UUID followedUserId;
    private UUID unrelatedUserId;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        followedUserId = UUID.randomUUID();
        unrelatedUserId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Personalized Feed Tests")
    class FeedTests {

        @Test
        @DisplayName("Feed includes author's own posts and followed users' posts")
        void fetchFeed_IncludesOwnAndFollowedPosts() {
            when(userServiceClient.getFollowedUserIds(currentUserId)).thenReturn(List.of(followedUserId));

            Post ownPost = new Post(UUID.randomUUID(), currentUserId, new PostContent("Own post"));
            Post followedPost = new Post(UUID.randomUUID(), followedUserId, new PostContent("Followed post"));

            when(postRepository.findFeedFirstPage(any(), any(Pageable.class)))
                    .thenReturn(List.of(ownPost, followedPost));
            when(likeRepository.findLikedPostIdsByUserIdAndPostIdIn(eq(currentUserId), any()))
                    .thenReturn(Collections.emptySet());

            PageResponse<PostResponse> result = postService.fetchFeed(currentUserId, null, 10);

            assertThat(result.content()).hasSize(2);
            assertThat(result.content().get(0).authorId()).isEqualTo(currentUserId.toString());
            assertThat(result.content().get(1).authorId()).isEqualTo(followedUserId.toString());
            assertThat(result.hasNext()).isFalse();
            assertThat(result.nextCursor()).isNull();
        }

        @Test
        @DisplayName("Empty following list still queries author's own posts")
        void fetchFeed_EmptyFollowingList_ReturnsOwnPosts() {
            when(userServiceClient.getFollowedUserIds(currentUserId)).thenReturn(Collections.emptyList());

            Post ownPost = new Post(UUID.randomUUID(), currentUserId, new PostContent("My lone post"));
            when(postRepository.findFeedFirstPage(eq(Set.of(currentUserId)), any(Pageable.class)))
                    .thenReturn(List.of(ownPost));

            PageResponse<PostResponse> result = postService.fetchFeed(currentUserId, null, 10);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).content()).isEqualTo("My lone post");
        }

        @Test
        @DisplayName("Cursor pagination calculates nextCursor and hasNext correctly")
        void fetchFeed_CursorPagination_HasNext() {
            when(userServiceClient.getFollowedUserIds(currentUserId)).thenReturn(List.of(followedUserId));

            Post post1 = new Post(UUID.randomUUID(), currentUserId, new PostContent("Post 1"));
            Post post2 = new Post(UUID.randomUUID(), followedUserId, new PostContent("Post 2"));
            Post post3 = new Post(UUID.randomUUID(), currentUserId, new PostContent("Post 3"));

            // Size requested is 2, but repository returns 3 items (size + 1)
            when(postRepository.findFeedFirstPage(any(), any(Pageable.class)))
                    .thenReturn(List.of(post1, post2, post3));

            PageResponse<PostResponse> result = postService.fetchFeed(currentUserId, null, 2);

            assertThat(result.content()).hasSize(2);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.nextCursor()).isEqualTo(post2.getId().toString());
        }

        @Test
        @DisplayName("UserService failure propagates UserServiceUnavailableException without returning incorrect feed")
        void fetchFeed_UserServiceFails_ThrowsException() {
            when(userServiceClient.getFollowedUserIds(currentUserId))
                    .thenThrow(new UserServiceUnavailableException("Service down"));

            assertThatThrownBy(() -> postService.fetchFeed(currentUserId, null, 10))
                    .isInstanceOf(UserServiceUnavailableException.class);

            verify(postRepository, never()).findFeedFirstPage(any(), any());
        }
    }

    @Nested
    @DisplayName("Post Media Relationship Tests")
    class PostMediaTests {

        @Test
        @DisplayName("Create post without media")
        void createPost_NoMedia() {
            CreatePostRequest request = new CreatePostRequest("No media here", null);
            when(postRepository.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

            PostResponse response = postService.createPost(request, currentUserId, "Bearer token");

            assertThat(response.content()).isEqualTo("No media here");
            assertThat(response.mediaIds()).isEmpty();
            verify(mediaServiceClient, never()).validateAllMediaOwnership(any(), any());
        }

        @Test
        @DisplayName("Create post with multiple media IDs preserves ordering and validates ownership")
        void createPost_MultipleMedia_PreservesOrder() {
            UUID media1 = UUID.randomUUID();
            UUID media2 = UUID.randomUUID();
            CreatePostRequest request = new CreatePostRequest(
                    "Post with media",
                    List.of(media1.toString(), media2.toString()));

            when(postRepository.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

            PostResponse response = postService.createPost(request, currentUserId, "Bearer token");

            assertThat(response.mediaIds()).containsExactly(media1.toString(), media2.toString());
            verify(mediaServiceClient).validateAllMediaOwnership(List.of(media1, media2), "Bearer token");
        }

        @Test
        @DisplayName("Creating post with invalid media throws InvalidMediaException")
        void createPost_InvalidMedia_ThrowsException() {
            UUID invalidMedia = UUID.randomUUID();
            CreatePostRequest request = new CreatePostRequest(
                    "Bad media",
                    List.of(invalidMedia.toString()));

            org.mockito.Mockito.doThrow(new InvalidMediaException("Invalid media"))
                    .when(mediaServiceClient).validateAllMediaOwnership(any(), any());

            assertThatThrownBy(() -> postService.createPost(request, currentUserId, "Bearer token"))
                    .isInstanceOf(InvalidMediaException.class);

            verify(postRepository, never()).save(any());
        }

        @Test
        @DisplayName("Replace post replaces media associations")
        void replacePost_ReplacesMedia() {
            UUID postId = UUID.randomUUID();
            UUID oldMedia = UUID.randomUUID();
            UUID newMedia = UUID.randomUUID();

            Post post = new Post(postId, currentUserId, new PostContent("Old text"), List.of(oldMedia));
            when(postRepository.findByIdAndAuthorId(postId, currentUserId)).thenReturn(Optional.of(post));
            when(postRepository.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

            ReplacePostRequest request = new ReplacePostRequest(
                    "New text",
                    List.of(newMedia.toString()));

            PostResponse response = postService.replacePost(postId, request, currentUserId, "Bearer token");

            assertThat(response.content()).isEqualTo("New text");
            assertThat(response.mediaIds()).containsExactly(newMedia.toString());
            verify(mediaServiceClient).validateAllMediaOwnership(List.of(newMedia), "Bearer token");
        }

        @Test
        @DisplayName("Update post without mediaIds retains existing media")
        void updatePost_OmittedMedia_RetainsExisting() {
            UUID postId = UUID.randomUUID();
            UUID existingMedia = UUID.randomUUID();

            Post post = new Post(postId, currentUserId, new PostContent("Original"), List.of(existingMedia));
            when(postRepository.findByIdAndAuthorId(postId, currentUserId)).thenReturn(Optional.of(post));
            when(postRepository.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

            UpdatePostRequest request = new UpdatePostRequest("Updated content", null, null);

            PostResponse response = postService.updatePost(postId, request, currentUserId, "Bearer token");

            assertThat(response.content()).isEqualTo("Updated content");
            assertThat(response.mediaIds()).containsExactly(existingMedia.toString());
            verify(mediaServiceClient, never()).validateAllMediaOwnership(any(), any());
        }

        @Test
        @DisplayName("Update post with empty mediaIds clears media")
        void updatePost_EmptyMedia_ClearsMedia() {
            UUID postId = UUID.randomUUID();
            UUID existingMedia = UUID.randomUUID();

            Post post = new Post(postId, currentUserId, new PostContent("Original"), List.of(existingMedia));
            when(postRepository.findByIdAndAuthorId(postId, currentUserId)).thenReturn(Optional.of(post));
            when(postRepository.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

            UpdatePostRequest request = new UpdatePostRequest(null, List.of(), null);

            PostResponse response = postService.updatePost(postId, request, currentUserId, "Bearer token");

            assertThat(response.mediaIds()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Authorization and Ownership Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("Non-owner cannot delete post")
        void deletePost_NotOwned_ThrowsException() {
            UUID postId = UUID.randomUUID();
            when(postRepository.findByIdAndAuthorId(postId, currentUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> postService.deletePost(postId, currentUserId))
                    .isInstanceOf(PostNotOwnedException.class);

            verify(postRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Non-owner cannot update post")
        void updatePost_NotOwned_ThrowsException() {
            UUID postId = UUID.randomUUID();
            when(postRepository.findByIdAndAuthorId(postId, currentUserId)).thenReturn(Optional.empty());

            UpdatePostRequest request = new UpdatePostRequest("Hacked", null, null);

            assertThatThrownBy(() -> postService.updatePost(postId, request, currentUserId, null))
                    .isInstanceOf(PostNotOwnedException.class);
        }

        @Test
        @DisplayName("Owner deletes post: repository delete is called (cascading post_medias)")
        void deletePost_Owned_Success() {
            UUID postId = UUID.randomUUID();
            Post post = new Post(postId, currentUserId, new PostContent("Text"));
            when(postRepository.findByIdAndAuthorId(postId, currentUserId)).thenReturn(Optional.of(post));

            postService.deletePost(postId, currentUserId);

            verify(postRepository).delete(post);
        }
    }
}
