package com.example.catalog.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.catalog.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
