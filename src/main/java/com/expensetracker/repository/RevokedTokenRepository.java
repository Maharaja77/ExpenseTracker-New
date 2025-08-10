package com.expensetracker.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.expensetracker.model.RevokedToken;

@Repository
public interface RevokedTokenRepository extends CrudRepository<RevokedToken, String> {
    boolean existsByToken(String token);
}
