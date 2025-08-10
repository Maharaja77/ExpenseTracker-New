package com.expensetracker.controller;

import com.expensetracker.model.RevokedToken;
import com.expensetracker.model.Role;
import com.expensetracker.model.User;
import com.expensetracker.repository.RevokedTokenRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RevokedTokenRepository revokedTokenRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PostMapping("/register")
	public ResponseEntity<String> registerUser(@RequestBody User user) {
		if (userRepository.findByUsername(user.getUsername()).isPresent()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists!");
		}

		user.setPassword(passwordEncoder.encode(user.getPassword()));

		// Default role assignment
		if (user.getRole() == null) {
			user.setRole(Role.ROLE_USER);
		}

		userRepository.save(user);
		return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody User user) {
		Optional<User> existingUser = userRepository.findByUsername(user.getUsername());

		if (existingUser.isPresent() && passwordEncoder.matches(user.getPassword(), existingUser.get().getPassword())) {
			return ResponseEntity.ok(Map.of("accessToken",
					jwtUtil.generateToken(existingUser.get().getUsername(), existingUser.get().getRole().name()),
					"refreshToken", jwtUtil.generateRefreshToken(existingUser.get().getUsername())));

		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
	}

	@PostMapping("/logout")
	public ResponseEntity<String> logout(@RequestHeader("Authorization") String token,
			@RequestBody Map<String, String> request) {
		token = token.substring(7);
		revokedTokenRepository.save(new RevokedToken(token));

		String refreshToken = request.get("refreshToken");
		if (refreshToken != null) {
			revokedTokenRepository.save(new RevokedToken(refreshToken));
		}

		return ResponseEntity.ok("Logged out successfully.");
	}

	@PostMapping("/refresh")
	public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
		String refreshToken = request.get("refreshToken");

		if (refreshToken == null || jwtUtil.isTokenExpired(refreshToken)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token.");
		}

		String username = jwtUtil.getUsernameFromToken(refreshToken);

		return ResponseEntity.ok(Map.of("accessToken", jwtUtil.generateToken(username, username)));
	}

}
