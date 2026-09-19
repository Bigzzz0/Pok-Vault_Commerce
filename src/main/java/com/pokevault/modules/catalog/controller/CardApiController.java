package com.pokevault.modules.catalog.controller;

import com.pokevault.common.response.ApiResponse;
import com.pokevault.modules.catalog.dto.CardExpansionResponse;
import com.pokevault.modules.catalog.dto.CardFilterRequest;
import com.pokevault.modules.catalog.dto.CardResponse;
import com.pokevault.modules.catalog.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Tag(name = "Card Catalog API", description = "Endpoints for browsing, filtering, and searching Pokemon TCG Pocket cards and expansions")
public class CardApiController {

    private final CardService cardService;

    @GetMapping
    @Operation(summary = "Get all cards with optional filtering", description = "Retrieve a list of Pokemon cards filtered by expansion, rarity, card type, or element")
    public ResponseEntity<ApiResponse<List<CardResponse>>> getAllCards(
            @ModelAttribute CardFilterRequest filter) {
        List<CardResponse> cards = cardService.getAllCards(filter);
        return ResponseEntity.ok(ApiResponse.ok("Retrieved " + cards.size() + " cards successfully", cards));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get card by ID", description = "Retrieve single card details by card ID")
    public ResponseEntity<ApiResponse<CardResponse>> getCardById(@PathVariable Long id) {
        CardResponse card = cardService.getCardById(id);
        return ResponseEntity.ok(ApiResponse.ok(card));
    }

    @GetMapping("/search")
    @Operation(summary = "Search cards by name", description = "Search cards with case-insensitive name matching")
    public ResponseEntity<ApiResponse<List<CardResponse>>> searchCards(@RequestParam String q) {
        List<CardResponse> cards = cardService.searchCards(q);
        return ResponseEntity.ok(ApiResponse.ok("Search results for '" + q + "'", cards));
    }

    @GetMapping("/expansions")
    @Operation(summary = "Get all card expansions", description = "Retrieve list of all official Pokemon TCG Pocket sets and booster packs")
    public ResponseEntity<ApiResponse<List<CardExpansionResponse>>> getAllExpansions() {
        List<CardExpansionResponse> expansions = cardService.getAllExpansions();
        return ResponseEntity.ok(ApiResponse.ok(expansions));
    }

    @GetMapping("/expansions/{code}")
    @Operation(summary = "Get expansion by set code", description = "Retrieve expansion set information by set code (e.g. A1, A1a)")
    public ResponseEntity<ApiResponse<CardExpansionResponse>> getExpansionByCode(@PathVariable String code) {
        CardExpansionResponse expansion = cardService.getExpansionByCode(code);
        return ResponseEntity.ok(ApiResponse.ok(expansion));
    }

    @GetMapping("/expansions/{code}/cards")
    @Operation(summary = "Get all cards in an expansion set", description = "Retrieve all cards belonging to the specified expansion set code")
    public ResponseEntity<ApiResponse<List<CardResponse>>> getCardsByExpansionCode(@PathVariable String code) {
        List<CardResponse> cards = cardService.getCardsByExpansionCode(code);
        return ResponseEntity.ok(ApiResponse.ok("Retrieved " + cards.size() + " cards for expansion " + code, cards));
    }
}
