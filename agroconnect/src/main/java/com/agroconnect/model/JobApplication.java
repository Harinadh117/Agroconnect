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
@Table(name = "job_applications")
public class JobApplication {

    public enum Status { PENDING, SELECTED, REJECTED }
    public enum Type { APPLIED, DIRECT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type = Type.APPLIED;

    @Column(name = "applied_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime appliedAt;

    @PrePersist
    protected void onCreate() { this.appliedAt = LocalDateTime.now(); }

    public JobApplication() {}

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }
    public User getWorker() { return worker; }
    public void setWorker(User worker) { this.worker = worker; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
}
