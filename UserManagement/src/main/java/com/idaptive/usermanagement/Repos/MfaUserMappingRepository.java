package com.idaptive.usermanagement.Repos;

import com.idaptive.usermanagement.entity.MfaUserMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MfaUserMappingRepository extends JpaRepository<MfaUserMapping, Integer> {
}
