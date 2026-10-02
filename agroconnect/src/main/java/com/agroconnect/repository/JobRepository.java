package com.agroconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agroconnect.model.Job;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByFarmerId(Long farmerId);
    List<Job> findByDistrict(String district);
    List<Job> findByStatus(Job.Status status);
    List<Job> findByDistrictAndStatus(String district, Job.Status status);
}
