package com.company.product.api.repository;

import com.company.product.api.entity.UserAccount;
import com.company.product.api.entity.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    Optional<UserAccount> findByEmployeeCode(String employeeCode);

    List<UserAccount> findAllByRoleOrderByFullNameAsc(UserRole role);
}
