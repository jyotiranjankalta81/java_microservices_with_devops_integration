package com.jobintel.job.repository;

import com.jobintel.job.domain.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByCountryIgnoreCaseAndCityIgnoreCaseAndStateIgnoreCase(
            String country, String city, String state);

    Optional<Location> findByCountryIgnoreCaseAndCityIgnoreCaseAndStateIsNull(
            String country, String city);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT l.country FROM Location l WHERE l.country IS NOT NULL AND l.country != '' ORDER BY l.country ASC")
    java.util.List<String> findDistinctCountries();

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT l.city FROM Location l WHERE l.city IS NOT NULL AND l.city != '' AND (:country IS NULL OR :country = '' OR LOWER(l.country) = LOWER(:country)) ORDER BY l.city ASC")
    java.util.List<String> findDistinctCitiesByCountry(@org.springframework.data.repository.query.Param("country") String country);
}
