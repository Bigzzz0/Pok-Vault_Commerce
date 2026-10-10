package com.pokevault.modules.web.dto.view;

import com.pokevault.domain.enums.CardType;
import com.pokevault.domain.enums.ElementType;
import com.pokevault.domain.enums.Rarity;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/** การ์ดหนึ่งใบในแกลเลอรี / Spotlight พร้อมราคาและสต็อกรวมของร้าน */
@Value
@Builder
public class CardView {
    Long id;
    String name;
    String cardNumber;
    String expansionCode;
    Rarity rarity;
    CardType cardType;
    /** คำอธิบายระดับความหายาก ถ้า null หน้าเว็บจะแสดงชื่อ enum แทน */
    String rarityDescription;
    /** Trainer ไม่มีธาตุ จึงแสดงเป็น COLORLESS */
    ElementType elementType;
    Integer hp;
    String description;
    String imageUrl;
    int totalStock;
    /** ล็อตที่ใช้เปิดออเดอร์ให้ลูกค้า (ล็อตที่ถูกที่สุดที่ยังมีของ) */
    Long orderInventoryId;
    BigDecimal price;
}
