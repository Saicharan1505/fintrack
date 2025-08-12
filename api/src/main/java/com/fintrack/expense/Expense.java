package com.fintrack.expense;

import com.fintrack.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private User employee;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false)
    private String category;
    @Column(columnDefinition = "text")
    private String notes;
    @Column(nullable = false)
    private String status = "PENDING";
    @Column(name = "created_at", columnDefinition = "timestamptz")
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public User getEmployee() {
        return employee;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getNotes() {
        return notes;
    }

    public String getStatus() {
        return status;
    }

    public void setEmployee(User u) {
        this.employee = u;
    }

    public void setTitle(String t) {
        this.title = t;
    }

    public void setAmount(BigDecimal a) {
        this.amount = a;
    }

    public void setCategory(String c) {
        this.category = c;
    }

    public void setNotes(String n) {
        this.notes = n;
    }

    // --- Added for convenience ---
    public User getUser() {
        return this.employee;
    }

    public void setUser(User user) {
        this.employee = user;
    }
}
