package com.hotel.hms.Service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.hotel.hms.Entity.User;
import com.hotel.hms.Exception.ConflictException;
import com.hotel.hms.Exception.ResourceNotFoundException;
import com.hotel.hms.Exception.UnauthorizedException;
import com.hotel.hms.Repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(
            UserRepository userRepository,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtService = jwtService;
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    // Create or update user
    public User saveUser(User user) {

        // New user
        if (user.getId() == null) {

            user.setPassword(
                    passwordEncoder.encode(user.getPassword())
            );

            return userRepository.save(user);
        }

        // Existing user
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setRole(user.getRole());

        // Update password only if a new password is provided
        if (user.getPassword() != null &&
                !user.getPassword().isBlank()) {

            existingUser.setPassword(
                    passwordEncoder.encode(user.getPassword())
            );
        }

        return userRepository.save(existingUser);
    }

    // Register new USER
    public User registerUser(User user) {

        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new ConflictException(
                    "Email is already registered"
            );
        }

        // Always create normal USER account
        user.setRole("USER");

        // Encrypt password
        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        return userRepository.save(user);
    }

    // Delete user
    public void deleteUser(Integer id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        userRepository.delete(user);
    }

    // Login
    public String login(String email, String password) {

        User user = userRepository.findByEmail(email);

        if (user == null ||
                !passwordEncoder.matches(
                        password,
                        user.getPassword())) {

            throw new UnauthorizedException(
                    "Invalid email or password");
        }

        return jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );
    }
}