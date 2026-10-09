package com.pokevault.common.security;

import com.pokevault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ownership authorization only; order pricing, stock and state rules remain in OrderService. */
@Service("orderAccessPolicy")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderAccessPolicy {
    private final UserRepository userRepository;

    public boolean canAccessCustomer(Authentication authentication, Long userId) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        boolean staff = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));
        if (staff) return true;
        boolean customer = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
        return customer && userId != null && userRepository.findByUsername(authentication.getName())
                .map(user -> userId.equals(user.getId())).orElse(false);
    }
}
