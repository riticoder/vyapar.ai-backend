package com.Nxtwave_X_openAI_buildathon.vyapar.ai_backend.login.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.Nxtwave_X_openAI_buildathon.vyapar.ai_backend.login.model.User;
import com.Nxtwave_X_openAI_buildathon.vyapar.ai_backend.login.repository.UserRepository;
import com.Nxtwave_X_openAI_buildathon.vyapar.ai_backend.login.util.JwtUtil;

/**
 * Authentication service inside login module.
 * Uses BCrypt for password verification and issues JWT tokens (18h expiry by config).
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticate user and return JWT token when successful.
     * If stored password is plain-text (legacy seed), migrate it to a bcrypt hash.
     */
    public Optional<String> login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User u = userOpt.get();
            String stored = u.getPassword();
            boolean ok = false;
            if (stored != null && stored.startsWith("$2")) {
                // bcrypt hash
                ok = passwordEncoder.matches(password, stored);
            } else {
                // legacy plain-text - accept and upgrade to bcrypt
                ok = stored != null && stored.equals(password);
                if (ok) {
                    u.setPassword(passwordEncoder.encode(password));
                    userRepository.save(u);
                }
            }

            if (ok) {
                String token = jwtUtil.generateToken(username);
                return Optional.of(token);
            }
        }
        return Optional.empty();
    }

    public Optional<String> validateToken(String token) {
        if (token == null) return Optional.empty();
        // token may be in form "Bearer <token>" - sanitize
        String t = token.startsWith("Bearer ") ? token.substring(7) : token;
        return jwtUtil.validateTokenAndGetSubject(t);
    }
}
