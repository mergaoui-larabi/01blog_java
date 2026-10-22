package com.blog.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.blog.user.domain.entity.FollowerKey;
import com.blog.user.domain.entity.Followers;
import com.blog.user.exception.InvalidValueException;
import com.blog.user.repository.FollowersRepository;
import com.blog.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FollowerServiceTest {

    @Mock
    private FollowersRepository followersRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowerService followerService;

    private UUID user1;
    private UUID user2;

    @BeforeEach
    void setUp() {
        user1 = UUID.randomUUID();
        user2 = UUID.randomUUID();
    }

    @Test
    @DisplayName("Successfully follow another user")
    void follow_Success() {
        when(userRepository.existsById(user2)).thenReturn(true);
        when(followersRepository.existsById(new FollowerKey(user1, user2))).thenReturn(false);

        followerService.follow(user1, user2);

        verify(followersRepository).save(any(Followers.class));
    }

    @Test
    @DisplayName("Self-follow is prohibited")
    void follow_SelfFollow_ThrowsException() {
        assertThatThrownBy(() -> followerService.follow(user1, user1))
                .isInstanceOf(InvalidValueException.class)
                .hasMessageContaining("cannot follow themselves");

        verify(followersRepository, never()).save(any());
    }

    @Test
    @DisplayName("Following non-existent user throws exception")
    void follow_NonExistentUser_ThrowsException() {
        when(userRepository.existsById(user2)).thenReturn(false);

        assertThatThrownBy(() -> followerService.follow(user1, user2))
                .isInstanceOf(InvalidValueException.class)
                .hasMessageContaining("does not exist");

        verify(followersRepository, never()).save(any());
    }

    @Test
    @DisplayName("Duplicate follow is idempotent (no second save)")
    void follow_Duplicate_Idempotent() {
        when(userRepository.existsById(user2)).thenReturn(true);
        when(followersRepository.existsById(new FollowerKey(user1, user2))).thenReturn(true);

        followerService.follow(user1, user2);

        verify(followersRepository, never()).save(any());
    }

    @Test
    @DisplayName("Unfollow existing follow relationship")
    void unfollow_Existing_Deletes() {
        FollowerKey key = new FollowerKey(user1, user2);
        when(followersRepository.existsById(key)).thenReturn(true);

        followerService.unfollow(user1, user2);

        verify(followersRepository).deleteById(key);
    }

    @Test
    @DisplayName("Unfollow non-existent follow relationship is no-op")
    void unfollow_NonExistent_NoOp() {
        FollowerKey key = new FollowerKey(user1, user2);
        when(followersRepository.existsById(key)).thenReturn(false);

        followerService.unfollow(user1, user2);

        verify(followersRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Retrieve followed user IDs")
    void getFollowedUserIds_ReturnsList() {
        List<UUID> expected = List.of(user2, UUID.randomUUID());
        when(followersRepository.findFollowedIdsByFollowerId(user1)).thenReturn(expected);

        List<UUID> actual = followerService.getFollowedUserIds(user1);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Empty followed user list returns empty list")
    void getFollowedUserIds_Empty_ReturnsEmptyList() {
        when(followersRepository.findFollowedIdsByFollowerId(user1)).thenReturn(List.of());

        List<UUID> actual = followerService.getFollowedUserIds(user1);

        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("Check if user is following")
    void isFollowing_ReturnsTrueWhenFollows() {
        when(followersRepository.existsByFollowerIdAndFollowedId(user1, user2)).thenReturn(true);

        boolean result = followerService.isFollowing(user1, user2);

        assertThat(result).isTrue();
    }
}
