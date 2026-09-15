package com.pokevault.modules.catalog.dto;

import com.pokevault.domain.entity.User;
import com.pokevault.domain.entity.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long userId;
    private String username;
    private String email;
    private String role;
    private String fullName;
    private String phoneNumber;
    private String shippingAddress;
    private String membershipTier;
    private Integer rewardPoints;

    public static UserProfileResponse fromUser(User user) {
        if (user == null) return null;
        UserProfile profile = user.getUserProfile();
        return UserProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .fullName(profile != null ? profile.getFullName() : null)
                .phoneNumber(profile != null ? profile.getPhoneNumber() : null)
                .shippingAddress(profile != null ? profile.getShippingAddress() : null)
                .membershipTier(profile != null && profile.getMembershipTier() != null ? profile.getMembershipTier().name() : null)
                .rewardPoints(profile != null ? profile.getRewardPoints() : 0)
                .build();
    }
}
