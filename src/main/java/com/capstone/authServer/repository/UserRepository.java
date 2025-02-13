package com.capstone.authServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.capstone.authServer.model.User;

public interface UserRepository extends JpaRepository<User, String> {
    // googleId is the PK, so the ID type is String
    User findByEmail(String email);
}
