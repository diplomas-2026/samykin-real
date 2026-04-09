package com.company.product.api.repository;

import com.company.product.api.entity.Payout;
import com.company.product.api.entity.PayoutStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PayoutRepository extends JpaRepository<Payout, Long> {

    Optional<Payout> findByPayoutCode(String payoutCode);

    @Query("""
        select p from Payout p
        join fetch p.employee e
        join fetch p.createdBy cb
        order by p.createdAt desc
        """)
    List<Payout> findAllDetailed();

    @Query("""
        select p from Payout p
        join fetch p.employee e
        join fetch p.createdBy cb
        where lower(e.email) = lower(:email)
        order by p.createdAt desc
        """)
    List<Payout> findAllDetailedByEmployeeEmail(@Param("email") String email);

    @Query("""
        select p from Payout p
        join fetch p.employee e
        join fetch p.createdBy cb
        where e.id = :employeeId
        order by p.createdAt desc
        """)
    List<Payout> findAllDetailedByEmployeeId(@Param("employeeId") Long employeeId);

    long countByStatus(PayoutStatus status);

    List<Payout> findAllByStatusOrderByCreatedAtDesc(PayoutStatus status);

    @Query("""
        select p from Payout p
        join fetch p.employee e
        join fetch p.createdBy cb
        where p.payoutDate between :from and :to
        order by p.payoutDate asc
        """)
    List<Payout> findAllByPayoutDateBetweenOrderByPayoutDateAsc(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
