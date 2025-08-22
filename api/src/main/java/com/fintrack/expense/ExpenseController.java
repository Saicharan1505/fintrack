// package com.fintrack.expense;

// import com.fintrack.auth.CurrentUserResolver;
// import com.fintrack.expense.dto.ExpenseCreateRequest;
// import com.fintrack.expense.dto.ExpenseResponse;
// import com.fintrack.user.User;
// import jakarta.validation.Valid;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.context.request.NativeWebRequest;

// import java.util.List;

// @RestController
// @RequestMapping("/api/expenses")
// public class ExpenseController {
//     private final ExpenseRepository repo;
//     private final CurrentUserResolver current;
//     private final ExpenseService service;

//     public ExpenseController(ExpenseRepository repo, CurrentUserResolver current, ExpenseService service) {
//         this.repo = repo;
//         this.current = current;
//         this.service = service;
//     }

//     // ---- Create (uses email header -> service overload) ----
//     @PostMapping
//     public ExpenseResponse create(@Valid @RequestBody ExpenseCreateRequest req,
//             @RequestHeader("X-User-Email") String email) {
//         return service.create(req, email);
//     }

//     // ---- List my expenses ----
//     @GetMapping("/mine")
//     public List<ExpenseResponse> mine(NativeWebRequest web) {
//         User me = current.resolve(web);
//         return repo.findByEmployeeOrderByCreatedAtDesc(me).stream()
//                 .map(e -> new ExpenseResponse(e.getId(), e.getTitle(), e.getAmount(), e.getCategory(), e.getStatus()))
//                 .toList();
//     }

//     // ---- List pending (manager view) ----
//     @GetMapping("/pending")
//     public List<ExpenseResponse> pending(NativeWebRequest web) {
//         // You’re not using 'me' here now, but keep for future auth checks
//         User me = current.resolve(web);
//         return service.getPendingExpenses().stream()
//                 .map(e -> new ExpenseResponse(e.getId(), e.getTitle(), e.getAmount(), e.getCategory(), e.getStatus()))
//                 .toList();
//     }

//     // ---- Approve ----
//     @PostMapping("/{id}/approve")
//     public ExpenseResponse approve(@PathVariable Integer id, NativeWebRequest web) {
//         User me = current.resolve(web);
//         Expense e = service.approveExpense(id, me);
//         return new ExpenseResponse(e.getId(), e.getTitle(), e.getAmount(), e.getCategory(), e.getStatus());
//     }

//     // ---- Reject ----
//     @PostMapping("/{id}/reject")
//     public ExpenseResponse reject(@PathVariable Integer id, NativeWebRequest web) {
//         User me = current.resolve(web);
//         Expense e = service.rejectExpense(id, me);
//         return new ExpenseResponse(e.getId(), e.getTitle(), e.getAmount(), e.getCategory(), e.getStatus());
//     }
// }

// imports you may need
package com.fintrack.expense;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.fintrack.auth.CurrentUserResolver;
import com.fintrack.expense.dto.CreateExpenseRequest;
import com.fintrack.expense.dto.ExpenseCreateRequest;
import com.fintrack.expense.dto.ExpenseResponse;
import com.fintrack.user.User;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    public ExpenseResponse create(@Valid @RequestBody CreateExpenseRequest req,
            NativeWebRequest web) {
        // Resolve the logged-in user once
        User creator = current.resolve(web);

        // Pass the User directly to the service
        return service.create(req, creator);
    }

    // ---- List my expenses ----
    @GetMapping("/mine")
    public List<ExpenseResponse> mine(NativeWebRequest web) {
        User me = current.resolve(web);
        return repo.findByEmployeeOrderByCreatedAtDesc(me).stream()
                .map(e -> new ExpenseResponse(e.getId(), e.getTitle(), e.getAmount(), e.getCategory(), e.getStatus(),
                        e.getNotes(), // NEW
                        e.getReceiptUrl()))
                .toList();
    }

    // ---- List pending (manager view) ----
    @GetMapping("/pending")
    public List<ExpenseResponse> pending(NativeWebRequest web) {
        User me = current.resolve(web);
        return service.getPendingExpenses().stream()
                .map(e -> new ExpenseResponse(e.getId(), e.getTitle(), e.getAmount(), e.getCategory(), e.getStatus(),
                        e.getNotes(), // NEW
                        e.getReceiptUrl()))
                .toList();
    }

    // ---- Approve ----
    @PostMapping("/{id}/approve")
    public ExpenseResponse approve(@PathVariable Integer id, NativeWebRequest web) {
        User me = current.resolve(web);
        Expense e = service.approveExpense(id, me);
        return new ExpenseResponse(e.getId(), e.getTitle(), e.getAmount(), e.getCategory(), e.getStatus(), e.getNotes(), // NEW
                e.getReceiptUrl());
    }

    // ---- Reject ----
    @PostMapping("/{id}/reject")
    public ExpenseResponse reject(@PathVariable Integer id, NativeWebRequest web) {
        User me = current.resolve(web);
        Expense e = service.rejectExpense(id, me);
        return new ExpenseResponse(e.getId(), e.getTitle(), e.getAmount(), e.getCategory(), e.getStatus(), e.getNotes(), // NEW
                e.getReceiptUrl());
    }

    // ---- Upload receipt (image/pdf) ----
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> uploadReceipt(
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request) throws IOException {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        // Basic content-type allow list: images + PDFs
        String ct = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        boolean allowed = ct.startsWith("image/") || ct.equals("application/pdf");
        if (!allowed) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only images or PDF files are allowed"));
        }

        // Ensure local uploads dir exists
        String uploadDir = "uploads";
        File dir = new File(uploadDir);
        if (!dir.exists())
            dir.mkdirs();

        // Create a safe unique filename
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String cleanName = original.replaceAll("[\\\\/<>:\"|?*]+", "_");
        String storedName = UUID.randomUUID() + "_" + cleanName;

        Path target = Paths.get(uploadDir, storedName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // Public URL served by StaticResourceConfig below
        String relativeUrl = "/uploads/" + storedName;

        // Optionally build absolute URL
        String base = request.getScheme() + "://" + request.getServerName()
                + (request.getServerPort() > 0 ? ":" + request.getServerPort() : "");
        String absoluteUrl = base + relativeUrl;

        Map<String, String> payload = new HashMap<>();
        payload.put("url", relativeUrl);
        payload.put("absoluteUrl", absoluteUrl);

        return ResponseEntity.ok(payload);
    }
}
