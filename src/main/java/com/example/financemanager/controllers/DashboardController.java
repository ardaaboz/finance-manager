package com.example.financemanager.controllers;

import com.example.financemanager.entities.Transaction;
import com.example.financemanager.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class DashboardController {
    @Autowired
    private TransactionService transactionService;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String type,
                            @RequestParam(required = false) String category,
                            Principal principal, Model model) {
        // Get logged in username
        String username = principal.getName();

        // Get filtered transactions
        List<Transaction> transactions = transactionService.getFilteredTransactions(username, type, category);

        // Get financial summary
        double totalIncome = transactionService.getTotalIncome(username);
        double totalExpense = transactionService.getTotalExpense(username);
        double balance = transactionService.getBalance(username);

        // Add data to model (sending it to the view)
        model.addAttribute("username", username);
        model.addAttribute("transactions", transactions);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("balance", balance);


        // Keep selected filters on the form
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedCategory", category);
        return "dashboard";
    }
}
