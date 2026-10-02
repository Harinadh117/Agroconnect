package com.agroconnect.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    public enum Role { FARMER, WORKER, KAVULU, ADMIN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 10)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String village;
    private String district;
    private String state;

    @Column(name = "work_type")
    private String workType;

    private String experience;

    @Column(name = "available_days")
    private Integer availableDays = 0;

    @Column(name = "available_from")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate availableFrom;

    @Column(name = "preferred_crop")
    private String preferredCrop;

    @Column(nullable = false)
    private Boolean verified = false;

    @Column(nullable = false)
    private Boolean suspended = false;

    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public Integer getAvailableDays() { return availableDays; }
    public void setAvailableDays(Integer availableDays) { this.availableDays = availableDays; }
    public LocalDate getAvailableFrom() { return availableFrom; }
    public void setAvailableFrom(LocalDate availableFrom) { this.availableFrom = availableFrom; }
    public String getPreferredCrop() { return preferredCrop; }
    public void setPreferredCrop(String preferredCrop) { this.preferredCrop = preferredCrop; }
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    public Boolean getSuspended() { return suspended; }
    public void setSuspended(Boolean suspended) { this.suspended = suspended; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}