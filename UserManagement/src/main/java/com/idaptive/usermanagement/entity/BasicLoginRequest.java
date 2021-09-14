package com.idaptive.usermanagement.entity;

public class BasicLoginRequest {

    private String Username;
    public char[] Password;

    public BasicLoginRequest(String username, char[] password) {
        Username = username;
        Password = password;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public char[] getPassword() {
        return Password;
    }

    public void setPassword(char[] password) {
        Password = password;
    }
}
