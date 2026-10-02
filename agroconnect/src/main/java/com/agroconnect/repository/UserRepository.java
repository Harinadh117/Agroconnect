package com.agroconnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agroconnect.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    List<User> findByRole(User.Role role);
    List<User> findByRoleAndSuspendedFalse(User.Role role);
    List<User> findByRoleAndAvailableDaysGreaterThan(User.Role role, int days);
    List<User> findByRoleAndDistrictAndAvailableDaysGreaterThan(User.Role role, String district, int days);
    long countByRole(User.Role role);
}
