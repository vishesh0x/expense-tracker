package in.vishesh.expense_tracker.controller;

import in.vishesh.expense_tracker.entity.Expense;
import in.vishesh.expense_tracker.services.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<List<Expense>> getAllExpenses(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(expenseService.getAllExpenses(email));
    }

    @PostMapping
    public ResponseEntity<Expense> createExpense(@RequestBody Expense expense, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(expenseService.createExpense(expense, email));
    }

    @GetMapping("/month")
    public ResponseEntity<List<Expense>> getMonthlyExpenses(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {

        String email = authentication.getName();
        return ResponseEntity.ok(expenseService.getExpensesByMonth(email, year, month));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Double>> getExpenseStats(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(expenseService.getCategoryTotals(email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        expenseService.deleteExpense(id, email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard-summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(expenseService.getDashboardStats(email));
    }
}