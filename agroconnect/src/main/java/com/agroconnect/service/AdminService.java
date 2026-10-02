package com.agroconnect.service;

import java.util.*;

import org.springframework.stereotype.Service;

import com.agroconnect.model.*;
import com.agroconnect.repository.*;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final LandListingRepository landRepository;

    public AdminService(UserRepository userRepository, JobRepository jobRepository,
                        LandListingRepository landRepository) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.landRepository = landRepository;
    }

    public Map<String, Object> getAnalytics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalFarmers", userRepository.countByRole(User.Role.FARMER));
        stats.put("totalWorkers", userRepository.countByRole(User.Role.WORKER));
        stats.put("totalKavulu", userRepository.countByRole(User.Role.KAVULU));
        stats.put("activeJobs", jobRepository.count());
        stats.put("activeLand", landRepository.count());
        return stats;
    }

    public List<Map<String, Object>> getAllUsers(String role) {
        List<User> users;
        if (role != null && !role.isEmpty()) {
            users = userRepository.findByRole(User.Role.valueOf(role.toUpperCase()));
        } else {
            users = userRepository.findAll();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", user.getId());
            map.put("name", user.getName());
            map.put("phone", user.getPhone());
            map.put("role", user.getRole().name());
            map.put("village", user.getVillage());
            map.put("district", user.getDistrict());
            map.put("verified", user.getVerified());
            map.put("suspended", user.getSuspended());
            map.put("createdAt", user.getCreatedAt());
            result.add(map);
        }
        return result;
    }

    public void suspendUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setSuspended(!user.getSuspended());
        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
    }

    public List<Map<String, Object>> getAllJobs() {
        List<Job> jobs = jobRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Job job : jobs) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("jobId", job.getJobId());
            map.put("workType", job.getWorkType());
            map.put("durationDays", job.getDurationDays());
            map.put("village", job.getVillage());
            map.put("district", job.getDistrict());
            map.put("farmerName", job.getFarmer().getName());
            map.put("createdAt", job.getCreatedAt());
            result.add(map);
        }
        return result;
    }

    public void removeJob(Long jobId) {
        jobRepository.deleteById(jobId);
    }

    public List<Map<String, Object>> getAllLand() {
        List<LandListing> lands = landRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (LandListing land : lands) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("landId", land.getLandId());
            map.put("village", land.getVillage());
            map.put("district", land.getDistrict());
            map.put("area", land.getArea());
            map.put("cropType", land.getCropType());
            map.put("farmerName", land.getFarmer().getName());
            map.put("createdAt", land.getCreatedAt());
            result.add(map);
        }
        return result;
    }

    public void removeLand(Long landId) {
        landRepository.deleteById(landId);
    }
}
