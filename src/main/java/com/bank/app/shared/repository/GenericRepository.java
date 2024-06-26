package com.bank.app.shared.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;


@NoRepositoryBean
// @Repository
public interface GenericRepository<T, ID > extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {}
