package com.bank.app.components.account.repository;

import com.bank.app.shared.repository.GenericRepository;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bank.app.components.account.model.*;

public interface AccountRepository extends GenericRepository<Account, Long> {

    @Query("SELECT new com.bank.app.components.account.repository.SelectDto(u.id, u.accountNumber, u.balance, u.user_id) FROM Account u")
    List<SelectDto> getForSelect();

    // @Query("""
    // SELECT new com.bank.app.components.account.repository.SelectDto(
    // e.id,
    // CONCAT(e.firstname, ' ', e.lastname),
    // e.accounts
    // )
    // FROM User e
    // """)
    // List<SelectDto> getForSelectGroup();
}
