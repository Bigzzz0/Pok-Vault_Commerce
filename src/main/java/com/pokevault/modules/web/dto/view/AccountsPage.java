package com.pokevault.modules.web.dto.view;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/** ข้อมูลหน้า /accounts: บัญชีเกมพร้อมสรุปสถานะ และลูกค้าที่กำหนดระดับสมาชิกได้ */
@Value
@Builder
public class AccountsPage {
    List<GameAccountView> accounts;
    long readyAccounts;
    long cooldownAccounts;
    List<CustomerView> customers;
}
