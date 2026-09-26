package com.pokevault.repository;

import com.pokevault.domain.entity.CardInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardInventoryRepository extends JpaRepository<CardInventory, Long> {

    List<CardInventory> findByCardId(Long cardId);

    List<CardInventory> findByGameAccountId(Long gameAccountId);
}
