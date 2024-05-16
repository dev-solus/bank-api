package com.bank.app.components.operation.repository;

import com.bank.app.shared.repository.GenericRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bank.app.components.operation.model.*;

public interface OperationRepository extends GenericRepository<Operation, Long> { 
    @Query("""
        SELECT o
        FROM Operation o 
        WHERE :accountId = 0 OR (o.accountDebit.id = :accountId OR o.accountCredit.id = :accountId)
    """)
    Page<Operation> findAllQ(
        @Param("accountId") Long accountId, 
        Pageable pageable
    );
}

// record SelectDto(Long id) {}

