package com.bank.app.components.account.repository;

import com.bank.app.shared.repository.GenericRepository;

import static java.lang.StringTemplate.STR;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;

import com.bank.app.components.account.model.*;

public interface AccountRepository extends GenericRepository<Account, Long> {

    // @Query("""
    //     SELECT new com.bank.app.components.account.repository.SelectDto(
    //         e.id, 
    //         e.accountNumber, 
    //         CONCAT(e.user.firstname, ' ', e.user.lastname),
    //         e.user.cin
    //     )
    //     FROM Account e
    // """)
    // List<SelectDto> getForSelect();

    @Query("""
        SELECT new com.bank.app.components.account.repository.SelectDto(
            e.id, 
            CONCAT(e.firstname, ' ', e.lastname),
            
        )
        FROM User e
    """)
    List<SelectDto> getForSelectGroup();
}


record SelectDto(Long id, String name) {}

