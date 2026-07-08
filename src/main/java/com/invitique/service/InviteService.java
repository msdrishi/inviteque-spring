package com.invitique.service;

import com.invitique.domain.model.Invite;
import com.invitique.domain.model.User;
import com.invitique.dto.request.InviteRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InviteService {
    Invite createOrUpdateInvite(User user, InviteRequest request);
    Invite updateInvite(User user, UUID id, InviteRequest request);
    void deleteInvite(User user, UUID id);
    Optional<Invite> getInviteByCode(String code);
    Optional<Invite> getInviteById(UUID id);
    List<Invite> getUserInvites(User user);
    void updatePaymentStatus(UUID inviteId, String orderId, String paymentId, double amount);
}
