package com.mar.web.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "user")
public class User {
    @Id
    @GeneratedValue
    private int id;

    private String name;
    private String password;
}
