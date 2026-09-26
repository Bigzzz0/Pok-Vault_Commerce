package com.pokevault.modules.catalog.service;

import com.pokevault.modules.catalog.dto.CardExpansionResponse;
import com.pokevault.modules.catalog.dto.CardFilterRequest;
import com.pokevault.modules.catalog.dto.CardResponse;

import java.util.List;

public interface CardService {

    List<CardResponse> getAllCards(CardFilterRequest filter);

    CardResponse getCardById(Long id);

    List<CardResponse> searchCards(String query);

    List<CardExpansionResponse> getAllExpansions();

    CardExpansionResponse getExpansionByCode(String code);

    List<CardResponse> getCardsByExpansionCode(String expansionCode);
}
