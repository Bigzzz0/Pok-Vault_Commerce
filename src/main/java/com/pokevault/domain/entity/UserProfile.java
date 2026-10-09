package com.pokevault.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pokevault.domain.enums.MembershipTier;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "full_name", length = 100)
    @Size(max = 100)
    private String fullName;

    @Column(name = "phone_number", length = 20)
    @Size(max = 20)
    private String phoneNumber;

    @Column(name = "shipping_address", length = 500)
    @Size(max = 500)
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_tier", nullable = false, length = 30)
    @Builder.Default
    private MembershipTier membershipTier = MembershipTier.REGULAR;

    @Column(name = "reward_points", nullable = false)
    @Min(0)
    @Builder.Default
    private Integer rewardPoints = 0;
}
