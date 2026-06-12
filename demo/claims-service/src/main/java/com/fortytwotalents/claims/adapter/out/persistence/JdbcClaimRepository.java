package com.fortytwotalents.claims.adapter.out.persistence;

import org.springframework.data.repository.CrudRepository;

/**
 * Spring Data JDBC repository for {@link ClaimEntity} – internal to the persistence
 * adapter. The public contract is
 * {@link com.fortytwotalents.claims.application.port.out.ClaimRepository}.
 */
interface JdbcClaimRepository extends CrudRepository<ClaimEntity, String> {

}
