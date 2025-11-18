package in.vishesh.expense_tracker.controller;

import in.vishesh.expense_tracker.entity.User;
import in.vishesh.expense_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PutMapping("/budget")
    public ResponseEntity<?> updateBudget(@RequestBody Map<String, Double> payload, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByUsername(email).orElseThrow();

        Double newBudget = payload.get("budget");
        user.setBudget(newBudget);
        userRepository.save(user);

        return ResponseEntity.ok("Budget updated");
    }
}