package com.example.myapplication;

public class Member {
    private String name;
    private String id;
    private String email;
    private int imageResId;

    public Member(String name, String id, String email, int imageResId) {
        this.name = name;
        this.id = id;
        this.email = email;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }
    public String getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }
    public int getImageResId() {
        return imageResId;
    }
}

