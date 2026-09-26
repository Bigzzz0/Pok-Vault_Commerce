package com.pokevault.modules.catalog.service;

import com.pokevault.common.response.PageResponse;
import com.pokevault.modules.catalog.dto.CardExpansionResponse;
import com.pokevault.modules.catalog.dto.CardFilterRequest;
import com.pokevault.modules.catalog.dto.CardRequest;
import com.pokevault.modules.catalog.dto.CardResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CardService {

    List<CardResponse> getAllCards(CardFilterRequest filter);

    PageResponse<CardResponse> getCardsPaged(CardFilterRequest filter, Pageable pageable);

    CardResponse getCardById(Long id);

    List<CardResponse> searchCards(String query);

    List<CardExpansionResponse> getAllExpansions();

    CardExpansionResponse getExpansionByCode(String code);

    List<CardResponse> getCardsByExpansionCode(String expansionCode);

    CardResponse createCard(CardRequest request);

    CardResponse updateCard(Long id, CardRequest request);

    void deleteCard(Long id);
}
