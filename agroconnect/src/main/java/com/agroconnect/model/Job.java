package com.agroconnect.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "farmer_id", nullable = false)
    private User farmer;

    @Column(name = "work_type", nullable = false)
    private String workType;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "workers_required", nullable = false)
    private Integer workersRequired = 1;

    private String village;
    private String district;
    private String state;

    @Column(name = "experience_required")
    private String experienceRequired;

    public enum Status { OPEN, FILLED }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public Job() {}

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }
    public User getFarmer() { return farmer; }
    public void setFarmer(User farmer) { this.farmer = farmer; }
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public Integer getWorkersRequired() { return workersRequired; }
    public void setWorkersRequired(Integer workersRequired) { this.workersRequired = workersRequired; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getExperienceRequired() { return experienceRequired; }
    public void setExperienceRequired(String experienceRequired) { this.experienceRequired = experienceRequired; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
