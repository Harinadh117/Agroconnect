package com.agroconnect.model;

import java.math.BigDecimal;
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
@Table(name = "land_listings")
public class LandListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "land_id")
    private Long landId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "farmer_id", nullable = false)
    private User farmer;

    private String village;
    private String district;
    private String state;

    @Column(nullable = false)
    private BigDecimal area;

    @Column(name = "crop_type")
    private String cropType;

    private String duration;

    public enum Status { AVAILABLE, TAKEN }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.AVAILABLE;

    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public LandListing() {}

    public Long getLandId() { return landId; }
    public void setLandId(Long landId) { this.landId = landId; }
    public User getFarmer() { return farmer; }
    public void setFarmer(User farmer) { this.farmer = farmer; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public BigDecimal getArea() { return area; }
    public void setArea(BigDecimal area) { this.area = area; }
    public String getCropType() { return cropType; }
    public void setCropType(String cropType) { this.cropType = cropType; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
