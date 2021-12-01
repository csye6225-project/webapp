package com.example.demo.repository1;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo1 extends JpaRepository<User,String>  {
    User findUserByUsername(String username);
}
