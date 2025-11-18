package in.vishesh.expense_tracker.services;

import in.vishesh.expense_tracker.entity.Expense;
import in.vishesh.expense_tracker.entity.User;
import in.vishesh.expense_tracker.repository.ExpenseRepository;
import in.vishesh.expense_tracker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Expense> getAllExpenses(String email) {
        User user = userRepository.findByUsername(email).orElseThrow();
        return expenseRepository.findByUserId(user.getId());
    }

    public Expense createExpense(Expense expense, String email) {
        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        expense.setUser(user);
        return expenseRepository.save(expense);
    }

    public List<Expense> getExpensesByMonth(String email, int year, int month) {
        User user = userRepository.findByUsername(email).orElseThrow();

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return expenseRepository.findByUserIdAndDateBetween(user.getId(), start, end);
    }

    public Map<String, Double> getCategoryTotals(String email) {
        User user = userRepository.findByUsername(email).orElseThrow();
        List<Object[]> results = expenseRepository.findTotalByCategory(user.getId());

        Map<String, Double> categoryMap = new HashMap<>();
        for (Object[] result : results) {
            String category = (String) result[0];
            Double total = (Double) result[1];
            categoryMap.put(category, total);
        }
        return categoryMap;
    }

    public void deleteExpense(Long id, String email) {
        User user = userRepository.findByUsername(email).orElseThrow();

        Expense expense = expenseRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Expense not found or access denied"));

        expenseRepository.delete(expense);
    }

    public Map<String, Object> getDashboardStats(String email) {
        User user = userRepository.findByUsername(email).orElseThrow();
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        Double currentMonthSpent = expenseRepository.sumAmountByUserIdAndDateBetween(user.getId(), startOfMonth, endOfMonth);
        if (currentMonthSpent == null) currentMonthSpent = 0.0;

        LocalDate startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDate endOfLastMonth = startOfLastMonth.withDayOfMonth(startOfLastMonth.lengthOfMonth());
        Double lastMonthSpent = expenseRepository.sumAmountByUserIdAndDateBetween(user.getId(), startOfLastMonth, endOfLastMonth);
        if (lastMonthSpent == null) lastMonthSpent = 0.0;

        Double monthlyBudget = user.getBudget();
        if (monthlyBudget == null) monthlyBudget = 0.0;

        double remaining = monthlyBudget - currentMonthSpent;

        List<Expense> thisMonthExpenses = expenseRepository.findByUserIdAndDateBetween(user.getId(), startOfMonth, endOfMonth);

        Map<String, Double> dailyTrend = new TreeMap<>();
        for (Expense e : thisMonthExpenses) {
            String dateKey = e.getDate().toString();
            dailyTrend.put(dateKey, dailyTrend.getOrDefault(dateKey, 0.0) + e.getAmount());
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSpent", currentMonthSpent);
        stats.put("lastMonthSpent", lastMonthSpent);
        stats.put("budget", monthlyBudget);
        stats.put("remaining", remaining);
        stats.put("dailyTrend", dailyTrend);

        return stats;
    }
}