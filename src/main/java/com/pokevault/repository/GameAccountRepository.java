package com.pokevault.repository;

import com.pokevault.domain.entity.GameAccount;
import com.pokevault.domain.enums.AccountTradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameAccountRepository extends JpaRepository<GameAccount, Long> {

    Optional<GameAccount> findByAccountCode(String accountCode);

    List<GameAccount> findByTradeStatus(AccountTradeStatus tradeStatus);
}
