package com.agroconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agroconnect.model.LandRequest;

public interface LandRequestRepository extends JpaRepository<LandRequest, Long> {
    List<LandRequest> findByLandLandId(Long landId);
    List<LandRequest> findByKavuluId(Long kavuluId);
    boolean existsByLandLandIdAndKavuluId(Long landId, Long kavuluId);
}
