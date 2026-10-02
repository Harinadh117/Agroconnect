package com.agroconnect.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.agroconnect.model.User;
import com.agroconnect.repository.UserRepository;

@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getSuspended()) {
            throw new UsernameNotFoundException("Account suspended");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getPhone(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }

    public User register(Map<String, String> body) {
        String phone = body.get("phone");
        String name = body.get("name");
        String password = body.get("password");
        String roleStr = body.getOrDefault("role", "FARMER").toUpperCase();

        // Validate required fields
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Name is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (phone == null || phone.length() != 10) {
            throw new RuntimeException("Phone must be exactly 10 digits");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("Phone number already registered");
        }

        // Validate role
        User.Role role;
        try {
            role = User.Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role. Must be FARMER, WORKER, KAVULU, or ADMIN");
        }

        User user = new User();
        user.setName(name.trim());
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setVillage(body.get("village"));
        user.setDistrict(body.get("district"));
        user.setState(body.get("state"));
        user.setVerified(true);

        String roleName = role.name();
        if ("WORKER".equals(roleName)) {
            user.setWorkType(body.get("workType"));
            user.setExperience(body.get("experience"));
            String days = body.get("availableDays");
            user.setAvailableDays(days != null ? Integer.parseInt(days) : 0);
        } else if ("KAVULU".equals(roleName)) {
            user.setPreferredCrop(body.get("preferredCrop"));
            user.setExperience(body.get("experience"));
        } else if ("FARMER".equals(roleName)) {
            user.setPreferredCrop(body.get("preferredCrop"));
        }

        return userRepository.save(user);
    }

    public Map<String, Object> getCurrentUser(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("phone", user.getPhone());
        map.put("role", user.getRole().name());
        map.put("village", user.getVillage());
        map.put("district", user.getDistrict());
        map.put("state", user.getState());
        map.put("verified", user.getVerified());

        if (user.getRole() == User.Role.WORKER) {
            map.put("workType", user.getWorkType());
            map.put("experience", user.getExperience());
            map.put("availableDays", user.getAvailableDays());
            map.put("availableFrom", user.getAvailableFrom());
        } else if (user.getRole() == User.Role.KAVULU) {
            map.put("preferredCrop", user.getPreferredCrop());
            map.put("experience", user.getExperience());
        }

        return map;
    }
}
