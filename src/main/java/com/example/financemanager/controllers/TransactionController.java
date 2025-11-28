package com.example.financemanager.controllers;

import java.security.Principal;
import java.time.LocalDate;

import com.example.financemanager.entities.Transaction;
import com.example.financemanager.services.TransactionService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Validated
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @GetMapping("/add-transaction")
    public String addTransactionForm() {
        return "add-transaction";
    }

    @PostMapping("/add-transaction")
    public String addTransaction(@RequestParam @NotBlank(message = "Description is required") String description,
                                 @RequestParam @Positive(message = "Amount must be greater than 0") double amount,
                                 @RequestParam @NotBlank(message = "Type is required") String type,
                                 @RequestParam @NotBlank(message = "Category is required") String category,
                                 @RequestParam(required = false) Boolean isRecurring,
                                 @RequestParam(required = false) Integer dayOfMonth,
                                 @RequestParam(required = false) String dueDate,
                                 @RequestParam(required = false) String returnView,
                                 @RequestParam(required = false) Integer returnYear,
                                 @RequestParam(required = false) Integer returnMonth,
                                 Principal principal) {

        // Get logged-in username
        String username = principal.getName();

        // Check if it's a recurring transaction
        if (isRecurring != null && isRecurring && dayOfMonth != null) {
            transactionService.createRecurringTransaction(username, description, amount,
                    type, category, dayOfMonth);
        }
        // Check if it has a due date
        else if (dueDate != null && !dueDate.isEmpty()) {
            LocalDate parsedDueDate = LocalDate.parse(dueDate);
            transactionService.createTransactionWithDueDate(username, description, amount,
                    type, category, parsedDueDate);
        }
        // Regular transaction
        else {
            transactionService.createTransaction(username, description, amount, type, category);
        }

        // Return to calendar view if that's where they came from
        if ("calendar".equals(returnView) && returnYear != null && returnMonth != null) {
            return "redirect:/bills?view=calendar&year=" + returnYear + "&month=" + returnMonth;
        }

        // Redirect to dashboard
        return "redirect:/dashboard";
    }

    @GetMapping("/delete-transaction")
    public String deleteTransaction(@RequestParam long id, Principal principal) {
        String username = principal.getName();
        transactionService.deleteTransaction(id, username);
        return "redirect:/dashboard";
    }

    // Edit transaction
    @GetMapping("/edit-transaction")
    public String showEditTransactionForm(@RequestParam Long id,
                                          Principal principal,
                                          Model model) {
        String username = principal.getName();
        Transaction transaction = transactionService.getTransactionById(id, username);

        model.addAttribute("transaction", transaction);
        return "edit-transaction";
    }

    @PostMapping("/edit-transaction")
    public String editTransaction(@RequestParam Long id,
                                  @RequestParam @NotBlank(message = "Description is required") String description,
                                  @RequestParam @Positive(message = "Amount must be greater than 0") double amount,
                                  @RequestParam @NotBlank(message = "Type is required") String type,
                                  @RequestParam @NotBlank(message = "Category is required") String category,
                                  @RequestParam(required = false) Boolean isRecurring,
                                  @RequestParam(required = false) Integer dayOfMonth,
                                  Principal principal) {

        String username = principal.getName();
        transactionService.updateTransaction(id, username, description, amount,
                type, category, isRecurring, dayOfMonth);

        return "redirect:/dashboard";
    }
}