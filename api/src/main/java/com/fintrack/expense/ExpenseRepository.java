package com.fintrack.expense;

import com.fintrack.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
    List<Expense> findByEmployeeOrderByCreatedAtDesc(User employee);

    List<Expense> findByStatusOrderByCreatedAtAsc(String status);

}
