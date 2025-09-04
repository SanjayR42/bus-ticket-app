package com.bus.reservation.repository;

import com.bus.reservation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    
    Boolean existsByPhone(String phone);
    
    Optional<User> findByPhone(String phone);
    List<User> findByRole(User.Role role);
}