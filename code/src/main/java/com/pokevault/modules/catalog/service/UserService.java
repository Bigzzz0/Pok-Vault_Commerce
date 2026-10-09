package com.pokevault.modules.catalog.service;

import com.pokevault.modules.catalog.dto.UserProfileResponse;
import jakarta.validation.constraints.Size;

public interface UserService {

    UserProfileResponse getUserProfile(Long userId);

    UserProfileResponse updateShippingAddress(Long userId, @Size(max = 500) String shippingAddress);

    Integer getRewardPoints(Long userId);
}
