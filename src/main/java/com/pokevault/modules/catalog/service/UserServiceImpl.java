package com.pokevault.modules.catalog.service;

import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.domain.entity.User;
import com.pokevault.domain.entity.UserProfile;
import com.pokevault.modules.catalog.dto.UserProfileResponse;
import com.pokevault.repository.UserProfileRepository;
import com.pokevault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return UserProfileResponse.fromUser(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateShippingAddress(Long userId, String shippingAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            profile = UserProfile.builder()
                    .user(user)
                    .shippingAddress(shippingAddress)
                    .build();
            user.setUserProfile(profile);
        } else {
            profile.setShippingAddress(shippingAddress);
        }

        userProfileRepository.save(profile);
        return UserProfileResponse.fromUser(user);
    }

    @Override
    public Integer getRewardPoints(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));
        return profile.getRewardPoints();
    }
}
