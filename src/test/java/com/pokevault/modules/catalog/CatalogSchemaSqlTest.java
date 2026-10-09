package com.pokevault.modules.catalog;

import com.pokevault.domain.entity.*;
import com.pokevault.domain.enums.*;
import com.pokevault.modules.catalog.service.UserService;
import com.pokevault.repository.*;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

/** Loads the real schema.sql and data.sql BEFORE Hibernate validates them. */
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:catalog-sql-tests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate", "spring.jpa.defer-datasource-initialization=false",
        "spring.sql.init.mode=always", "spring.sql.init.continue-on-error=false", "spring.jpa.show-sql=false"})
@ActiveProfiles("schema-test")
@Transactional
class CatalogSchemaSqlTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository users;
    @Autowired UserProfileRepository profiles;
    @Autowired CardRepository cards;
    @Autowired CardExpansionRepository expansions;
    @Autowired UserService userService;

    private Map<String, Object> column(String table, String column) {
        return jdbc.queryForMap("SELECT IS_NULLABLE, CHARACTER_MAXIMUM_LENGTH FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_NAME=? AND COLUMN_NAME=?", table.toUpperCase(), column.toUpperCase());
    }

    @Test void realSqlSeedContainsAtLeastTwentyCardsAndAllExpansionsHaveRequiredFields() {
        assertThat(cards.count()).isGreaterThanOrEqualTo(20);
        assertThat(expansions.findAll()).isNotEmpty().allSatisfy(expansion -> {
            assertThat(expansion.getSeries()).isNotBlank();
            assertThat(expansion.getTotalCards()).isPositive();
        });
    }

    @Test void schemaLengthsAndNullabilityMatchCoreEntityContract() {
        assertThat(column("user_profiles", "full_name").get("IS_NULLABLE")).isEqualTo("YES");
        assertThat(((Number) column("user_profiles", "shipping_address").get("CHARACTER_MAXIMUM_LENGTH")).intValue()).isEqualTo(500);
        assertThat(((Number) column("user_profiles", "membership_tier").get("CHARACTER_MAXIMUM_LENGTH")).intValue()).isEqualTo(30);
        assertThat(((Number) column("cards", "card_number").get("CHARACTER_MAXIMUM_LENGTH")).intValue()).isEqualTo(20);
        assertThat(column("cards", "element_type").get("IS_NULLABLE")).isEqualTo("YES");
        assertThat(column("card_expansions", "series").get("IS_NULLABLE")).isEqualTo("NO");
        assertThat(((Number) column("card_expansions", "series").get("CHARACTER_MAXIMUM_LENGTH")).intValue()).isEqualTo(50);
        assertThat(column("card_expansions", "total_cards").get("IS_NULLABLE")).isEqualTo("NO");
    }

    private User customer() {
        return users.saveAndFlush(User.builder().username("sql_customer").email("sql-customer@example.test")
                .password("synthetic-hash").build());
    }

    @Test void addressServiceCanCreateProfileWithoutFullNameAgainstRealSql() {
        User user = customer();
        var result = userService.updateShippingAddress(user.getId(), "x".repeat(500));
        profiles.flush();
        assertThat(result.getShippingAddress()).hasSize(500);
        assertThat(profiles.findByUserId(user.getId())).hasValueSatisfying(profile -> {
            assertThat(profile.getFullName()).isNull();
            assertThat(profile.getMembershipTier()).isEqualTo(MembershipTier.REGULAR);
        });
    }

    @Test void overlongAddressIsRejectedBeforeCreatingProfile() {
        User user = customer();
        assertThatThrownBy(() -> userService.updateShippingAddress(user.getId(), "x".repeat(501)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThat(profiles.findByUserId(user.getId())).isEmpty();
    }

    @Test void cardNumberAtMaximumLengthAndNullTrainerElementCanBePersisted() {
        CardExpansion expansion = expansions.findAll().get(0);
        Card card = cards.saveAndFlush(Card.builder().expansion(expansion).cardNumber("x".repeat(20))
                .name("Test Trainer").cardType(CardType.TRAINER_ITEM).rarity(Rarity.COMMON).build());
        assertThat(card.getId()).isNotNull();
        assertThat(card.getElementType()).isNull();
    }

    @Test void sqlRejectsCardNumberLongerThanTwentyEvenWithoutRequestValidation() {
        CardExpansion expansion = expansions.findAll().get(0);
        assertThatThrownBy(() -> cards.saveAndFlush(Card.builder().expansion(expansion).cardNumber("x".repeat(21))
                .name("Too Long").cardType(CardType.POKEMON).rarity(Rarity.COMMON).build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
