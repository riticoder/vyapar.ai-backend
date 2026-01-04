package com.Nxtwave_X_openAI_buildathon.vyapar.ai_backend.login.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.Nxtwave_X_openAI_buildathon.vyapar.ai_backend.login.dto.AuthResponse;
import com.Nxtwave_X_openAI_buildathon.vyapar.ai_backend.login.dto.GoogleLoginRequest;
import com.Nxtwave_X_openAI_buildathon.vyapar.ai_backend.login.model.User;
import com.Nxtwave_X_openAI_buildathon.vyapar.ai_backend.login.repository.UserRepository;
import com.Nxtwave_X_openAI_buildathon.vyapar.ai_backend.login.service.AuthService;
import com.Nxtwave_X_openAI_buildathon.vyapar.ai_backend.login.service.JwtService;
import com.Nxtwave_X_openAI_buildathon.vyapar.ai_backend.login.util.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import jakarta.validation.Valid;

/**
 * Login controller with both traditional and OAuth2 support.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtService jwtService;

    @Value("${app.googleClientId:}")
    private String googleClientId;

    public AuthController(AuthService authService, UserRepository userRepository, 
                         PasswordEncoder passwordEncoder, JwtUtil jwtUtil, JwtService jwtService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        
        if (email == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "email and password required"));
        }

        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid credentials"));
        }

        User user = userOpt.get();
        
        // Generate JWT tokens
        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        
        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, user.getEmail()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null || name == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "name, email and password required"));
        }

        // Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "email already exists"));
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(name);
        user.setProvider("LOCAL");
        user.setProviderId(null);
        user.setEnabled(true);
        userRepository.save(user);

        // Generate JWT tokens for immediate login after registration
        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, user.getEmail()));
    }

    @GetMapping("/users/emails")
    public ResponseEntity<?> getAllEmails() {
        List<User> users = userRepository.findAll();
        List<Map<String, String>> emails = users.stream()
                .filter(user -> user.getEmail() != null && !user.getEmail().isEmpty())
                .map(user -> {
                    Map<String, String> emailInfo = new HashMap<>();
                    emailInfo.put("email", user.getEmail());
                    emailInfo.put("name", user.getFullName() != null ? user.getFullName() : user.getUsername());
                    emailInfo.put("provider", user.getProvider() != null ? user.getProvider() : "LOCAL");
                    return emailInfo;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of("emails", emails));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // In a stateless JWT system, logout is handled client-side by removing the token
        // But we can track logout events, invalidate refresh tokens, etc.
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // Google One Tap - uses Google API client verification
    @PostMapping("/google-one-tap")
    public ResponseEntity<?> googleOneTap(@RequestBody Map<String, String> body) {
        String credential = body.get("credential");
        if (credential == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "credential required"));
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(java.util.Collections.singletonList(
                    "914271961036-1l55qu90ll7sdamrjht0p8g75gocadft.apps.googleusercontent.com"))
                .build();

            GoogleIdToken idToken = verifier.verify(credential);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String sub = payload.getSubject();

                // Find or create user
                Optional<User> userOpt = userRepository.findByEmail(email);
                User user;
                
                if (userOpt.isEmpty()) {
                    user = new User();
                    user.setUsername(email);
                    user.setEmail(email);
                    user.setFullName(name != null ? name : email);
                    user.setProvider("GOOGLE");
                    user.setProviderId(sub);
                    user.setPassword(new BCryptPasswordEncoder().encode("GOOGLE_ONE_TAP"));
                    userRepository.save(user);
                } else {
                    user = userOpt.get();
                }

                // Generate tokens using JwtService
                String accessToken = jwtService.generateAccessToken(user.getEmail());
                String refreshToken = jwtService.generateRefreshToken(user.getEmail());
                
                return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, user.getEmail()));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "invalid token"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "verification failed: " + e.getMessage()));
        }
    }

    // Google Login - uses REST call to Google's tokeninfo endpoint
    @PostMapping("/google-login")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        try {
            RestTemplate rt = new RestTemplate();
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getIdToken();
            Map<String, Object> payload = rt.getForObject(url, Map.class);
            
            if (payload == null || payload.get("email") == null) {
                throw new RuntimeException("Invalid Google ID token");
            }

            // validate audience if configured
            if (googleClientId != null && !googleClientId.isBlank()) {
                Object aud = payload.get("aud");
                if (aud == null || !googleClientId.equals(String.valueOf(aud))) {
                    throw new RuntimeException("Invalid Google client ID");
                }
            }

            Object emailVerified = payload.get("email_verified");
            if (emailVerified == null || !(String.valueOf(emailVerified).equalsIgnoreCase("true") || String.valueOf(emailVerified).equalsIgnoreCase("1"))) {
                throw new RuntimeException("Google email not verified");
            }

            String email = String.valueOf(payload.get("email"));
            String name = payload.get("name") != null ? String.valueOf(payload.get("name")) : email;
            String googleId = payload.get("sub") != null ? String.valueOf(payload.get("sub")) : null;

            // Find or create user
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                user = new User();
                user.setUsername(email);
                user.setEmail(email);
                user.setFullName(name);
                user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                user.setProvider("GOOGLE");
                user.setProviderId(googleId);
                userRepository.save(user);
            }

            // Generate tokens using JwtService
            String accessToken = jwtService.generateAccessToken(user.getEmail());
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());

            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, user.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
