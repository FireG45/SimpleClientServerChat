package com.example.ServerSideIS.repositories;

import com.example.ServerSideIS.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, Integer> {
    public Optional<User> findUserByUsername(String username);
}
