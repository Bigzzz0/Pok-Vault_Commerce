package com.pokevault.modules.web.dto.view;

import com.pokevault.domain.enums.CardType;
import com.pokevault.domain.enums.ElementType;
import com.pokevault.domain.enums.Rarity;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/** ข้อมูลหน้า /cards: การ์ดที่ผ่านตัวกรอง, ตัวกรองที่เลือก และ pill ของแต่ละแถว */
@Value
@Builder
public class CardGalleryPage {
    List<CardView> cards;
    ElementType selectedElement;
    Rarity selectedRarity;
    CardType selectedType;
    String search;
    String allElementsUrl;
    List<FilterOption> elementFilters;
    String allRaritiesUrl;
    List<FilterOption> rarityFilters;
    String allTypesUrl;
    List<FilterOption> typeFilters;
}
