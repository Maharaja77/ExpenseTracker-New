package com.expensetracker.service;

import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ExpenseServiceTest {

    private ExpenseRepository expenseRepo;
    private UserRepository userRepo;
    private ExpenseService expenseService;

    private User mockUser;
    private Expense mockExpense;

    @BeforeEach
    void setup() {
        expenseRepo = mock(ExpenseRepository.class);
        userRepo = mock(UserRepository.class);
        expenseService = new ExpenseService();

        // Inject mocks
        injectField(expenseService, "expenseRepo", expenseRepo);
        injectField(expenseService, "userRepo", userRepo);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");

        mockExpense = new Expense();
        mockExpense.setId(1L);
        mockExpense.setDescription("Test Expense");
        mockExpense.setAmount(BigDecimal.valueOf(100.00));
        mockExpense.setDate(LocalDate.now());
        mockExpense.setUser(mockUser);
    }

    @Test
    void testCreateExpense() {
        when(userRepo.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        when(expenseRepo.save(any(Expense.class))).thenReturn(mockExpense);

        Expense saved = expenseService.create(mockExpense, "testUser");

        assertEquals("Test Expense", saved.getDescription());
        verify(expenseRepo, times(1)).save(mockExpense);
    }

    @Test
    void testGetAllExpenses() {
        when(userRepo.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        when(expenseRepo.findByUserId(1L)).thenReturn(List.of(mockExpense));

        List<Expense> expenses = expenseService.getAll("testUser");

        assertEquals(1, expenses.size());
        assertEquals("Test Expense", expenses.get(0).getDescription());
    }

    @Test
    void testGetExpenseByIdSuccess() {
        when(expenseRepo.findById(1L)).thenReturn(Optional.of(mockExpense));

        Expense found = expenseService.getById(1L, "testUser");

        assertEquals(1L, found.getId());
        assertEquals("Test Expense", found.getDescription());
    }

    @Test
    void testGetExpenseByIdAccessDenied() {
        mockUser.setUsername("otherUser");
        mockExpense.setUser(mockUser);
        when(expenseRepo.findById(1L)).thenReturn(Optional.of(mockExpense));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                expenseService.getById(1L, "testUser"));

        assertEquals("Access denied.", exception.getMessage());
    }

    @Test
    void testUpdateExpense() {
        when(expenseRepo.findById(1L)).thenReturn(Optional.of(mockExpense));
        when(expenseRepo.save(any(Expense.class))).thenReturn(mockExpense);

        Expense updated = new Expense();
        updated.setDescription("Updated");
        updated.setAmount(BigDecimal.valueOf(150.0));
        updated.setDate(LocalDate.now());

        Expense result = expenseService.update(1L, updated, "testUser");

        assertEquals("Updated", result.getDescription());
        assertEquals(BigDecimal.valueOf(150.0), result.getAmount());
    }

    @Test
    void testDeleteExpense() {
        when(expenseRepo.findById(1L)).thenReturn(Optional.of(mockExpense));

        expenseService.delete(1L, "testUser");

        verify(expenseRepo, times(1)).delete(mockExpense);
    }

    // Utility: inject private field using reflection
    private void injectField(Object target, String fieldName, Object toInject) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, toInject);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
