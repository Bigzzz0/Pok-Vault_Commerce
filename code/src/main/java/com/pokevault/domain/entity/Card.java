package com.pokevault.domain.entity;

import com.pokevault.domain.enums.CardType;
import com.pokevault.domain.enums.ElementType;
import com.pokevault.domain.enums.Rarity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cards", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"expansion_id", "card_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "expansion_id", nullable = false)
    private CardExpansion expansion;

    @Column(name = "card_number", nullable = false, length = 20)
    private String cardNumber;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false, length = 30)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", nullable = false, length = 30)
    private Rarity rarity;

    @Enumerated(EnumType.STRING)
    @Column(name = "element_type", length = 30)
    private ElementType elementType;

    @Column(name = "hp")
    private Integer hp;

    @Column(name = "retreat_cost")
    private Integer retreatCost;

    @Column(name = "image_url", length = 500)
    private String imageUrl;
}
