package com.invitique.service;

import com.invitique.domain.model.*;
import com.invitique.domain.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AdminTrackerService {

    private final AdminExpenseRepository expenseRepo;
    private final AdminSettlementRepository settlementRepo;
    private final AdminClientOrderRepository orderRepo;
    private final AdminLeadRepository leadRepo;
    private final AdminTodoRepository todoRepo;

    @PostConstruct
    public void seedData() {
        if (expenseRepo.count() == 0) {
            expenseRepo.saveAll(Arrays.asList(
                AdminExpense.builder().id("exp-1").title("Promotion on Instagram (Meta Ads)").category("Marketing & Ads").amount(1062.0).frequency("One-time").paymentMethod("Meta Ads UPI").date("2026-08-27").notes("Instagram ad campaign targeting wedding & engagement couples").build(),
                AdminExpense.builder().id("exp-2").title("Domain Charges on Namecheap (Inviteque)").category("Domain & DNS").amount(700.0).frequency("Annual").paymentMethod("Debit Card").date("2026-05-24").notes("Annual domain name registration on Namecheap").build(),
                AdminExpense.builder().id("exp-3").title("Render Backend Hosting Web Service").category("Hosting & Server").amount(350.0).frequency("Monthly").paymentMethod("Credit Card").date("2026-08-09").notes("Monthly Node.js & PostgreSQL backend instance on Render").build()
            ));
        }
        if (settlementRepo.count() == 0) {
            settlementRepo.saveAll(Arrays.asList(
                AdminSettlement.builder().id("set-1").clientName("Pavitra").serviceType("Template Customization (2 links / variant)").amount(500.0).paymentMethod("UPI - GPay").date("2026-08-24").status("Settled").notes("Advance paid on 24 Aug (Total ₹2,500, pending ₹2,000)").build(),
                AdminSettlement.builder().id("set-2").clientName("Shradha").serviceType("Template Customization (Splash screen addition)").amount(500.0).paymentMethod("UPI - PhonePe").date("2026-08-24").status("Settled").notes("Advance paid on 24 Aug (Total ₹2,000, pending ₹1,500)").build()
            ));
        }
        if (orderRepo.count() == 0) {
            orderRepo.saveAll(Arrays.asList(
                AdminClientOrder.builder().id("ord-1").clientName("Pavitra").phone("+919876543210").email("pavitra@gmail.com").source("Instagram").serviceName("Customized Wedding Template (2 Links / Custom Variants)").totalCharge(2500.0).advancePaid(500.0).advanceDate("2026-08-24").clientDeadline("2026-09-07").deliveryDate("2026-09-07").status("In Progress").deliverableUrl("/template/midnight-waltz/Pavitra-Sri").deliverableUrls(Arrays.asList("/template/midnight-waltz/Pavitra-Sri/1", "/template/midnight-waltz/Pavitra-Sri/2")).notes("Client wants little modification over the existing template and needs two links.").build(),
                AdminClientOrder.builder().id("ord-2").clientName("Shradha").phone("+919123456789").email("shradha@gmail.com").source("WhatsApp").serviceName("Customized Wedding Template + Custom Splash Screen").totalCharge(2000.0).advancePaid(500.0).advanceDate("2026-08-24").clientDeadline("2026-09-20").deliveryDate("2026-09-20").status("In Progress").deliverableUrl("/template/everlastingvows/Shradha").deliverableUrls(Arrays.asList("/template/everlastingvows/Shradha")).notes("Client wants custom splash screen on existing template page.").build()
            ));
        }
        if (leadRepo.count() == 0) {
            leadRepo.saveAll(Arrays.asList(
                AdminLead.builder().id("lead-1").name("Kirti").phone("+919988776655").source("Instagram").serviceInterested("Custom Luxury Wedding Invitation").budgetExpectation(2500.0).inquiryDate("2026-08-26").status("Waiting for Reply").notes("Asked the plan and waiting for reply. Move to active client once confirmed with timeline & budget.").build(),
                AdminLead.builder().id("lead-2").name("Anoushka").phone("+919876501234").source("WhatsApp").serviceInterested("Bespoke Sunflower Fields + Custom Audio").budgetExpectation(3000.0).inquiryDate("2026-08-27").status("Waiting for Reply").notes("Discussed custom animations and music synch. Awaiting package confirmation.").build(),
                AdminLead.builder().id("lead-3").name("Saaransh").phone("+919811223344").source("Mail").serviceInterested("House Warming / Wedding Suite").budgetExpectation(2000.0).inquiryDate("2026-08-28").status("Waiting for Reply").notes("Sent portfolio pricing sheet. Waiting for confirmation on final dates.").build()
            ));
        }
        if (todoRepo.count() == 0) {
            todoRepo.saveAll(Arrays.asList(
                AdminTodo.builder().id("td-1").text("Prepare 2 custom links & template modifications for Pavitra (Deadline: Sep 7)").priority("High").tag("Client Work").dueDate("2026-09-07").completed(false).createdAtDateString("2026-08-28").build(),
                AdminTodo.builder().id("td-2").text("Design custom splash screen draft for Shradha (Deadline: Sep 20)").priority("Medium").tag("Design").dueDate("2026-09-15").completed(false).createdAtDateString("2026-08-28").build(),
                AdminTodo.builder().id("td-3").text("Follow up with inquiries: Kirti, Anoushka, and Saaransh on Instagram & WhatsApp").priority("High").tag("Leads & Sales").dueDate("2026-08-29").completed(false).createdAtDateString("2026-08-28").build(),
                AdminTodo.builder().id("td-4").text("Monitor Instagram promotion ad performance (Spent: ₹1,062 on Aug 27)").priority("Medium").tag("Marketing").dueDate("2026-08-30").completed(true).createdAtDateString("2026-08-27").build()
            ));
        }
    }

    // EXPENSES
    public List<AdminExpense> getAllExpenses() { return expenseRepo.findAll(); }
    public AdminExpense saveExpense(AdminExpense expense) { return expenseRepo.save(expense); }
    public void deleteExpense(String id) { expenseRepo.deleteById(id); }

    // SETTLEMENTS
    public List<AdminSettlement> getAllSettlements() { return settlementRepo.findAll(); }
    public AdminSettlement saveSettlement(AdminSettlement settlement) { return settlementRepo.save(settlement); }
    public void deleteSettlement(String id) { settlementRepo.deleteById(id); }

    // CLIENT ORDERS
    public List<AdminClientOrder> getAllClientOrders() { return orderRepo.findAll(); }
    public AdminClientOrder saveClientOrder(AdminClientOrder order) { return orderRepo.save(order); }
    public void deleteClientOrder(String id) { orderRepo.deleteById(id); }

    // LEADS
    public List<AdminLead> getAllLeads() { return leadRepo.findAll(); }
    public AdminLead saveLead(AdminLead lead) { return leadRepo.save(lead); }
    public void deleteLead(String id) { leadRepo.deleteById(id); }

    // TODOS
    public List<AdminTodo> getAllTodos() { return todoRepo.findAll(); }
    public AdminTodo saveTodo(AdminTodo todo) { return todoRepo.save(todo); }
    public void deleteTodo(String id) { todoRepo.deleteById(id); }
}
