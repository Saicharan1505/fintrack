package com.fintrack.expense;

import com.fintrack.auth.CurrentUserResolver;
import com.fintrack.expense.dto.ExpenseCreateRequest;
import com.fintrack.expense.dto.ExpenseResponse;
import com.fintrack.user.User;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseRepository repo;
    private final CurrentUserResolver current;
    private final ExpenseService service;

    public ExpenseController(ExpenseRepository repo, CurrentUserResolver current, ExpenseService service) {
        this.repo = repo;
        this.current = current;
        this.service = service;
    }

    // ---- Create (uses email header -> service overload) ----
    @PostMapping
    public ExpenseResponse create(@Valid @RequestBody ExpenseCreateRequest req,
            @RequestHeader("X-User-Email") String email) {
        return service.create(req, email);
    }

    // ---- List my expenses ----
    @GetMapping("/mine")
    public List<ExpenseResponse> mine(NativeWebRequest web) {
        User me = current.resolve(web);
        return repo.findByEmployeeOrderByCreatedAtDesc(me).stream()
                .map(e -> new ExpenseResponse(e.getId(), e.getTitle(), e.getAmount(), e.getCategory(), e.getStatus()))
                .toList();
    }

    // ---- List pending (manager view) ----
    @GetMapping("/pending")
    public List<ExpenseResponse> pending(NativeWebRequest web) {
        // You’re not using 'me' here now, but keep for future auth checks
        User me = current.resolve(web);
        return service.getPendingExpenses().stream()
                .map(e -> new ExpenseResponse(e.getId(), e.getTitle(), e.getAmount(), e.getCategory(), e.getStatus()))
                .toList();
    }

    // ---- Approve ----
    @PostMapping("/{id}/approve")
    public ExpenseResponse approve(@PathVariable Integer id, NativeWebRequest web) {
        User me = current.resolve(web);
        Expense e = service.approveExpense(id, me);
        return new ExpenseResponse(e.getId(), e.getTitle(), e.getAmount(), e.getCategory(), e.getStatus());
    }

    // ---- Reject ----
    @PostMapping("/{id}/reject")
    public ExpenseResponse reject(@PathVariable Integer id, NativeWebRequest web) {
        User me = current.resolve(web);
        Expense e = service.rejectExpense(id, me);
        return new ExpenseResponse(e.getId(), e.getTitle(), e.getAmount(), e.getCategory(), e.getStatus());
    }
}
