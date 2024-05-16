package com.bank.app.components.user.repository;

import com.bank.app.shared.dto.SelectDto;
import com.bank.app.shared.repository.GenericRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bank.app.components.account.model.Account;
import com.bank.app.components.user.model.*;

public interface UserRepository extends GenericRepository<User, Long> { 
    public Optional<User> findByEmail(String email);
    @Query("SELECT i.role_id, COUNT(i) FROM User i WHERE i.role_id = :role_id GROUP BY i.role_id")
    Long getUserCountByRoleId(@Param("role_id") Long role_id);

    @Query("SELECT new com.bank.app.shared.dto.SelectDto(u.id, CONCAT(u.firstname, ' ', u.lastname)) FROM User u")
    List<SelectDto> getForSelect();


    // @Query("""
    //         SELECT
    //             e.id, 
    //             CONCAT(e.firstname, ' ', e.lastname),
    //             a
    //         FROM User e
    //             LEFT JOIN e.accounts a
    //     """)
    // List<SelectGroupDto> getForSelectGroup();
}


record SelectGroupDto(Long id, String name, List<Account> accounts) {}
