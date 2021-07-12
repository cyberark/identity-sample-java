package com.idaptive.usermanagement.entity;


import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DBUser {
    private Integer Id;
    private String Name;
    private String Password;
    private String Mail;
    private String DisplayName;
    private String MobileNumber;

    public DBUser() {
    }
    public DBUser(String name, String password, String mail, String displayName, String mobileNumber){
    this.Name = name;
    this.Password = password;
    this.Mail = mail;
    this.DisplayName = displayName;
    this.MobileNumber = mobileNumber;
    }
    public DBUser(Integer id, String name, String password, String mail, String displayName, String mobileNumber){
        this.Id = id;
        this.Name = name;
        this.Password = password;
        this.Mail = mail;
        this.DisplayName = displayName;
        this.MobileNumber = mobileNumber;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return Id;
    }

    public void setId(Integer id){
        this.Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getMail() {
        return Mail;
    }

    public void setMail(String mail) {
        Mail = mail;
    }

    public String getDisplayName() {
        return DisplayName;
    }

    public void setDisplayName(String displayName) {
        DisplayName = displayName;
    }

    public String getMobileNumber() {
        return MobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        MobileNumber = mobileNumber;
    }
}
