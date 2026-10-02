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
@Table(name = "land_requests")
public class LandRequest {

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "land_id", nullable = false)
    private LandListing land;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "kavulu_id", nullable = false)
    private User kavulu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "requested_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestedAt;

    @PrePersist
    protected void onCreate() {
        this.requestedAt = LocalDateTime.now();
    }

    public LandRequest() {}

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public LandListing getLand() { return land; }
    public void setLand(LandListing land) { this.land = land; }

    public User getKavulu() { return kavulu; }
    public void setKavulu(User kavulu) { this.kavulu = kavulu; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
}
