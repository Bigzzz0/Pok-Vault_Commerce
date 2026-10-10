package com.pokevault.domain.entity;

import com.pokevault.common.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardInventoryTest {

    @Test
    void deductStockReducesQuantityWhenStockIsAvailable() {
        CardInventory inventory = CardInventory.builder().quantity(3).build();

        inventory.deductStock(2);

        assertThat(inventory.getQuantity()).isEqualTo(1);
    }

    @Test
    void deductStockRejectsRequestsExceedingAvailableQuantity() {
        CardInventory inventory = CardInventory.builder().quantity(1).build();

        assertThatThrownBy(() -> inventory.deductStock(2))
                .isInstanceOf(InsufficientStockException.class);
        assertThat(inventory.getQuantity()).isEqualTo(1);
    }

    @Test
    void stockChangesRequirePositiveQuantities() {
        CardInventory inventory = CardInventory.builder().quantity(1).build();

        assertThatThrownBy(() -> inventory.deductStock(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> inventory.restoreStock(-1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(inventory.getQuantity()).isEqualTo(1);
    }

    @Test
    void restoreStockIncreasesQuantity() {
        CardInventory inventory = CardInventory.builder().quantity(1).build();

        inventory.restoreStock(2);

        assertThat(inventory.getQuantity()).isEqualTo(3);
    }

    @Test
    void pricesCannotBeNegative() {
        CardInventory inventory = new CardInventory();

        assertThatThrownBy(() -> inventory.setBuyInPrice(new BigDecimal("-0.01")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> inventory.setSellingPrice(new BigDecimal("-0.01")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}