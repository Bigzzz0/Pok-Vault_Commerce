package com.pokevault.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "card_expansions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardExpansion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "series", nullable = false, length = 50)
    private String series;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "total_cards", nullable = false)
    private Integer totalCards;

    @OneToMany(mappedBy = "expansion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Card> cards = new ArrayList<>();
}
