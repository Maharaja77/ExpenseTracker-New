package com.expensetracker.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;

@Service
public class ExpenseService {

	@Autowired
	private ExpenseRepository expenseRepo;

	@Autowired
	private UserRepository userRepo;

	// Create Expense
	public Expense create(Expense expense, String username) {
		User user = userRepo.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("User not found: " + username));
		expense.setUser(user);
		return expenseRepo.save(expense);
	}

	// Get all expenses for a user
	public List<Expense> getAll(String username) {
		User user = userRepo.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("User not found: " + username));
		return expenseRepo.findByUserId(user.getId());
	}

	// Get single expense by ID (with ownership check)
	public Expense getById(Long id, String username) {
		Expense expense = expenseRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("Expense not found with ID: " + id));
		if (!expense.getUser().getUsername().equals(username)) {
			throw new RuntimeException("Access denied.");
		}
		return expense;
	}

	// Update an existing expense (with ownership check)
	public Expense update(Long id, Expense newExpense, String username) {
		Expense existingExpense = getById(id, username); // includes ownership check
		existingExpense.setDescription(newExpense.getDescription());
		existingExpense.setAmount(newExpense.getAmount());
		existingExpense.setDate(newExpense.getDate());
		return expenseRepo.save(existingExpense);
	}

	// Delete an expense (with ownership check)
	public void delete(Long id, String username) {
		Expense expense = getById(id, username); // includes ownership check
		expenseRepo.delete(expense);
	}
}
