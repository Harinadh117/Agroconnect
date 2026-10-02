package com.agroconnect.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.agroconnect.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics() {
        return ResponseEntity.ok(adminService.getAnalytics());
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestParam(required = false) String role) {
        return ResponseEntity.ok(adminService.getAllUsers(role));
    }

    @PutMapping("/users/{userId}/suspend")
    public ResponseEntity<?> suspendUser(@PathVariable Long userId) {
        try {
            adminService.suspendUser(userId);
            return ResponseEntity.ok(Map.of("message", "User suspension toggled"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            adminService.deleteUser(userId);
            return ResponseEntity.ok(Map.of("message", "User deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/jobs")
    public ResponseEntity<?> getAllJobs() {
        return ResponseEntity.ok(adminService.getAllJobs());
    }

    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<?> removeJob(@PathVariable Long jobId) {
        try {
            adminService.removeJob(jobId);
            return ResponseEntity.ok(Map.of("message", "Job removed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/land")
    public ResponseEntity<?> getAllLand() {
        return ResponseEntity.ok(adminService.getAllLand());
    }

    @DeleteMapping("/land/{landId}")
    public ResponseEntity<?> removeLand(@PathVariable Long landId) {
        try {
            adminService.removeLand(landId);
            return ResponseEntity.ok(Map.of("message", "Land listing removed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
