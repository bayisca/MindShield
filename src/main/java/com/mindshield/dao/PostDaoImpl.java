package com.mindshield.dao;

import com.mindshield.models.BlogPost;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PostDaoImpl implements PostDao {
    private List<BlogPost> dbMock;
    private static final String DATA_FILE = "posts.dat";

    public PostDaoImpl() {
        this.dbMock = loadPosts();
    }

    @Override
    public void save(BlogPost post) {
        dbMock.add(post);
        savePosts();
    }

    public void update() {
        // Called when an existing post is modified (e.g. comment added, published)
        savePosts();
    }

    @Override
    public BlogPost findById(String id) {
        return dbMock.stream()
                .filter(post -> post.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<BlogPost> findAll() {
        return new ArrayList<>(dbMock);
    }

    @Override
    public List<BlogPost> searchByTitleOrContent(String searchTerm) {
        return dbMock.stream()
                .filter(post -> post.containsInTitle(searchTerm) || post.containsInBody(searchTerm))
                .collect(Collectors.toList());
    }

    private void savePosts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(dbMock);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private List<BlogPost> loadPosts() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (List<BlogPost>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }
}
