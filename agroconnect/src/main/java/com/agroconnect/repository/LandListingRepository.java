package com.agroconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agroconnect.model.LandListing;

public interface LandListingRepository extends JpaRepository<LandListing, Long> {
    List<LandListing> findByFarmerId(Long farmerId);
    List<LandListing> findByDistrict(String district);
    List<LandListing> findByStatus(LandListing.Status status);
    List<LandListing> findByDistrictAndStatus(String district, LandListing.Status status);
}
