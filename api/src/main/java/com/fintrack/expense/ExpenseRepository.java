package com.fintrack.expense;

import com.fintrack.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

    List<Expense> findByEmployeeOrderByCreatedAtDesc(User employee);

    List<Expense> findByStatusOrderByCreatedAtAsc(String status);

    long countByStatus(String status);

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.status = 'APPROVED'")
    BigDecimal totalApprovedAmount();

    @Query("""
            select new com.fintrack.admin.dto.SpendByCategory(e.category, sum(e.amount))
            from Expense e
            where e.status = 'APPROVED'
            group by e.category
            order by sum(e.amount) desc
            """)
    List<com.fintrack.admin.dto.SpendByCategory> spendByCategory();

    @Query("select e from Expense e order by e.createdAt desc")
    Page<Expense> findRecent(Pageable pageable);
}
