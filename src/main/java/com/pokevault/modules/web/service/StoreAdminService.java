package com.pokevault.modules.web.service;

import com.pokevault.domain.enums.MembershipTier;

import java.math.BigDecimal;
import java.util.Map;

/**
 * งานหลังบ้านของร้าน (ADMIN / STAFF): ปรับราคาขายการ์ด และกำหนดระดับสมาชิกของลูกค้า
 */
public interface StoreAdminService {

    Map<String, Object> updateSellingPrice(Long inventoryId, BigDecimal sellingPrice);

    Map<String, Object> updateMembershipTier(Long userId, MembershipTier tier);
}
