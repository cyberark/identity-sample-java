package com.idaptive.usermanagement.Repos;

import com.idaptive.usermanagement.entity.TokenStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenStoreRepository extends JpaRepository<TokenStore, Integer> {

    @Query("SELECT t FROM TokenStore t WHERE t.sessionUuid = :sessionUuid")
    TokenStore findBySession(@Param("sessionUuid") String sessionUuid);

    @Query("SELECT t FROM TokenStore t WHERE t.mfaToken = :mfaToken")
    TokenStore findByToken(@Param("mfaToken") String mfaToken);
}
