package com.pokevault.modules.catalog;

import com.pokevault.domain.entity.*;
import com.pokevault.domain.enums.*;
import com.pokevault.modules.catalog.dto.*;
import com.pokevault.modules.catalog.service.CardServiceImpl;
import com.pokevault.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;

/** Real JPA queries/constraints on a fresh H2 database; each test rolls back. */
@DataJpaTest(properties = {"spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"}, showSql = false)
@ActiveProfiles("test")
@Import(CardServiceImpl.class)
class CatalogPersistenceTest {
    @Autowired TestEntityManager em;
    @Autowired CardRepository cards;
    @Autowired UserRepository users;
    @Autowired UserProfileRepository profiles;
    @Autowired CardServiceImpl service;

    private CardExpansion expansion(String code) {
        return em.persistAndFlush(CardExpansion.builder().code(code).name("Set " + code).series("Pocket").totalCards(226).build());
    }
    private Card card(CardExpansion expansion, String number, String name, ElementType element) {
        return em.persistAndFlush(Card.builder().expansion(expansion).cardNumber(number).name(name)
                .cardType(CardType.POKEMON).rarity(Rarity.COMMON).elementType(element).hp(60).build());
    }

    @Test void realSearchAndExpansionQueriesFindMatchingCards() {
        var a1 = expansion("A1");
        var a2 = expansion("A2");
        card(a1, "001", "Pikachu", ElementType.LIGHTNING);
        card(a2, "001", "Charmander", ElementType.FIRE);
        em.clear();
        assertThat(cards.findByNameContainingIgnoreCase("PIKA")).extracting(Card::getName).containsExactly("Pikachu");
        assertThat(cards.findByExpansionCode("A1")).extracting(Card::getName).containsExactly("Pikachu");
        assertThat(cards.findByExpansionIdAndCardNumber(a1.getId(), "001")).isPresent();
    }

    @Test void realSpecificationCombinesAllFiltersAndSortsBeforePaging() {
        var a1 = expansion("A1");
        var a2 = expansion("A2");
        card(a1, "001", "Pikachu Z", ElementType.LIGHTNING);
        card(a1, "002", "Pikachu A", ElementType.LIGHTNING);
        card(a1, "003", "Charmander", ElementType.FIRE);
        card(a2, "001", "Pikachu Other", ElementType.LIGHTNING);
        em.clear();
        var filter = CardFilterRequest.builder().name(" PIKA ").expansionCode(" a1 ")
                .rarity(" common ").cardType(" pokemon ").elementType(" lightning ").build();
        var first = service.getCardsPaged(filter, PageRequest.of(0, 1, Sort.by("name")));
        var second = service.getCardsPaged(filter, PageRequest.of(1, 1, Sort.by("name")));
        assertThat(first.getContent()).extracting(CardResponse::getName).containsExactly("Pikachu A");
        assertThat(first.getTotalElements()).isEqualTo(2);
        assertThat(first.getTotalPages()).isEqualTo(2);
        assertThat(first.isFirst()).isTrue();
        assertThat(first.isLast()).isFalse();
        assertThat(second.getContent()).extracting(CardResponse::getName).containsExactly("Pikachu Z");
        assertThat(second.isLast()).isTrue();
    }

    @Test void duplicateCardNumberWithinSameExpansionIsRejected() {
        var a1 = expansion("A1");
        card(a1, "001", "Pikachu", ElementType.LIGHTNING);
        assertThatThrownBy(() -> cards.saveAndFlush(Card.builder().expansion(a1).cardNumber("001")
                .name("Duplicate").cardType(CardType.POKEMON).rarity(Rarity.COMMON).build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test void sameCardNumberInDifferentExpansionsIsAllowed() {
        card(expansion("A1"), "001", "Pikachu", ElementType.LIGHTNING);
        card(expansion("A2"), "001", "Pikachu", ElementType.LIGHTNING);
        assertThat(cards.count()).isEqualTo(2);
    }

    @Test void userProfileCascadesAndCanBeQueriedAfterReload() {
        User user = User.builder().username("red").email("red@example.test").password("synthetic-hash").build();
        user.setUserProfile(UserProfile.builder().user(user).fullName("Red")
                .membershipTier(MembershipTier.VIP).rewardPoints(50).build());
        user = users.saveAndFlush(user);
        Long id = user.getId();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        em.clear();
        assertThat(users.findByUsername("red")).isPresent();
        assertThat(users.findByEmail("red@example.test")).isPresent();
        assertThat(users.existsByUsername("red")).isTrue();
        assertThat(users.existsByEmail("missing@example.test")).isFalse();
        assertThat(profiles.findByUserId(id)).hasValueSatisfying(profile -> {
            assertThat(profile.getMembershipTier()).isEqualTo(MembershipTier.VIP);
            assertThat(profile.getRewardPoints()).isEqualTo(50);
            assertThat(profile.getUser().getId()).isEqualTo(id);
        });
    }

    @Test void removingUserProfileDeletesOrphan() {
        User user = User.builder().username("red").email("red@example.test").password("synthetic-hash").build();
        user.setUserProfile(UserProfile.builder().user(user).build());
        user = users.saveAndFlush(user);
        Long id = user.getId();
        user.setUserProfile(null);
        users.flush();
        em.clear();
        assertThat(profiles.findByUserId(id)).isEmpty();
    }

    @Test void auditCreationTimestampIsRetainedOnUpdate() {
        Card saved = card(expansion("A1"), "001", "Pikachu", ElementType.LIGHTNING);
        Long id = saved.getId();
        em.clear();
        saved = cards.findById(id).orElseThrow();
        var created = saved.getCreatedAt();
        var updated = saved.getUpdatedAt();
        assertThat(created).isNotNull();
        assertThat(updated).isNotNull();
        saved.setName("Pikachu Updated");
        cards.flush();
        em.clear();
        var loaded = cards.findById(id).orElseThrow();
        assertThat(loaded.getCreatedAt()).isEqualTo(created);
        assertThat(loaded.getUpdatedAt()).isAfterOrEqualTo(updated);
        assertThat(loaded.getName()).isEqualTo("Pikachu Updated");
    }
}
