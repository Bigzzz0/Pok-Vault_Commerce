package com.pokevault.modules.web.controller;

import com.pokevault.common.response.ApiResponse;
import com.pokevault.domain.enums.MembershipTier;
import com.pokevault.modules.web.service.StoreAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * REST API Controller สำหรับงานหลังบ้านของร้าน: ปรับราคาขายการ์ด และกำหนดระดับสมาชิกของลูกค้า
 * ผู้รับผิดชอบ: สมาชิกคนที่ 5 (Frontend & Chat Commerce)
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Store Admin API", description = "Back-office endpoints for ADMIN / STAFF: card pricing and customer membership tiers")
public class StoreAdminApiController {

    private static final Set<String> BACK_OFFICE_ROLES = Set.of("ROLE_ADMIN", "ROLE_STAFF");

    private final StoreAdminService storeAdminService;

    @PatchMapping("/inventories/{inventoryId}/price")
    @Operation(summary = "Update card selling price", description = "Set the retail price of one inventory item (ADMIN / STAFF only)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updatePrice(@PathVariable Long inventoryId,
                                                                      @RequestParam BigDecimal price,
                                                                      Authentication authentication) {
        ResponseEntity<ApiResponse<Map<String, Object>>> denied = denyUnlessBackOffice(authentication);
        if (denied != null) {
            return denied;
        }
        return ResponseEntity.ok(ApiResponse.ok("Selling price updated", storeAdminService.updateSellingPrice(inventoryId, price)));
    }

    @PatchMapping("/customers/{userId}/membership-tier")
    @Operation(summary = "Update customer membership tier", description = "Set REGULAR / VIP / WHOLESALE for a customer (ADMIN / STAFF only)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateMembershipTier(@PathVariable Long userId,
                                                                               @RequestParam MembershipTier tier,
                                                                               Authentication authentication) {
        ResponseEntity<ApiResponse<Map<String, Object>>> denied = denyUnlessBackOffice(authentication);
        if (denied != null) {
            return denied;
        }
        return ResponseEntity.ok(ApiResponse.ok("Membership tier updated", storeAdminService.updateMembershipTier(userId, tier)));
    }

    // /api/** เปิด permitAll ใน SecurityConfig จึงต้องเช็ก role ของ session ที่นี่เอง
    private ResponseEntity<ApiResponse<Map<String, Object>>> denyUnlessBackOffice(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Please sign in as store staff"));
        }
        boolean backOffice = authentication.getAuthorities().stream()
                .anyMatch(a -> BACK_OFFICE_ROLES.contains(a.getAuthority()));
        if (!backOffice) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Only store staff can do this"));
        }
        return null;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalid(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }
}
