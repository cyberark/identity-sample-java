/*
 * Copyright (c) 2021 CyberArk Software Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sampleapp.Repos;

import com.sampleapp.entity.TokenStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenStoreRepository extends JpaRepository<TokenStore, Integer> {

    @Query("SELECT t FROM TokenStore t WHERE t.sessionUuid = :sessionUuid")
    TokenStore findBySession(@Param("sessionUuid") String sessionUuid);

    @Query("SELECT t FROM TokenStore t WHERE t.mfaToken = :mfaToken")
    TokenStore findByToken(@Param("mfaToken") String mfaToken);
}
