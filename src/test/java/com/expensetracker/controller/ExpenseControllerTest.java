package com.expensetracker.controller;

import com.expensetracker.model.Expense;
import com.expensetracker.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ExpenseControllerTest {

    private MockMvc mockMvc;
    private ExpenseService expenseService;
    private ObjectMapper objectMapper;
    private Principal principal;
    private Expense expense;

    @BeforeEach
    void setup() {
        expenseService = Mockito.mock(ExpenseService.class);
        ExpenseController expenseController = new ExpenseController(expenseService);
        // Inject mock via reflection
        try {
            var field = ExpenseController.class.getDeclaredField("expenseService");
            field.setAccessible(true);
            field.set(expenseController, expenseService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock expenseService", e);
        }

        mockMvc = MockMvcBuilders.standaloneSetup(expenseController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Register JavaTime module

        principal = () -> "testUser";

        expense = new Expense();
        expense.setId(1L);
        expense.setDescription("Test Expense");
        expense.setAmount(BigDecimal.valueOf(100.00));
        expense.setDate(LocalDate.now());
    }

    @Test
    void testCreateExpense() throws Exception {
        Mockito.when(expenseService.create(any(Expense.class), eq("testUser"))).thenReturn(expense);

        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expense))
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Test Expense"))
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    void testGetAllExpenses() throws Exception {
        List<Expense> expenses = Arrays.asList(expense);
        Mockito.when(expenseService.getAll("testUser")).thenReturn(expenses);

        mockMvc.perform(get("/api/expenses").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].description").value("Test Expense"));
    }

    @Test
    void testGetExpenseById() throws Exception {
        Mockito.when(expenseService.getById(1L, "testUser")).thenReturn(expense);

        mockMvc.perform(get("/api/expenses/1").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Test Expense"));
    }

    @Test
    void testUpdateExpense() throws Exception {
        Expense updated = new Expense();
        updated.setId(1L);
        updated.setDescription("Updated Expense");
        updated.setAmount(BigDecimal.valueOf(200.00));
        updated.setDate(LocalDate.now());

        Mockito.when(expenseService.update(eq(1L), any(Expense.class), eq("testUser"))).thenReturn(updated);

        mockMvc.perform(put("/api/expenses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated))
                .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated Expense"))
                .andExpect(jsonPath("$.amount").value(200.00));
    }

    @Test
    void testDeleteExpense() throws Exception {
        Mockito.doNothing().when(expenseService).delete(1L, "testUser");

        mockMvc.perform(delete("/api/expenses/1").principal(principal))
                .andExpect(status().isNoContent());
    }
}
