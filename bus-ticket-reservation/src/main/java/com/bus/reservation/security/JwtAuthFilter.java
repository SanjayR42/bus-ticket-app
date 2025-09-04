package com.bus.reservation.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    
    public JwtAuthFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain)
            throws ServletException, IOException {
        
        final String servletPath = request.getServletPath();
        final String requestURI = request.getRequestURI();
        final String contextPath = request.getContextPath();
        
        log.debug("Servlet Path: {}", servletPath);
        log.debug("Request URI: {}", requestURI);
        log.debug("Context Path: {}", contextPath);
        
        // Remove context path from requestURI for matching
        String pathToCheck = requestURI;
        if (contextPath != null && !contextPath.isEmpty() && requestURI.startsWith(contextPath)) {
            pathToCheck = requestURI.substring(contextPath.length());
        }
        
        log.debug("Path to check: {}", pathToCheck);
        
        // Skip JWT processing for auth endpoints and public endpoints
        if (pathToCheck.startsWith("/api/v1/auth/") || 
            pathToCheck.contains("/swagger-ui") || 
            pathToCheck.contains("/v3/api-docs") ||
            pathToCheck.equals("/error")) {
            log.debug("Skipping JWT filter for public endpoint: {}", pathToCheck);
            filterChain.doFilter(request, response);
            log.debug("=== JWT FILTER END (SKIPPED) ===");
            return;
        }

        log.debug("PROCESSING JWT FILTER - Protected endpoint: {}", pathToCheck);
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Check if Authorization header is present
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Authorization header found, continuing chain");
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token
        jwt = authHeader.substring(7);
        log.debug("JWT token extracted: {}", jwt.substring(0, Math.min(20, jwt.length())) + "...");
        
        try {
            // Extract username from JWT
            userEmail = jwtUtils.extractUsername(jwt);
            log.debug("Extracted username from JWT: {}", userEmail);
            
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.debug("Loading user details for: {}", userEmail);
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                
                if (jwtUtils.isTokenValid(jwt, userDetails.getUsername())) {
                    log.debug("Token is valid for user: {}", userEmail);
                    
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authentication set in SecurityContext");
                } else {
                    log.warn("Token is invalid for user: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage());
            // Don't stop the filter chain - let Spring Security handle the authentication failure
        }
        
        filterChain.doFilter(request, response);
    }
}