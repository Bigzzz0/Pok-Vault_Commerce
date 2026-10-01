package com.pokevault.domain.entity;

import java.math.BigDecimal;
import java.util.Objects;

import com.pokevault.common.exception.InsufficientStockException;
import com.pokevault.domain.enums.CardCondition;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "card_inventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardInventory extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "card_id", nullable = false)
	private Card card;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_account_id")
	private GameAccount gameAccount;

	@Enumerated(EnumType.STRING)
	@Column(name = "card_condition", nullable = false, length = 30)
	@Builder.Default
	private CardCondition condition = CardCondition.MINT;

	@Column(name = "quantity", nullable = false)
	@Builder.Default
	private int quantity = 0;

	@Column(name = "buy_in_price", nullable = false, precision = 10, scale = 2)
	@Builder.Default
	private BigDecimal buyInPrice = BigDecimal.ZERO;

	@Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
	@Builder.Default
	private BigDecimal sellingPrice = BigDecimal.ZERO;

	@Column(name = "storage_slot", length = 50)
	private String storageSlot;

	public boolean hasSufficientStock(int requestedQuantity) {
		validatePositiveQuantity(requestedQuantity);
		return quantity >= requestedQuantity;
	}

	public void deductStock(int count) {
		validatePositiveQuantity(count);
		if (!hasSufficientStock(count)) {
			throw new InsufficientStockException(
					"Requested " + count + " cards, but only " + quantity + " are in stock"
			);
		}
		quantity -= count;
	}

	public void restoreStock(int count) {
		validatePositiveQuantity(count);
		quantity += count;
	}

	public void setQuantity(int quantity) {
		if (quantity < 0) {
			throw new IllegalArgumentException("Inventory quantity cannot be negative");
		}
		this.quantity = quantity;
	}

	public void setBuyInPrice(BigDecimal buyInPrice) {
		this.buyInPrice = validateNonNegativePrice(buyInPrice, "Buy-in price");
	}

	public void setSellingPrice(BigDecimal sellingPrice) {
		this.sellingPrice = validateNonNegativePrice(sellingPrice, "Selling price");
	}

	@PrePersist
	@PreUpdate
	private void validateInventory() {
		if (quantity < 0) {
			throw new IllegalArgumentException("Inventory quantity cannot be negative");
		}
		Objects.requireNonNull(card, "Card is required for an inventory item");
		Objects.requireNonNull(condition, "Card condition is required");
		validateNonNegativePrice(buyInPrice, "Buy-in price");
		validateNonNegativePrice(sellingPrice, "Selling price");
	}

	private static void validatePositiveQuantity(int count) {
		if (count <= 0) {
			throw new IllegalArgumentException("Quantity must be greater than zero");
		}
	}

	private static BigDecimal validateNonNegativePrice(BigDecimal price, String fieldName) {
		Objects.requireNonNull(price, fieldName + " is required");
		if (price.signum() < 0) {
			throw new IllegalArgumentException(fieldName + " cannot be negative");
		}
		return price;
	}
}
