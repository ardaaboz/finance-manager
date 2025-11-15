package com.example.financemanager.controllers;

import com.example.financemanager.entities.Transaction;
import com.example.financemanager.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;

@Controller
public class BillController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/bills")
    public String viewBills(@RequestParam(required = false, defaultValue = "all") String filter,
                            @RequestParam(required = false, defaultValue = "list") String view,
                            @RequestParam(required = false) Integer year,
                            @RequestParam(required = false) Integer month,
                            Principal principal,
                            Model model) {
        String username = principal.getName();

        LocalDate today = LocalDate.now();
        if (year == null) year = today.getYear();
        if (month == null) month = today.getMonthValue();

        // Get filtered bills
        List<Transaction> bills = transactionService.getFilteredBills(username, filter);

        // Get calendar data (includes both recurring and one-time with due dates)
        Map<Integer, List<Transaction>> calendar = transactionService.getTransactionsCalendar(username, year, month);

        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        int firstDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();
        int daysInMonth = firstDayOfMonth.lengthOfMonth();

        model.addAttribute("bills", bills);
        model.addAttribute("filter", filter);
        model.addAttribute("view", view);
        model.addAttribute("calendar", calendar);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("monthName", Month.of(month).name());
        model.addAttribute("firstDayOfWeek", firstDayOfWeek);
        model.addAttribute("daysInMonth", daysInMonth);
        model.addAttribute("today", today);

        return "bills";
    }

    @GetMapping("/mark-paid")
    public String markTransactionPaid(@RequestParam Long id,
                                      @RequestParam(required = false, defaultValue = "all") String filter,
                                      @RequestParam(required = false, defaultValue = "list") String view,
                                      @RequestParam(required = false) Integer year,
                                      @RequestParam(required = false) Integer month,
                                      Principal principal) {
        String username = principal.getName();
        transactionService.markTransactionPaid(id, username);

        if ("calendar".equals(view) && year != null && month != null) {
            return "redirect:/bills?view=calendar&year=" + year + "&month=" + month;
        }

        return "redirect:/bills?filter=" + filter + "&view=" + view;
    }

    @GetMapping("/quick-add-transaction")
    public String showQuickAddForm(@RequestParam Integer year,
                                   @RequestParam Integer month,
                                   @RequestParam(required = false) Integer day,
                                   Model model) {
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("day", day);
        model.addAttribute("monthName", Month.of(month).name());

        // Set default due date if day is provided
        if (day != null) {
            LocalDate defaultDate = LocalDate.of(year, month, day);
            model.addAttribute("defaultDueDate", defaultDate);
        }

        return "quick-add-transaction";
    }
}