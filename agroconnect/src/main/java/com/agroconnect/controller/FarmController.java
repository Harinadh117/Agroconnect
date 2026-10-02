package com.agroconnect.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.agroconnect.model.User;
import com.agroconnect.repository.UserRepository;
import com.agroconnect.service.FarmService;

@RestController
@RequestMapping("/api/farm")
public class FarmController {

    private final FarmService farmService;
    private final UserRepository userRepository;

    public FarmController(FarmService farmService, UserRepository userRepository) {
        this.farmService = farmService;
        this.userRepository = userRepository;
    }

    // ===== JOBS =====

    @PostMapping("/jobs")
    public ResponseEntity<?> postJob(@RequestBody Map<String, String> body) {
        try {
            farmService.postJob(getCurrentUserId(), body);
            return ResponseEntity.ok(Map.of("message", "Job posted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/jobs")
    public ResponseEntity<?> getAllJobs(@RequestParam(required = false) String district) {
        return ResponseEntity.ok(farmService.getAllJobs(district));
    }

    @GetMapping("/my-jobs")
    public ResponseEntity<?> getMyJobs() {
        return ResponseEntity.ok(farmService.getMyJobs(getCurrentUserId()));
    }

    @PostMapping("/jobs/{jobId}/apply")
    public ResponseEntity<?> applyForJob(@PathVariable Long jobId) {
        try {
            farmService.applyForJob(jobId, getCurrentUserId());
            return ResponseEntity.ok(Map.of("message", "Applied successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-applications")
    public ResponseEntity<?> getMyApplications() {
        try {
            return ResponseEntity.ok(farmService.getMyApplications(getCurrentUserId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/applications/{appId}/select")
    public ResponseEntity<?> selectWorker(@PathVariable Long appId) {
        try {
            farmService.selectWorker(appId, getCurrentUserId());
            return ResponseEntity.ok(Map.of("message", "Worker selected"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/applications/{appId}/reject")
    public ResponseEntity<?> rejectWorker(@PathVariable Long appId) {
        try {
            farmService.rejectWorker(appId, getCurrentUserId());
            return ResponseEntity.ok(Map.of("message", "Worker rejected"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===== LAND =====

    @PostMapping("/land")
    public ResponseEntity<?> postLand(@RequestBody Map<String, String> body) {
        try {
            farmService.postLand(getCurrentUserId(), body);
            return ResponseEntity.ok(Map.of("message", "Land listing posted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/land")
    public ResponseEntity<?> getAllLand(@RequestParam(required = false) String district) {
        return ResponseEntity.ok(farmService.getAllLand(district));
    }

    @GetMapping("/my-land")
    public ResponseEntity<?> getMyLand() {
        return ResponseEntity.ok(farmService.getMyLand(getCurrentUserId()));
    }

    @PostMapping("/land/{landId}/request")
    public ResponseEntity<?> requestLand(@PathVariable Long landId) {
        try {
            farmService.requestLand(landId, getCurrentUserId());
            return ResponseEntity.ok(Map.of("message", "Land request submitted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/land-requests/{reqId}/approve")
    public ResponseEntity<?> approveLandRequest(@PathVariable Long reqId) {
        try {
            farmService.approveLandRequest(reqId, getCurrentUserId());
            return ResponseEntity.ok(Map.of("message", "Request approved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/land-requests/{reqId}/reject")
    public ResponseEntity<?> rejectLandRequest(@PathVariable Long reqId) {
        try {
            farmService.rejectLandRequest(reqId, getCurrentUserId());
            return ResponseEntity.ok(Map.of("message", "Request rejected"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===== WORKERS =====

    @GetMapping("/workers")
    public ResponseEntity<?> getAvailableWorkers(@RequestParam(required = false) String district) {
        return ResponseEntity.ok(farmService.getAvailableWorkers(district));
    }

    @PostMapping("/hire-worker/{workerId}")
    public ResponseEntity<?> hireWorker(@PathVariable Long workerId, @RequestBody Map<String, Integer> body) {
        try {
            Integer durationDays = body.getOrDefault("durationDays", 1);
            farmService.hireWorkerDirectly(getCurrentUserId(), workerId, durationDays);
            return ResponseEntity.ok(Map.of("message", "Worker hired successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/worker/availability")
    public ResponseEntity<?> updateAvailability(@RequestBody Map<String, String> body) {
        try {
            farmService.updateAvailability(getCurrentUserId(), Integer.parseInt(body.get("availableDays")));
            return ResponseEntity.ok(Map.of("message", "Availability updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===== USER PROFILE =====

    @GetMapping("/profile/current")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            Long userId = getCurrentUserId();
            return ResponseEntity.ok(farmService.getCurrentUserProfile(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> body) {
        try {
            User updated = farmService.updateUserProfile(getCurrentUserId(), body);
            return ResponseEntity.ok(farmService.getCurrentUserProfile(updated.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByPhone(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
