package com.github.rodrigo_sp17.mscheduler.user.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Embeddable;

@Embeddable
public class UserInfo {

    private String name;

    private String userName;

    private String email;

    @JsonIgnore
    private String password;


    public UserInfo() {
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return (name + ", " + userName + ", " + email + ", " + password);
    }
}
