package com.agroconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agroconnect.model.JobApplication;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJobJobId(Long jobId);
    List<JobApplication> findByWorkerId(Long workerId);
    boolean existsByJobJobIdAndWorkerId(Long jobId, Long workerId);
    long countByJobJobIdAndStatus(Long jobId, JobApplication.Status status);
}
