package com.pokevault.modules.web.service;

import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.entity.User;
import com.pokevault.domain.entity.UserProfile;
import com.pokevault.domain.enums.MembershipTier;
import com.pokevault.domain.enums.UserRole;
import com.pokevault.repository.CardInventoryRepository;
import com.pokevault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreAdminServiceImpl implements StoreAdminService {

    private final CardInventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Map<String, Object> updateSellingPrice(Long inventoryId, BigDecimal sellingPrice) {
        if (sellingPrice == null || sellingPrice.signum() < 0) {
            throw new IllegalArgumentException("ราคาขายต้องตั้งแต่ 0 ขึ้นไป");
        }
        CardInventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("CardInventory", "id", inventoryId));

        BigDecimal oldPrice = inventory.getSellingPrice();
        inventory.setSellingPrice(sellingPrice.setScale(2, RoundingMode.HALF_UP));
        CardInventory saved = inventoryRepository.save(inventory);
        log.info("Updated selling price of inventory [ID: {}]: {} -> {}", inventoryId, oldPrice, saved.getSellingPrice());

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("inventoryId", inventoryId);
        view.put("sellingPrice", saved.getSellingPrice());
        return view;
    }

    @Override
    @Transactional
    public Map<String, Object> updateMembershipTier(Long userId, MembershipTier tier) {
        if (tier == null) {
            throw new IllegalArgumentException("กรุณาเลือกระดับสมาชิก");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        // ระดับสมาชิกมีผลกับส่วนลด (Strategy) ของลูกค้าเท่านั้น
        if (user.getRole() != UserRole.CUSTOMER) {
            throw new IllegalArgumentException("กำหนดระดับสมาชิกได้เฉพาะบัญชีลูกค้า");
        }

        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            profile = UserProfile.builder().user(user).fullName(user.getUsername()).build();
            user.setUserProfile(profile);
        }
        MembershipTier oldTier = profile.getMembershipTier();
        profile.setMembershipTier(tier);
        userRepository.save(user);
        log.info("Updated membership tier of customer [ID: {}]: {} -> {}", userId, oldTier, tier);

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("userId", userId);
        view.put("membershipTier", tier);
        return view;
    }
}
