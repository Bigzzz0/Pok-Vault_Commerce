package com.pokevault.modules.web.dto.view;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/** ข้อมูลหน้า /inventory: สต็อกทุกล็อต และลูกค้าที่พนักงานจองแทนได้ */
@Value
@Builder
public class InventoryPage {
    List<InventoryView> inventories;
    List<CustomerView> customers;
}
