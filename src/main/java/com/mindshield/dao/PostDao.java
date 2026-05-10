package com.mindshield.dao;

import com.mindshield.models.BlogPost;
import java.util.List;

public interface PostDao {
    void save(BlogPost post);
    void update();
    void deleteById(String id);
    BlogPost findById(String id);
    List<BlogPost> findAll();
    List<BlogPost> searchByTitleOrContent(String searchTerm);

    // Favori blog işlemleri
    boolean isFavoriteBlog(String userId, String postId);
    void addFavoriteBlog(String userId, String postId);
    void removeFavoriteBlog(String userId, String postId);
    List<BlogPost> getFavoriteBlogs(String userId);
    void deleteAllFavoriteBlogsForUser(String userId);
}
