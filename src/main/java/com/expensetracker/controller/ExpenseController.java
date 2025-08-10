package com.expensetracker.controller;

import com.expensetracker.model.Expense;
import com.expensetracker.service.ExpenseService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

	private final ExpenseService expenseService;

	public ExpenseController(ExpenseService expenseService) {
		this.expenseService = expenseService;
	}

	// Create a new expense
	@PreAuthorize("hasAuthority('ROLE_USER')")
	@PostMapping
	public ResponseEntity<Expense> createExpense(@RequestBody Expense expense, Principal principal) {
		Expense createdExpense = expenseService.create(expense, principal.getName());
		return ResponseEntity.ok(createdExpense);
	}

	// Get all expenses for logged-in user
	@PreAuthorize("hasAuthority('ROLE_USER')")
	@GetMapping
	public ResponseEntity<List<Expense>> getAllExpenses(Principal principal) {
		List<Expense> expenses = expenseService.getAll(principal.getName());
		return ResponseEntity.ok(expenses);
	}

	// Get an expense by ID
	@PreAuthorize("hasAuthority('ROLE_USER')")
	@GetMapping("/{id}")
	public ResponseEntity<Expense> getExpenseById(@PathVariable Long id, Principal principal) {
		Expense expense = expenseService.getById(id, principal.getName());
		return ResponseEntity.ok(expense);
	}

	// Update an expense
	@PreAuthorize("hasAuthority('ROLE_USER')")
	@PutMapping("/{id}")
	public ResponseEntity<Expense> updateExpense(@PathVariable Long id, @RequestBody Expense expense,
			Principal principal) {
		Expense updatedExpense = expenseService.update(id, expense, principal.getName());
		return ResponseEntity.ok(updatedExpense);
	}

	// Delete an expense
	@PreAuthorize("hasAuthority('ROLE_USER')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteExpense(@PathVariable Long id, Principal principal) {
		expenseService.delete(id, principal.getName());
		return ResponseEntity.noContent().build();
	}
}
