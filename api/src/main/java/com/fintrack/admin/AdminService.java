package com.fintrack.admin;

import com.fintrack.admin.dto.AdminOverview;
import com.fintrack.admin.dto.ExpenseRow;
import com.fintrack.admin.dto.SpendByCategory;
import com.fintrack.expense.Expense;
import com.fintrack.expense.ExpenseRepository;
import com.fintrack.exception.AppExceptions.ForbiddenException;
import com.fintrack.user.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final ExpenseRepository expenseRepo;

    public AdminService(ExpenseRepository expenseRepo) {
        this.expenseRepo = expenseRepo;
    }

    public AdminOverview getOverview(User current) {
        ensureAdmin(current);

        long pending = expenseRepo.countByStatus("PENDING");
        long approved = expenseRepo.countByStatus("APPROVED");
        long rejected = expenseRepo.countByStatus("REJECTED");

        var approvedTotal = expenseRepo.totalApprovedAmount();
        List<SpendByCategory> byCategory = expenseRepo.spendByCategory();

        var recentPage = expenseRepo.findRecent(PageRequest.of(0, 5));
        List<ExpenseRow> recent = recentPage.stream()
                .map(this::toRow)
                .toList();

        return new AdminOverview(pending, approved, rejected, approvedTotal, byCategory, recent);
    }

    private void ensureAdmin(User current) {
        boolean isAdmin = current.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getName()));
        if (!isAdmin) {
            throw new ForbiddenException("Admins only");
        }
    }

    private ExpenseRow toRow(Expense e) {
        String employeeName = e.getEmployee() != null ? e.getEmployee().getFullName() : "(unknown)";
        return new ExpenseRow(
                e.getId(),
                e.getTitle(),
                e.getAmount(),
                e.getCategory(),
                e.getStatus(),
                employeeName,
                e.getCreatedAt());
    }
}
