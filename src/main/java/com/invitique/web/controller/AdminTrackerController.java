package com.invitique.web.controller;

import com.invitique.domain.model.*;
import com.invitique.service.AdminTrackerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/tracker")
@RequiredArgsConstructor
public class AdminTrackerController {

    private final AdminTrackerService trackerService;

    // EXPENSES
    @GetMapping("/expenses")
    public ResponseEntity<List<AdminExpense>> getExpenses() {
        return ResponseEntity.ok(trackerService.getAllExpenses());
    }

    @PostMapping("/expenses")
    public ResponseEntity<AdminExpense> saveExpense(@RequestBody AdminExpense expense) {
        return ResponseEntity.ok(trackerService.saveExpense(expense));
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable String id) {
        trackerService.deleteExpense(id);
        return ResponseEntity.ok().build();
    }

    // SETTLEMENTS
    @GetMapping("/settlements")
    public ResponseEntity<List<AdminSettlement>> getSettlements() {
        return ResponseEntity.ok(trackerService.getAllSettlements());
    }

    @PostMapping("/settlements")
    public ResponseEntity<AdminSettlement> saveSettlement(@RequestBody AdminSettlement settlement) {
        return ResponseEntity.ok(trackerService.saveSettlement(settlement));
    }

    @DeleteMapping("/settlements/{id}")
    public ResponseEntity<Void> deleteSettlement(@PathVariable String id) {
        trackerService.deleteSettlement(id);
        return ResponseEntity.ok().build();
    }

    // CLIENT ORDERS
    @GetMapping("/orders")
    public ResponseEntity<List<AdminClientOrder>> getOrders() {
        return ResponseEntity.ok(trackerService.getAllClientOrders());
    }

    @PostMapping("/orders")
    public ResponseEntity<AdminClientOrder> saveOrder(@RequestBody AdminClientOrder order) {
        return ResponseEntity.ok(trackerService.saveClientOrder(order));
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        trackerService.deleteClientOrder(id);
        return ResponseEntity.ok().build();
    }

    // LEADS
    @GetMapping("/leads")
    public ResponseEntity<List<AdminLead>> getLeads() {
        return ResponseEntity.ok(trackerService.getAllLeads());
    }

    @PostMapping("/leads")
    public ResponseEntity<AdminLead> saveLead(@RequestBody AdminLead lead) {
        return ResponseEntity.ok(trackerService.saveLead(lead));
    }

    @DeleteMapping("/leads/{id}")
    public ResponseEntity<Void> deleteLead(@PathVariable String id) {
        trackerService.deleteLead(id);
        return ResponseEntity.ok().build();
    }

    // TODOS
    @GetMapping("/todos")
    public ResponseEntity<List<AdminTodo>> getTodos() {
        return ResponseEntity.ok(trackerService.getAllTodos());
    }

    @PostMapping("/todos")
    public ResponseEntity<AdminTodo> saveTodo(@RequestBody AdminTodo todo) {
        return ResponseEntity.ok(trackerService.saveTodo(todo));
    }

    @DeleteMapping("/todos/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable String id) {
        trackerService.deleteTodo(id);
        return ResponseEntity.ok().build();
    }
}
