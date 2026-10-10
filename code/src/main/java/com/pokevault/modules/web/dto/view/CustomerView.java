package com.pokevault.modules.web.dto.view;

import com.pokevault.domain.enums.MembershipTier;
import lombok.Builder;
import lombok.Value;

/** ลูกค้าในฟอร์มจองของพนักงาน และตารางระดับสมาชิก */
@Value
@Builder
public class CustomerView {
    Long id;
    String username;
    String email;
    String displayName;
    MembershipTier membershipTier;
}
