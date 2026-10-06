package com.pokevault.modules.vault.service;

import com.pokevault.domain.enums.AccountTradeStatus;
import com.pokevault.modules.vault.dto.AccountCardResponse;
import com.pokevault.modules.vault.dto.AddPulledCardRequest;
import com.pokevault.modules.vault.dto.GameAccountRequest;
import com.pokevault.modules.vault.dto.GameAccountResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class GameAccountServiceImpl implements GameAccountService {

    @Override
    public GameAccountResponse createAccount(GameAccountRequest request) {
        return null;
    }

    @Override
    public List<GameAccountResponse> getAllAccounts() {
        return null;
    }

    @Override
    public GameAccountResponse getAccountById(Long id) {
        return null;
    }

    @Override
    public AccountCardResponse addPulledCard(Long id, AddPulledCardRequest request) {
        return null;
    }

    @Override
    public List<AccountCardResponse> getAccountCards(Long id) {
        return null;
    }

    @Override
    public GameAccountResponse updateTradeStatus(Long id, AccountTradeStatus status) {
        return null;
    }

    @Override
    public BigDecimal calculateTotalVaultCostValue() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateTotalVaultSellingValue() {
        return BigDecimal.ZERO;
    }
}
