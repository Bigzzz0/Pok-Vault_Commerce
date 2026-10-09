package com.pokevault.modules.web.dto.view;

import lombok.Builder;
import lombok.Value;

/** pill ตัวกรองหนึ่งอันในหน้า /cards; url คงตัวกรองแถวอื่นและคำค้นไว้ */
@Value
@Builder
public class FilterOption {
    String name;
    String label;
    boolean active;
    String url;
}
