package com.pokevault.repository;

import com.pokevault.domain.entity.CardExpansion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardExpansionRepository extends JpaRepository<CardExpansion, Long> {
    Optional<CardExpansion> findByCode(String code);
    boolean existsByCode(String code);
    List<CardExpansion> findBySeriesContainingIgnoreCase(String series);
}
