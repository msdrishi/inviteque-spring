package com.invitique.service;

import com.invitique.domain.model.Invite;
import com.invitique.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOrderNotification(Invite invite, User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("inviteque.support@gmail.com");
            message.setTo("inviteque.support@gmail.com");
            message.setSubject("🔔 New Order Purchased - InviteQue: " + invite.getCode());

            String groomName = "N/A";
            String brideName = "N/A";
            if (invite.getCoupleData() != null) {
                groomName = String.valueOf(invite.getCoupleData().getOrDefault("groomName", "N/A"));
                brideName = String.valueOf(invite.getCoupleData().getOrDefault("brideName", "N/A"));
            }

            String customerName = user != null ? user.getName() : "Unknown";
            String customerEmail = user != null ? user.getEmail() : "Unknown";

            String paidAtStr = invite.getPaidAt() != null 
                    ? invite.getPaidAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) 
                    : "N/A";

            String body = String.format(
                    "Dear Admin,\n\n" +
                    "A new digital wedding invitation has been purchased on InviteQue!\n\n" +
                    "=========================================\n" +
                    "ORDER DETAILS\n" +
                    "=========================================\n" +
                    "Order / Invite Code: %s\n" +
                    "Template ID:        %s\n" +
                    "Amount Paid:        ₹%.2f\n" +
                    "Coupon Used:        %s\n" +
                    "Razorpay Order ID:  %s\n" +
                    "Razorpay Payment ID:%s\n" +
                    "Purchase Date:      %s\n\n" +
                    "=========================================\n" +
                    "COUPLE & CUSTOMER DETAILS\n" +
                    "=========================================\n" +
                    "Couple:             %s & %s\n" +
                    "Customer Name:      %s\n" +
                    "Customer Email:     %s\n\n" +
                    "Live Invite Link:   https://www.inviteque.com/templates/%s/%s\n\n" +
                    "Best regards,\n" +
                    "InviteQue Notification Engine",
                    invite.getCode(),
                    invite.getTemplateId(),
                    invite.getAmountPaid() != null ? invite.getAmountPaid() : 999.0,
                    invite.getCouponCode() != null ? invite.getCouponCode() : "NONE",
                    invite.getRazorpayOrderId() != null ? invite.getRazorpayOrderId() : "N/A",
                    invite.getRazorpayPaymentId() != null ? invite.getRazorpayPaymentId() : "N/A",
                    paidAtStr,
                    groomName,
                    brideName,
                    customerName,
                    customerEmail,
                    invite.getTemplateId(),
                    invite.getCode()
            );

            message.setText(body);
            mailSender.send(message);
            log.info("Purchase notification email sent successfully for invite code: {}", invite.getCode());
        } catch (Exception e) {
            log.error("Failed to send purchase notification email for invite code: {}", invite.getCode(), e);
        }
    }
}
