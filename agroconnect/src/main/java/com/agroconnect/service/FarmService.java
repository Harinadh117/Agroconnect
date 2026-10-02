package com.agroconnect.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agroconnect.model.Job;
import com.agroconnect.model.JobApplication;
import com.agroconnect.model.LandListing;
import com.agroconnect.model.LandRequest;
import com.agroconnect.model.User;
import com.agroconnect.repository.JobApplicationRepository;
import com.agroconnect.repository.JobRepository;
import com.agroconnect.repository.LandListingRepository;
import com.agroconnect.repository.LandRequestRepository;
import com.agroconnect.repository.UserRepository;

@Service
public class FarmService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository appRepository;
    private final LandListingRepository landRepository;
    private final LandRequestRepository landRequestRepository;
    private final UserRepository userRepository;

    public FarmService(JobRepository jobRepository, JobApplicationRepository appRepository,
                       LandListingRepository landRepository, LandRequestRepository landRequestRepository,
                       UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.appRepository = appRepository;
        this.landRepository = landRepository;
        this.landRequestRepository = landRequestRepository;
        this.userRepository = userRepository;
    }

    // ===== JOBS =====

    public Job postJob(Long farmerId, Map<String, String> body) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        Job job = new Job();
        job.setFarmer(farmer);
        job.setWorkType(body.get("workType"));
        job.setDurationDays(Integer.parseInt(body.get("durationDays")));
        job.setWorkersRequired(Integer.parseInt(body.getOrDefault("workersRequired", "1")));
        job.setVillage(body.getOrDefault("village", farmer.getVillage()));
        job.setDistrict(body.getOrDefault("district", farmer.getDistrict()));
        job.setState(body.getOrDefault("state", farmer.getState()));
        job.setExperienceRequired(body.get("experienceRequired"));

        return jobRepository.save(job);
    }

    public List<Map<String, Object>> getAllJobs(String district) {
        List<Job> jobs = (district != null && !district.isEmpty())
                ? jobRepository.findByDistrictAndStatus(district, Job.Status.OPEN)
                : jobRepository.findByStatus(Job.Status.OPEN);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Job job : jobs) {
            result.add(jobToMap(job, false));
        }
        return result;
    }

    public List<Map<String, Object>> getMyJobs(Long farmerId) {
        List<Job> jobs = jobRepository.findByFarmerId(farmerId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Job job : jobs) {
            Map<String, Object> map = jobToMap(job, true);
            List<Map<String, Object>> applications = new ArrayList<>();
            List<Map<String, Object>> directHires = new ArrayList<>();

            for (JobApplication app : appRepository.findByJobJobId(job.getJobId())) {
                Map<String, Object> a = new LinkedHashMap<>();
                a.put("applicationId", app.getApplicationId());
                a.put("workerName", app.getWorker().getName());
                a.put("workerId", app.getWorker().getId());
                a.put("experience", app.getWorker().getExperience());
                a.put("status", app.getStatus().name());
                a.put("type", app.getType().name());
                if (app.getStatus() == JobApplication.Status.SELECTED) {
                    a.put("phone", app.getWorker().getPhone());
                }

                if (app.getType() == JobApplication.Type.APPLIED) {
                    applications.add(a);
                } else {
                    directHires.add(a);
                }
            }
            map.put("applications", applications);
            map.put("directHires", directHires);
            result.add(map);
        }
        return result;
    }

    public void applyForJob(Long jobId, Long workerId) {
        if (appRepository.existsByJobJobIdAndWorkerId(jobId, workerId)) {
            throw new RuntimeException("Already applied for this job");
        }
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (job.getStatus() == Job.Status.FILLED) {
            throw new RuntimeException("This job is already filled");
        }

        if (worker.getAvailableDays() < job.getDurationDays()) {
            throw new RuntimeException("Not enough available days");
        }

        JobApplication app = new JobApplication();
        app.setJob(job);
        app.setWorker(worker);
        app.setType(JobApplication.Type.APPLIED);
        app.setStatus(JobApplication.Status.PENDING);
        appRepository.save(app);
    }

    public List<Map<String, Object>> getMyApplications(Long workerId) {
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        List<JobApplication> applications = appRepository.findByWorkerId(workerId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (JobApplication app : applications) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("applicationId", app.getApplicationId());
            map.put("jobId", app.getJob().getJobId());
            map.put("workType", app.getJob().getWorkType());
            map.put("durationDays", app.getJob().getDurationDays());
            map.put("village", app.getJob().getVillage());
            map.put("district", app.getJob().getDistrict());
            map.put("state", app.getJob().getState());
            map.put("experienceRequired", app.getJob().getExperienceRequired());
            map.put("farmerName", app.getJob().getFarmer().getName());
            map.put("farmerId", app.getJob().getFarmer().getId());
            map.put("status", app.getStatus().name());
            map.put("type", app.getType().name());
            map.put("appliedAt", app.getAppliedAt());
            if (app.getStatus() == JobApplication.Status.SELECTED) {
                map.put("farmerPhone", app.getJob().getFarmer().getPhone());
            }
            result.add(map);
        }

        return result;
    }

    @Transactional
    public void selectWorker(Long applicationId, Long farmerId) {
        JobApplication app = appRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!app.getJob().getFarmer().getId().equals(farmerId)) {
            throw new RuntimeException("Not authorized");
        }

        Job job = app.getJob();
        long selectedCount = appRepository.countByJobJobIdAndStatus(
                job.getJobId(), JobApplication.Status.SELECTED);
        if (selectedCount >= job.getWorkersRequired()) {
            throw new RuntimeException("All worker spots filled");
        }

        app.setStatus(JobApplication.Status.SELECTED);
        appRepository.save(app);

        User worker = app.getWorker();
        worker.setAvailableDays(Math.max(0, worker.getAvailableDays() - job.getDurationDays()));
        userRepository.save(worker);

        // Check if job is now filled
        selectedCount++;
        if (selectedCount >= job.getWorkersRequired()) {
            job.setStatus(Job.Status.FILLED);
            jobRepository.save(job);
        }
    }

    public void rejectWorker(Long applicationId, Long farmerId) {
        JobApplication app = appRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        if (!app.getJob().getFarmer().getId().equals(farmerId)) {
            throw new RuntimeException("Not authorized");
        }
        app.setStatus(JobApplication.Status.REJECTED);
        appRepository.save(app);
    }

    // ===== LAND =====

    public LandListing postLand(Long farmerId, Map<String, String> body) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));

        LandListing land = new LandListing();
        land.setFarmer(farmer);
        land.setVillage(body.getOrDefault("village", farmer.getVillage()));
        land.setDistrict(body.getOrDefault("district", farmer.getDistrict()));
        land.setState(body.getOrDefault("state", farmer.getState()));
        land.setArea(new BigDecimal(body.get("area")));
        land.setCropType(body.get("cropType"));
        land.setDuration(body.get("duration"));

        return landRepository.save(land);
    }

    public List<Map<String, Object>> getAllLand(String district) {
        List<LandListing> lands = (district != null && !district.isEmpty())
                ? landRepository.findByDistrictAndStatus(district, LandListing.Status.AVAILABLE)
                : landRepository.findByStatus(LandListing.Status.AVAILABLE);

        List<Map<String, Object>> result = new ArrayList<>();
        for (LandListing land : lands) {
            result.add(landToMap(land, false));
        }
        return result;
    }

    public List<Map<String, Object>> getMyLand(Long farmerId) {
        List<LandListing> lands = landRepository.findByFarmerId(farmerId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (LandListing land : lands) {
            Map<String, Object> map = landToMap(land, true);
            List<Map<String, Object>> requests = new ArrayList<>();
            for (LandRequest req : landRequestRepository.findByLandLandId(land.getLandId())) {
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("requestId", req.getRequestId());
                r.put("kavuluName", req.getKavulu().getName());
                r.put("kavuluId", req.getKavulu().getId());
                r.put("experience", req.getKavulu().getExperience());
                r.put("status", req.getStatus().name());
                if (req.getStatus() == LandRequest.Status.APPROVED) {
                    r.put("phone", req.getKavulu().getPhone());
                }
                requests.add(r);
            }
            map.put("requests", requests);
            result.add(map);
        }
        return result;
    }

    public void requestLand(Long landId, Long kavuluId) {
        if (landRequestRepository.existsByLandLandIdAndKavuluId(landId, kavuluId)) {
            throw new RuntimeException("Already requested this land");
        }

        LandListing land = landRepository.findById(landId)
                .orElseThrow(() -> new RuntimeException("Land listing not found"));

        if (land.getStatus() == LandListing.Status.TAKEN) {
            throw new RuntimeException("This land is already taken");
        }

        User kavulu = userRepository.findById(kavuluId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LandRequest request = new LandRequest();
        request.setLand(land);
        request.setKavulu(kavulu);
        landRequestRepository.save(request);
    }

    @Transactional
    public void approveLandRequest(Long requestId, Long farmerId) {
        LandRequest request = landRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if (!request.getLand().getFarmer().getId().equals(farmerId)) {
            throw new RuntimeException("Not authorized");
        }

        LandListing land = request.getLand();
        if (land.getStatus() == LandListing.Status.TAKEN) {
            throw new RuntimeException("This land is already taken");
        }

        request.setStatus(LandRequest.Status.APPROVED);
        landRequestRepository.save(request);

        // Mark land as taken
        land.setStatus(LandListing.Status.TAKEN);
        landRepository.save(land);

        // Reject all other pending requests for this land
        List<LandRequest> otherRequests = landRequestRepository.findByLandLandId(land.getLandId());
        for (LandRequest other : otherRequests) {
            if (!other.getRequestId().equals(requestId) && other.getStatus() == LandRequest.Status.PENDING) {
                other.setStatus(LandRequest.Status.REJECTED);
                landRequestRepository.save(other);
            }
        }
    }

    public void rejectLandRequest(Long requestId, Long farmerId) {
        LandRequest request = landRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if (!request.getLand().getFarmer().getId().equals(farmerId)) {
            throw new RuntimeException("Not authorized");
        }
        request.setStatus(LandRequest.Status.REJECTED);
        landRequestRepository.save(request);
    }

    // ===== WORKER AVAILABILITY =====

    public void updateAvailability(Long workerId, int days) {
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));
        worker.setAvailableDays(days);
        userRepository.save(worker);
    }

    public List<Map<String, Object>> getAvailableWorkers(String district) {
        List<User> workers = (district != null && !district.isEmpty())
                ? userRepository.findByRoleAndDistrictAndAvailableDaysGreaterThan(User.Role.WORKER, district, 0)
                : userRepository.findByRoleAndAvailableDaysGreaterThan(User.Role.WORKER, 0);

        List<Map<String, Object>> result = new ArrayList<>();
        for (User w : workers) {
            if (w.getSuspended()) continue;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", w.getId());
            map.put("name", w.getName());
            map.put("village", w.getVillage());
            map.put("district", w.getDistrict());
            map.put("workType", w.getWorkType());
            map.put("experience", w.getExperience());
            map.put("availableDays", w.getAvailableDays());
            result.add(map);
        }
        return result;
    }

    @Transactional
    public void hireWorkerDirectly(Long farmerId, Long workerId, Integer durationDays) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        if (!farmer.getRole().equals(User.Role.FARMER)) {
            throw new RuntimeException("Only farmers can hire workers");
        }
        if (!worker.getRole().equals(User.Role.WORKER)) {
            throw new RuntimeException("Can only hire workers");
        }
        if (worker.getAvailableDays() < durationDays) {
            throw new RuntimeException("Worker does not have enough available days");
        }

        // Create a job for this direct hire (already filled since it's direct)
        Job job = new Job();
        job.setFarmer(farmer);
        job.setWorkType(worker.getWorkType());
        job.setDurationDays(durationDays);
        job.setWorkersRequired(1);
        job.setVillage(farmer.getVillage());
        job.setDistrict(farmer.getDistrict());
        job.setState(farmer.getState());
        job.setExperienceRequired(worker.getExperience());
        job.setStatus(Job.Status.FILLED);
        job = jobRepository.save(job);

        // Create application record with type=DIRECT and status=SELECTED
        JobApplication app = new JobApplication();
        app.setJob(job);
        app.setWorker(worker);
        app.setType(JobApplication.Type.DIRECT);
        app.setStatus(JobApplication.Status.SELECTED);
        appRepository.save(app);

        // Update worker availability
        worker.setAvailableDays(Math.max(0, worker.getAvailableDays() - durationDays));
        userRepository.save(worker);
    }

    // ===== USER PROFILE =====

    public User updateUserProfile(Long userId, Map<String, Object> profileData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update common fields (phone is NOT editable)
        if (profileData.containsKey("name")) {
            user.setName((String) profileData.get("name"));
        }
        if (profileData.containsKey("village")) {
            user.setVillage((String) profileData.get("village"));
        }
        if (profileData.containsKey("district")) {
            user.setDistrict((String) profileData.get("district"));
        }
        if (profileData.containsKey("state")) {
            user.setState((String) profileData.get("state"));
        }

        // Update role-specific fields
        if (user.getRole() == User.Role.WORKER) {
            if (profileData.containsKey("workType")) {
                user.setWorkType((String) profileData.get("workType"));
            }
            if (profileData.containsKey("experience")) {
                user.setExperience((String) profileData.get("experience"));
            }
            if (profileData.containsKey("availableDays")) {
                Object days = profileData.get("availableDays");
                user.setAvailableDays(days instanceof Integer ? (Integer) days :
                        Integer.parseInt(days.toString()));
            }
            if (profileData.containsKey("availableFrom")) {
                Object dateObj = profileData.get("availableFrom");
                if (dateObj instanceof LocalDate) {
                    user.setAvailableFrom((LocalDate) dateObj);
                } else if (dateObj instanceof String) {
                    user.setAvailableFrom(LocalDate.parse((String) dateObj));
                }
            }
        } else if (user.getRole() == User.Role.FARMER) {
            if (profileData.containsKey("preferredCrop")) {
                user.setPreferredCrop((String) profileData.get("preferredCrop"));
            }
        } else if (user.getRole() == User.Role.KAVULU) {
            if (profileData.containsKey("preferredCrop")) {
                user.setPreferredCrop((String) profileData.get("preferredCrop"));
            }
            if (profileData.containsKey("experience")) {
                user.setExperience((String) profileData.get("experience"));
            }
        }

        return userRepository.save(user);
    }

    public Map<String, Object> getCurrentUserProfile(Long userId) {
        User user = userRepository.findById(userId)
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
        } else if (user.getRole() == User.Role.FARMER) {
            map.put("preferredCrop", user.getPreferredCrop());
        } else if (user.getRole() == User.Role.KAVULU) {
            map.put("preferredCrop", user.getPreferredCrop());
            map.put("experience", user.getExperience());
        }

        return map;
    }

    // ===== HELPERS =====

    private Map<String, Object> jobToMap(Job job, boolean includeFarmerDetails) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("jobId", job.getJobId());
        map.put("workType", job.getWorkType());
        map.put("durationDays", job.getDurationDays());
        map.put("workersRequired", job.getWorkersRequired());
        map.put("village", job.getVillage());
        map.put("district", job.getDistrict());
        map.put("state", job.getState());
        map.put("experienceRequired", job.getExperienceRequired());
        map.put("status", job.getStatus().name());
        map.put("createdAt", job.getCreatedAt());
        map.put("farmerName", job.getFarmer().getName());
        if (includeFarmerDetails) {
            map.put("farmerId", job.getFarmer().getId());
        }
        return map;
    }

    private Map<String, Object> landToMap(LandListing land, boolean includeFarmerDetails) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("landId", land.getLandId());
        map.put("village", land.getVillage());
        map.put("district", land.getDistrict());
        map.put("state", land.getState());
        map.put("area", land.getArea());
        map.put("cropType", land.getCropType());
        map.put("duration", land.getDuration());
        map.put("status", land.getStatus().name());
        map.put("createdAt", land.getCreatedAt());
        map.put("farmerName", land.getFarmer().getName());
        if (includeFarmerDetails) {
            map.put("farmerId", land.getFarmer().getId());
        }
        return map;
    }
}
