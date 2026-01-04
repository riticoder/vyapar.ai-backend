package com.Nxtwave_X_openAI_buildathon.vyapar.ai_backend.login.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Nxtwave_X_openAI_buildathon.vyapar.ai_backend.login.model.User;

/**
 * Repository for login users.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
