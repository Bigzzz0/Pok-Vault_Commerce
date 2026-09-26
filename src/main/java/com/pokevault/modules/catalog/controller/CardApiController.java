package com.pokevault.modules.catalog.controller;

import com.pokevault.common.response.ApiResponse;
import com.pokevault.common.response.PageResponse;
import com.pokevault.modules.catalog.dto.CardExpansionResponse;
import com.pokevault.modules.catalog.dto.CardFilterRequest;
import com.pokevault.modules.catalog.dto.CardRequest;
import com.pokevault.modules.catalog.dto.CardResponse;
import com.pokevault.modules.catalog.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/paged")
    @Operation(summary = "Get cards with pagination and sorting", description = "Retrieve a paged list of Pokemon cards with query filters and sort options")
    public ResponseEntity<ApiResponse<PageResponse<CardResponse>>> getCardsPaged(
            @ModelAttribute CardFilterRequest filter,
            @PageableDefault(page = 0, size = 10, sort = "cardNumber", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<CardResponse> pageResult = cardService.getCardsPaged(filter, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Retrieved page " + pageResult.getPageNumber() + " successfully", pageResult));
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

    @PostMapping
    @Operation(summary = "Create a new card", description = "Add a new Pokemon card to the catalog")
    public ResponseEntity<ApiResponse<CardResponse>> createCard(@Valid @RequestBody CardRequest request) {
        CardResponse created = cardService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Card created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing card", description = "Update card details by card ID")
    public ResponseEntity<ApiResponse<CardResponse>> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody CardRequest request) {
        CardResponse updated = cardService.updateCard(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Card updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a card", description = "Remove a card from the catalog by ID")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}
