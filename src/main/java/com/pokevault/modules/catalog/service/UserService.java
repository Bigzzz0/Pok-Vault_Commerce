package com.pokevault.modules.catalog.service;

import com.pokevault.modules.catalog.dto.UserProfileResponse;

public interface UserService {

    UserProfileResponse getUserProfile(Long userId);

    UserProfileResponse updateShippingAddress(Long userId, String shippingAddress);

    Integer getRewardPoints(Long userId);
}
