package com.expensetracker.security;

import com.expensetracker.model.User;
import com.expensetracker.repository.RevokedTokenRepository;
import com.expensetracker.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RevokedTokenRepository revokedTokenRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String token = request.getHeader("Authorization");

		// Check if the Authorization header contains a Bearer token
		if (token != null && token.startsWith("Bearer ")) {
			token = token.substring(7); // Remove "Bearer " prefix

			// Check if the token is expired
			if (jwtUtil.isTokenExpired(token)) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("Invalid or expired token.");
				return;
			}

			// Check if the token has been revoked
			if (revokedTokenRepository.findById(token).isPresent()) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("Token has been revoked.");
				return;
			}

			// Get the username and role from the token
			String username = jwtUtil.getUsernameFromToken(token);
			String role = jwtUtil.getRoleFromToken(token);

			// Find the user from the database using the username
			Optional<User> user = userRepository.findByUsername(username);

			if (user.isPresent()) {
				// Create an authority using the role directly from the enum
				List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

				// Create an Authentication object and set it in the SecurityContext
				SecurityContextHolder.getContext()
						.setAuthentication(new UsernamePasswordAuthenticationToken(username, null, authorities));

				// Log the authorities for debugging
				System.out.println("Authorities: " + authorities);
			}
		}

		// Continue the filter chain
		chain.doFilter(request, response);
	}
}
