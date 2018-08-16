package com.mar.web.dao;

import com.mar.web.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends JpaRepository<User,String> {
    boolean findAllById(int id);
    boolean findByName(String name);

}
