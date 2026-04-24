package com.bankx.demo.security.repository;

import com.bankx.demo.common.enums.RoleEnum;
import com.bankx.demo.security.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleEnum name);

}
