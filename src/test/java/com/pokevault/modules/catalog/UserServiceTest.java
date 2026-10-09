package com.pokevault.modules.catalog;

import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.domain.entity.User;
import com.pokevault.domain.entity.UserProfile;
import com.pokevault.domain.enums.MembershipTier;
import com.pokevault.modules.catalog.service.UserServiceImpl;
import com.pokevault.repository.UserRepository;
import com.pokevault.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository users;
    @Mock UserProfileRepository profiles;
    @InjectMocks UserServiceImpl service;

    @Test void profileMapsMembershipAndContactFields() {
        User user = User.builder().id(1L).username("red").email("red@example.test").build();
        user.setUserProfile(UserProfile.builder().user(user).fullName("Red")
                .phoneNumber("0800000000").shippingAddress("Khon Kaen")
                .membershipTier(MembershipTier.VIP).rewardPoints(120).build());
        when(users.findById(1L)).thenReturn(Optional.of(user));
        var result = service.getUserProfile(1L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("red");
        assertThat(result.getEmail()).isEqualTo("red@example.test");
        assertThat(result.getRole()).isEqualTo("CUSTOMER");
        assertThat(result.getFullName()).isEqualTo("Red");
        assertThat(result.getPhoneNumber()).isEqualTo("0800000000");
        assertThat(result.getShippingAddress()).isEqualTo("Khon Kaen");
        assertThat(result.getMembershipTier()).isEqualTo("VIP");
        assertThat(result.getRewardPoints()).isEqualTo(120);
    }

    @Test void absentProfileReturnsZeroPointsAndNullAddress() {
        when(users.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        var result = service.getUserProfile(1L);
        assertThat(result.getRewardPoints()).isZero();
        assertThat(result.getShippingAddress()).isNull();
    }

    @Test void missingUserCannotBeRead() {
        when(users.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getUserProfile(99L)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(profiles);
    }

    @Test void addressUpdateCreatesAndLinksMissingProfile() {
        User user = User.builder().id(1L).build();
        when(users.findById(1L)).thenReturn(Optional.of(user));
        var result = service.updateShippingAddress(1L, "New address");
        assertThat(user.getUserProfile().getUser()).isSameAs(user);
        assertThat(user.getUserProfile().getMembershipTier()).isEqualTo(MembershipTier.REGULAR);
        assertThat(result.getShippingAddress()).isEqualTo("New address");
        verify(profiles).save(user.getUserProfile());
    }

    @Test void addressUpdatePreservesExistingMembershipAndPoints() {
        User user = User.builder().id(1L).build();
        UserProfile profile = UserProfile.builder().user(user).membershipTier(MembershipTier.VIP)
                .rewardPoints(42).shippingAddress("Old address").build();
        user.setUserProfile(profile);
        when(users.findById(1L)).thenReturn(Optional.of(user));
        var result = service.updateShippingAddress(1L, "New address");
        assertThat(user.getUserProfile()).isSameAs(profile);
        assertThat(result.getShippingAddress()).isEqualTo("New address");
        assertThat(result.getMembershipTier()).isEqualTo("VIP");
        assertThat(result.getRewardPoints()).isEqualTo(42);
        verify(profiles).save(profile);
    }

    @Test void missingUserCannotUpdateAddress() {
        when(users.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateShippingAddress(99L, "address"))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(profiles);
    }

    @Test void rewardPointsComeFromProfileRepository() {
        when(profiles.findByUserId(1L)).thenReturn(Optional.of(UserProfile.builder().rewardPoints(77).build()));
        assertThat(service.getRewardPoints(1L)).isEqualTo(77);
        verifyNoInteractions(users);
    }

    @Test void missingRewardProfileThrowsNotFound() {
        when(profiles.findByUserId(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getRewardPoints(99L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
