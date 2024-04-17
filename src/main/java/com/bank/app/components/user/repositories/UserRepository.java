package com.bank.app.components.user.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bank.app.components.user.model.User;
import com.bank.app.shared.repositories.GenericRepository;

import java.util.Optional;

public interface UserRepository extends GenericRepository<User, Long> {
    public Optional<User> findByEmail(String email);
    @Query("SELECT i.roleId, COUNT(i) FROM User i WHERE i.roleId = :roleId GROUP BY i.roleId")
    Long getUserCountByRoleId(@Param("roleId") Long roleId);


}
