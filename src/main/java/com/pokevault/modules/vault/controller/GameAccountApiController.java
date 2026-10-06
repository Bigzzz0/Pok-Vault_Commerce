package com.pokevault.modules.vault.controller;

import com.pokevault.common.response.ApiResponse;
import com.pokevault.modules.vault.dto.AccountCardResponse;
import com.pokevault.modules.vault.dto.AddPulledCardRequest;
import com.pokevault.modules.vault.dto.GameAccountRequest;
import com.pokevault.modules.vault.dto.GameAccountResponse;
import com.pokevault.modules.vault.service.GameAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller สำหรับจัดการคลังไอดีเกมของร้าน (Vault), สต็อกการ์ด,
 * และบันทึกการเปิดซอง
 * ผู้รับผิดชอบ: สมาชิกคนที่ 2 (Game Account Vault & Inventory Manager)
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Game Account Vault API", description = "Endpoints for managing store game accounts, vault inventory, and pack pull logging")
public class GameAccountApiController {

    private final GameAccountService gameAccountService;

    @PostMapping
    @Operation(summary = "Register new game account", description = "Register a new Pokemon TCG Pocket store account to the vault")
    public ResponseEntity<ApiResponse<GameAccountResponse>> createAccount(
            @Valid @RequestBody GameAccountRequest request) {
        GameAccountResponse response = gameAccountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Game account registered successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all game accounts", description = "Retrieve all store game accounts with card counts")
    public ResponseEntity<ApiResponse<List<GameAccountResponse>>> getAllAccounts() {
        List<GameAccountResponse> accounts = gameAccountService.getAllAccounts();
        return ResponseEntity
                .ok(ApiResponse.ok("Retrieved " + accounts.size() + " game accounts successfully", accounts));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get game account by ID", description = "Retrieve game account details by its ID")
    public ResponseEntity<ApiResponse<GameAccountResponse>> getAccountById(@PathVariable Long id) {
        GameAccountResponse account = gameAccountService.getAccountById(id);
        return ResponseEntity.ok(ApiResponse.ok("Game account retrieved successfully", account));
    }

    @PostMapping("/{id}/pulls")
    @Operation(summary = "Record pack pull into game account", description = "Record pulled cards and add inventory to the specified game account")
    public ResponseEntity<ApiResponse<AccountCardResponse>> addPulledCard(
            @PathVariable Long id,
            @Valid @RequestBody AddPulledCardRequest request) {
        AccountCardResponse response = gameAccountService.addPulledCard(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Pulled card recorded and added to vault successfully", response));
    }
}
