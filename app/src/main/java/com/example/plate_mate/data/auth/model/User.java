package com.example.plate_mate.data.auth.model;

public class User {
    private String email;
    private String Name;
    private String id;

    public User(String email, String name, String id) {
        this.email = email;
        Name = name;
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
