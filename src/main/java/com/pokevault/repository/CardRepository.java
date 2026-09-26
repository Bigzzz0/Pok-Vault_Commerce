package com.pokevault.repository;

import com.pokevault.domain.entity.Card;
import com.pokevault.domain.enums.CardType;
import com.pokevault.domain.enums.ElementType;
import com.pokevault.domain.enums.Rarity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {
    List<Card> findByNameContainingIgnoreCase(String name);
    List<Card> findByRarity(Rarity rarity);
    List<Card> findByCardType(CardType cardType);
    List<Card> findByElementType(ElementType elementType);
    List<Card> findByExpansionId(Long expansionId);
    List<Card> findByExpansionCode(String expansionCode);
    Optional<Card> findByExpansionIdAndCardNumber(Long expansionId, String cardNumber);
    Page<Card> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Card> findByExpansionCode(String expansionCode, Pageable pageable);
}
