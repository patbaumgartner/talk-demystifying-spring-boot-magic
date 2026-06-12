-- V1: Initial claims table
--
-- Column naming follows Spring Data JDBC's default NamingStrategy:
--   camelCase Java field → snake_case column  (insuredId → insured_id)

CREATE TABLE IF NOT EXISTS claims
(
    id         VARCHAR(36)  NOT NULL PRIMARY KEY,
    type       VARCHAR(50)  NOT NULL,
    status     VARCHAR(20)  NOT NULL,
    insured_id VARCHAR(100) NOT NULL
);
