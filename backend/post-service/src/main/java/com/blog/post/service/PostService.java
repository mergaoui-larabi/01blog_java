package com.blog.post.service;

import com.blog.post.domain.entity.Post;
import com.blog.post.domain.valueobject.AuthorId;
import com.blog.post.domain.valueobject.MediaId;
import com.blog.post.domain.valueobject.PostId;
import com.blog.post.domain.valueobject.UserId;
import com.blog.post.exception.PostNotFoundException;
import com.blog.post.exception.UserNotFollowingException;
import com.blog.post.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public PostRepository getPostRepository() {
        return postRepository;
    }

    @Transactional(readOnly = true)
    public List<Post> getFeed(AuthorId authorId, List<AuthorId> followingAuthorIds) {
        if (followingAuthorIds == null) {
            followingAuthorIds = List.of();
        }
        return postRepository.findFeedPosts(authorId, followingAuthorIds);
    }

    @Transactional(readOnly = true)
    public List<Post> getPostsByAuthor(AuthorId authorId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
    }

    @Transactional(readOnly = true)
    public Post getPostById(PostId postId) {
        return postRepository.findByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));
    }

    @Transactional(readOnly = true)
    public boolean isPostOwnedBy(PostId postId, AuthorId authorId) {
        return postRepository.findByIdAndAuthorId(postId.value(), authorId).isPresent();
    }
}
