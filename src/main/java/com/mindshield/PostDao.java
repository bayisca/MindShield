package com.mindshield.dao;

import com.mindshield.models.BlogPost;
import java.util.List;

public interface PostDao {
    void save(BlogPost post);
    void update();
    BlogPost findById(String id);
    List<BlogPost> findAll();
    List<BlogPost> searchByTitleOrContent(String searchTerm);
}
