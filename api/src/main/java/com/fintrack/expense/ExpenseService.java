package com.fintrack.expense;

import com.fintrack.exception.AppExceptions.*; // domain exceptions: NotFoundException, ForbiddenException, ConflictException
import com.fintrack.expense.dto.CreateExpenseRequest;
import com.fintrack.expense.dto.ExpenseResponse;
import com.fintrack.user.User;
import com.fintrack.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
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

    // Create new expense (employee)
    @Transactional
    public ExpenseResponse create(CreateExpenseRequest req, User creator) {
        Expense expense = new Expense();
        expense.setTitle(req.title());
        expense.setAmount(req.amount());
        expense.setCategory(req.category());
        expense.setStatus("PENDING");
        expense.setUser(creator);

        expense.setNotes(req.notes());
        expense.setReceiptUrl(req.receiptUrl());
        expense.setCreatedAt(OffsetDateTime.now());

        Expense saved = expenseRepo.save(expense);

        return new ExpenseResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getAmount(),
                saved.getCategory(),
                saved.getStatus(),
                saved.getNotes(),
                saved.getReceiptUrl());
    }

    // Manager approves
    @Transactional
    public Expense approveExpense(Integer id, User manager) {
        assertManager(manager);

        Expense expense = expenseRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense with id=" + id + " does not exist"));

        if (!"PENDING".equals(expense.getStatus())) {
            throw new ConflictException(
                    "Expense " + id + " is not PENDING (current status: " + expense.getStatus() + ")");
        }

        expense.setStatus("APPROVED");
        return expenseRepo.save(expense);
    }

    // Manager rejects
    @Transactional
    public Expense rejectExpense(Integer expenseId, User manager) {
        assertManager(manager);
        Expense expense = getPendingExpenseOrThrow(expenseId);
        expense.setStatus("REJECTED");
        return expenseRepo.save(expense);
    }

    // Attach receipt to an existing expense
    @Transactional
    public void attachReceipt(Integer expenseId, String filename) {
        Expense expense = expenseRepo.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found"));

        // Always store a clean relative URL
        String receiptUrl = "/uploads/" + filename;
        expense.setReceiptUrl(receiptUrl);

        expenseRepo.save(expense);
    }

    // ---------- helpers ----------

    private void assertManager(User user) {
        // TODO: real role check later
        boolean isManager = user.getEmail().equals("alice.manager@demo.local");
        if (!isManager) {
            throw new ForbiddenException("Only managers can approve/reject expenses");
        }
    }

    private Expense getPendingExpenseOrThrow(Integer expenseId) {
        Expense e = expenseRepo.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense with id=" + expenseId + " does not exist"));

        if (!"PENDING".equals(e.getStatus())) {
            throw new ConflictException(
                    "Expense " + expenseId + " is not PENDING (current status: " + e.getStatus() + ")");
        }
        return e;
    }
}
