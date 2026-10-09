package com.pokevault.common.security;

import com.pokevault.domain.entity.User;
import com.pokevault.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderAccessPolicyTest {
    @Mock UserRepository users;
    @Test void anonymousAndUnknownRolesAreDeniedWithoutLookup() {
        var policy = new OrderAccessPolicy(users);
        assertThat(policy.canAccessCustomer(null, 1L)).isFalse();
        assertThat(policy.canAccessCustomer(new AnonymousAuthenticationToken("key", "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")), 1L)).isFalse();
        assertThat(policy.canAccessCustomer(new UsernamePasswordAuthenticationToken("red", "password"), 1L)).isFalse();
        assertThat(policy.canAccessCustomer(new UsernamePasswordAuthenticationToken("red", "",
                AuthorityUtils.createAuthorityList("ROLE_UNKNOWN")), 1L)).isFalse();
        verifyNoInteractions(users);
    }
    @Test void deletedCustomerAccountAndNullIdAreDenied() {
        var policy = new OrderAccessPolicy(users);
        var auth = new UsernamePasswordAuthenticationToken("red", "", AuthorityUtils.createAuthorityList("ROLE_CUSTOMER"));
        assertThat(policy.canAccessCustomer(auth, null)).isFalse();
        when(users.findByUsername("red")).thenReturn(Optional.empty());
        assertThat(policy.canAccessCustomer(auth, 1L)).isFalse();
    }
    @Test void customerMustMatchDatabaseIdentity() {
        when(users.findByUsername("red")).thenReturn(Optional.of(User.builder().id(1L).build()));
        var auth = new UsernamePasswordAuthenticationToken("red", "", AuthorityUtils.createAuthorityList("ROLE_CUSTOMER"));
        var policy = new OrderAccessPolicy(users);
        assertThat(policy.canAccessCustomer(auth, 1L)).isTrue();
        assertThat(policy.canAccessCustomer(auth, 2L)).isFalse();
    }
}
