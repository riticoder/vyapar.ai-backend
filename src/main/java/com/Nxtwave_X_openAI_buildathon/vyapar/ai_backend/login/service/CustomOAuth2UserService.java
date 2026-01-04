package com.Nxtwave_X_openAI_buildathon.vyapar.ai_backend.login.service;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.Nxtwave_X_openAI_buildathon.vyapar.ai_backend.login.model.User;
import com.Nxtwave_X_openAI_buildathon.vyapar.ai_backend.login.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub"); // Google's unique ID
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"

        // Find or create user - use email as unique identifier
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        
        if (userOptional.isEmpty()) {
            // Create new user with email as username
            user = new User();
            user.setUsername(email); // Email is username for Google users
            user.setEmail(email);
            user.setFullName(name != null ? name : email);
            user.setProvider("GOOGLE");
            user.setProviderId(providerId);
            user.setPassword(new BCryptPasswordEncoder().encode("GOOGLE_OAUTH_USER")); // Dummy password
            userRepository.save(user);
        } else {
            user = userOptional.get();
            // Update user info from Google if needed
            boolean updated = false;
            if (!"GOOGLE".equals(user.getProvider())) {
                user.setProvider("GOOGLE");
                user.setProviderId(providerId);
                updated = true;
            }
            if (name != null && !name.equals(user.getFullName())) {
                user.setFullName(name);
                updated = true;
            }
            if (updated) {
                userRepository.save(user);
            }
        }

        return oAuth2User;
    }
}
