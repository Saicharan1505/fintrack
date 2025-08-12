
package com.fintrack.expense;

import com.fintrack.exception.AppExceptions.*; // domain exceptions: NotFoundException, ForbiddenException, ConflictException
import com.fintrack.expense.dto.ExpenseCreateRequest;
import com.fintrack.expense.dto.ExpenseResponse;
import com.fintrack.user.User;
import com.fintrack.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepo;
    private final UserRepository userRepo;

    public ExpenseService(ExpenseRepository expenseRepo, UserRepository userRepo) {
        this.expenseRepo = expenseRepo;
        this.userRepo = userRepo;
    }

    public List<Expense> getPendingExpenses() {
        return expenseRepo.findByStatusOrderByCreatedAtAsc("PENDING");
    }

    // --- NEW: overload so controllers can pass email directly ---
    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest req, String email) {
        User creator = userRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email=" + email + " does not exist"));
        return create(req, creator); // delegate to the existing method
    }

    // Existing: create with resolved User
    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest req, User creator) {
        Expense expense = new Expense();
        expense.setTitle(req.getTitle());
        expense.setAmount(req.getAmount());
        expense.setCategory(req.getCategory());
        expense.setStatus("PENDING"); // default for new
        expense.setUser(creator); // uses your convenience setter that maps to 'employee'

        Expense saved = expenseRepo.save(expense);

        return new ExpenseResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getAmount(),
                saved.getCategory(),
                saved.getStatus());
    }

    // @Transactional
    // public Expense approveExpense(Integer expenseId, User manager) {
    // assertManager(manager);
    // Expense expense = getPendingExpenseOrThrow(expenseId);
    // expense.setStatus("APPROVED");
    // return expenseRepo.save(expense);
    // }

    @Transactional
    public Expense approveExpense(Integer id, User manager) {
        // Reuse your existing role check
        assertManager(manager); // throws ForbiddenException if not allowed

        // 404 if expense doesn't exist
        Expense expense = expenseRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense with id=" + id + " does not exist"));

        // Only allow transition from PENDING -> APPROVED
        if (!"PENDING".equals(expense.getStatus())) {
            throw new ConflictException(
                    "Expense " + id + " is not PENDING (current status: " + expense.getStatus() + ")");
        }

        expense.setStatus("APPROVED");
        // TODO: write an audit log if you have that table
        return expenseRepo.save(expense);
    }

    @Transactional
    public Expense rejectExpense(Integer expenseId, User manager) {
        assertManager(manager);
        Expense expense = getPendingExpenseOrThrow(expenseId);
        expense.setStatus("REJECTED");
        return expenseRepo.save(expense);
    }

    // ---------- helpers

    private void assertManager(User user) {
        // TODO: replace this hard-coded check with a real role check later
        boolean isManager = user.getEmail().equals("alice.manager@demo.local")
                || user.getEmail().equals("ada.admin@demo.local");

        if (!isManager) {
            // 403 -> handled by GlobalExceptionHandler
            throw new ForbiddenException("Only managers can approve/reject expenses");
        }
    }

    private Expense getPendingExpenseOrThrow(Integer expenseId) {
        // 404 if not found
        Expense e = expenseRepo.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense with id=" + expenseId + " does not exist"));

        // 409 if not in the right state
        if (!"PENDING".equals(e.getStatus())) {
            throw new ConflictException(
                    "Expense " + expenseId + " is not PENDING (current status: " + e.getStatus() + ")");
        }
        return e;
    }
}
