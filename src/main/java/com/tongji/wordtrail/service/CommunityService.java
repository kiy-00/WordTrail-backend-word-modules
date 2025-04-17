package com.tongji.wordtrail.service;

import com.tongji.wordtrail.dto.CommentRequest;
import com.tongji.wordtrail.dto.PostResponse;
import com.tongji.wordtrail.model.Comment;
import com.tongji.wordtrail.model.Favourite;
import com.tongji.wordtrail.model.Post;
import com.tongji.wordtrail.model.Vote;
import com.tongji.wordtrail.repository.CommentRepository;
import com.tongji.wordtrail.repository.FavouriteRepository;
import com.tongji.wordtrail.repository.PostRepository;
import com.tongji.wordtrail.repository.VoteRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommunityService {
    @Autowired
    private final PostRepository postRepository;
    @Autowired
    private final VoteRepository voteRepository;
    @Autowired
    private final FavouriteRepository favouriteRepository;
    @Autowired
    private final CommentRepository commentRepository;

    @Autowired
    public CommunityService(PostRepository postRepository, VoteRepository voteRepository, FavouriteRepository favouriteRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.voteRepository = voteRepository;
        this.favouriteRepository = favouriteRepository;
        this.commentRepository = commentRepository;
    }

    public Post createPost(String username, String title, String content, List<String> filePaths) {
        // 直接存储字符串路径
        long postCountLong = postRepository.count(); // MongoDB 里统计文档总数
        int postCount = (int) postCountLong;
        // 示例：用帖子总数计算页码（比如每页10个）
        int page = postCount / 10 + 1;
        Post post = new Post(title, content, filePaths, LocalDateTime.now(), LocalDateTime.now(), username, null, 0, 0, page);
        return postRepository.save(post);
    }
    public Optional<Post> getPostById(String id) {
        return postRepository.findById(id);
    }
    public boolean deletePostById(String id) {
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<PostResponse> getPostsByPage(int page) {
        List<Post> posts = postRepository.findByPage(page);
        return posts.stream().map(post -> {
            PostResponse response = new PostResponse();
            response.setId(post.getId());
            response.setTitle(post.getTitle());
            response.setContent(post.getContent());
            response.setCreatedTime(post.getCreatedAt().toString());
            response.setUpdatedTime(post.getUpdatedTime().toString());
            response.setAuthor(post.getAuthor());
            response.setUserAvatarUrl(post.getUserAvatarUrl());
            response.setCommentCount(post.getCommentCount());
            response.setVoteCount(post.getVoteCount());
            return response;
        }).collect(Collectors.toList());
    }
    // 随机获取10条帖子
    public List<PostResponse> getRandomPostResponses() {
        List<Post> posts = postRepository.findRandomPosts(10);  // 随机10条
        return posts.stream().map(post -> {
            PostResponse response = new PostResponse();
            response.setId(post.getId());
            response.setTitle(post.getTitle());
            response.setContent(post.getContent());
            response.setCreatedTime(post.getCreatedAt().toString());
            response.setUpdatedTime(post.getUpdatedTime().toString());
            response.setAuthor(post.getAuthor());
            response.setUserAvatarUrl(post.getUserAvatarUrl());
            response.setCommentCount(post.getCommentCount());
            response.setVoteCount(post.getVoteCount());
            return response;
        }).collect(Collectors.toList());
    }
    public long getPostCount() {
        return postRepository.count();
    }
    public List<PostResponse> getPostsByUserPage(String author, int page) {
        List<Post> posts = postRepository.findByAuthorAndPage(author, page);
        return posts.stream().map(post -> {
            PostResponse response = new PostResponse();
            response.setId(post.getId());
            response.setTitle(post.getTitle());
            response.setContent(post.getContent());
            response.setCreatedTime(post.getCreatedAt().toString());
            response.setUpdatedTime(post.getUpdatedTime().toString());
            response.setAuthor(post.getAuthor());
            response.setUserAvatarUrl(post.getUserAvatarUrl());
            response.setCommentCount(post.getCommentCount());
            response.setVoteCount(post.getVoteCount());
            return response;
        }).collect(Collectors.toList());
    }
    public List<PostResponse> getPostSearch(String keyword, int page) {
        List<Post> posts = postRepository.findByKeywordAndPage(keyword, page);
        return posts.stream().map(post -> {
            PostResponse response = new PostResponse();
            response.setId(post.getId());
            response.setTitle(post.getTitle());
            response.setContent(post.getContent());
            response.setCreatedTime(post.getCreatedAt().toString());
            response.setUpdatedTime(post.getUpdatedTime().toString());
            response.setAuthor(post.getAuthor());
            response.setUserAvatarUrl(post.getUserAvatarUrl());
            response.setCommentCount(post.getCommentCount());
            response.setVoteCount(post.getVoteCount());
            return response;
        }).collect(Collectors.toList());
    }
    public int getPostSearchCount(String keyword) {
        List<Post> posts = postRepository.findByKeyword(keyword);
        return posts.size();
    }
    public int getPostVoteCount(String postId, String userId, String upvote) {
        Post post = postRepository.findById(postId).get();
        Vote vote = new Vote(LocalDateTime.now(), userId, upvote, postId);
        voteRepository.save(vote);
        if (Objects.equals(upvote, "true")) {
            post.setVoteCount(post.getVoteCount() + 1);
            postRepository.save(post);
        }
        else if(Objects.equals(upvote, "false")) {
            post.setVoteCount(post.getVoteCount() - 1);
            postRepository.save(post);
        }
        else if(upvote == null) {
            if(voteRepository.existsByUserId(userId)) {
                if(vote.getUpvote().equals("true")) {
                    post.setVoteCount(post.getVoteCount() - 1);
                    postRepository.save(post);
                }
                else if(vote.getUpvote().equals("false")) {
                    post.setVoteCount(post.getVoteCount() + 1);
                    postRepository.save(post);
                }
            }
        }
        return post.getVoteCount();
    }
    public int getPostVote(String postId, String userId) {
        Vote vote = voteRepository.findByPostIdAndUserId(postId, userId).get(0);
        if (vote.getUpvote().equals("true")) {
            return 1;
        }
        else if(vote.getUpvote().equals("false")) {
            return -1;
        }
        else{
            return 0;
        }

    }
    public int getPostVoteCount(String postId) {
        return voteRepository.findByPostId(postId).size();
    }
    public void addFavourite(String postId, String userId) {
        Favourite favourite = new Favourite(userId, postId, LocalDateTime.now(), LocalDateTime.now());
        favouriteRepository.save(favourite);
    }
    public boolean deleteFavourite(String postId, String userId) {
        if (favouriteRepository.existsByPostIdAndUserId(postId, userId)) {
            favouriteRepository.deleteByPostIdAndUserId(postId, userId);
            return true;
        }
        else {
            return false;
        }
    }
    public List<Favourite> getFavoriteList(String postId, String userId) {
        List<Favourite> favourites = favouriteRepository.findByPostIdAndUserId(postId, userId);
        return favourites;
    }
    public void addComment(CommentRequest request) {
        Comment comment = new Comment(request.getPostId(), request.getContent(), request.getUserId(), LocalDateTime.now(), LocalDateTime.now(), request.getParentComment(), request.getReplyToName(), request.isMyComment(), request.getAvatar());
        commentRepository.save(comment);
    }
    public void deleteComment(String CommentId) {
        Comment comment = commentRepository.findById(CommentId).get();
        commentRepository.delete(comment);
    }
    public List<Comment> getCommentList(String postId) {
        return commentRepository.findByPostId(postId);
    }
}
