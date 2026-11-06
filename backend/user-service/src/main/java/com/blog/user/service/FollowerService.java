package com.blog.user.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blog.user.domain.model.AccountStatus;
import com.blog.user.domain.entity.FollowerKey;
import com.blog.user.domain.entity.Followers;
import com.blog.user.exception.InvalidValueException;
import com.blog.user.repository.FollowersRepository;
import com.blog.user.repository.UserRepository;

@Service
public class FollowerService {

    private final FollowersRepository followersRepository;
    private final UserRepository userRepository;

    public FollowerService(FollowersRepository followersRepository, UserRepository userRepository) {
        this.followersRepository = followersRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UUID> getFollowedUserIds(UUID followerId) {
        Objects.requireNonNull(followerId, "followerId must not be null");
        return followersRepository.findFollowedIdsByFollowerId(followerId);
    }

    @Transactional(readOnly = true)
    public List<UUID> getFollowerIds(UUID followedId) {
        Objects.requireNonNull(followedId, "followedId must not be null");
        return followersRepository.findFollowerIdsByFollowedId(followedId);
    }

    /**
     * Only active accounts are worth notifying: a follower mid-deletion must not receive new events.
     */
    @Transactional(readOnly = true)
    public List<UUID> getActiveFollowerIds(UUID followedId) {
        Objects.requireNonNull(followedId, "followedId must not be null");
        return followersRepository.findFollowerIdsByFollowedIdWithStatus(followedId, AccountStatus.ACTIVE);
    }

    @Transactional
    public long removeAllForUser(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return followersRepository.deleteAllForUser(userId);
    }

    @Transactional
    public void follow(UUID followerId, UUID followedId) {
        Objects.requireNonNull(followerId, "followerId must not be null");
        Objects.requireNonNull(followedId, "followedId must not be null");

        if (followerId.equals(followedId)) {
            throw new InvalidValueException("A user cannot follow themselves");
        }

        if (!userRepository.existsById(followedId)) {
            throw new InvalidValueException("User to follow does not exist: " + followedId);
        }

        FollowerKey key = new FollowerKey(followerId, followedId);
        if (!followersRepository.existsById(key)) {
            Followers followers = new Followers(followerId, followedId);
            followersRepository.save(followers);
        }
    }

    @Transactional
    public void unfollow(UUID followerId, UUID followedId) {
        Objects.requireNonNull(followerId, "followerId must not be null");
        Objects.requireNonNull(followedId, "followedId must not be null");

        FollowerKey key = new FollowerKey(followerId, followedId);
        if (followersRepository.existsById(key)) {
            followersRepository.deleteById(key);
        }
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(UUID followerId, UUID followedId) {
        if (followerId == null || followedId == null) {
            return false;
        }
        return followersRepository.existsByFollowerIdAndFollowedId(followerId, followedId);
    }

    @Transactional(readOnly = true)
    public long countFollowing(UUID followerId) {
        return followersRepository.countByIdFollowerId(followerId);
    }

    @Transactional(readOnly = true)
    public long countFollowers(UUID followedId) {
        return followersRepository.countByIdFollowedId(followedId);
    }
}
