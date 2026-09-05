package com.invitique.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invitique.domain.model.Invite;
import com.invitique.domain.model.TemplateImage;
import com.invitique.domain.repository.InviteRepository;
import com.invitique.domain.repository.TemplateImageRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryMigrationService {

    @Autowired
    private InviteRepository inviteRepository;

    @Autowired
    private TemplateImageRepository templateImageRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public void migrateCloudinaryImages() {
        List<Invite> invites = inviteRepository.findAll();
        for (Invite invite : invites) {
            boolean updated = false;
            if (invite.getBackupData() == null) {
                Map<String, Object> backup = new java.util.HashMap<>();
                try {
                    backup.put("coupleData", objectMapper.readValue(objectMapper.writeValueAsString(invite.getCoupleData()), new TypeReference<Map<String, Object>>(){}));
                    backup.put("heroData", objectMapper.readValue(objectMapper.writeValueAsString(invite.getHeroData()), new TypeReference<Map<String, Object>>(){}));
                    backup.put("storyData", objectMapper.readValue(objectMapper.writeValueAsString(invite.getStoryData()), new TypeReference<Map<String, Object>>(){}));
                    backup.put("invitationData", objectMapper.readValue(objectMapper.writeValueAsString(invite.getInvitationData()), new TypeReference<Map<String, Object>>(){}));
                    backup.put("eventData", objectMapper.readValue(objectMapper.writeValueAsString(invite.getEventData()), new TypeReference<Map<String, Object>>(){}));
                    backup.put("venueData", objectMapper.readValue(objectMapper.writeValueAsString(invite.getVenueData()), new TypeReference<Map<String, Object>>(){}));
                    backup.put("scheduleData", objectMapper.readValue(objectMapper.writeValueAsString(invite.getScheduleData()), new TypeReference<Map<String, Object>>(){}));
                    backup.put("rsvpData", objectMapper.readValue(objectMapper.writeValueAsString(invite.getRsvpData()), new TypeReference<Map<String, Object>>(){}));
                    invite.setBackupData(backup);
                } catch (Exception ex) {
                    System.err.println("Failed to backup invite " + invite.getId() + ": " + ex.getMessage());
                }
            }

            // Process all JSONB fields
            try {
                if (processJsonField(invite.getCoupleData())) {
                    updated = true;
                }
                if (processJsonField(invite.getHeroData())) {
                    updated = true;
                }
                if (processJsonField(invite.getStoryData())) {
                    updated = true;
                }
                if (processJsonField(invite.getInvitationData())) {
                    updated = true;
                }
                if (processJsonField(invite.getEventData())) {
                    updated = true;
                }
                if (processJsonField(invite.getVenueData())) {
                    updated = true;
                }
                if (processJsonField(invite.getScheduleData())) {
                    updated = true;
                }
                if (processJsonField(invite.getRsvpData())) {
                    updated = true;
                }
            } catch (Exception e) {
                System.err.println("Error processing invite " + invite.getId() + ": " + e.getMessage());
            }

            if (updated) {
                inviteRepository.save(invite);
                System.out.println("Migrated images for invite: " + invite.getId());
            }
        }
    }

    private boolean processJsonField(Map<String, Object> data) {
        if (data == null) return false;
        boolean updated = false;

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                String strValue = (String) value;
                if (strValue.contains("res.cloudinary.com")) {
                    String newUrl = migrateImage(strValue);
                    if (newUrl != null) {
                        data.put(entry.getKey(), newUrl);
                        updated = true;
                    }
                }
            } else if (value instanceof List) {
                List list = (List) value;
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    if (item instanceof String) {
                        String strItem = (String) item;
                        if (strItem.contains("res.cloudinary.com")) {
                            String newUrl = migrateImage(strItem);
                            if (newUrl != null) {
                                list.set(i, newUrl);
                                updated = true;
                            }
                        }
                    } else if (item instanceof Map) {
                        if (processJsonField((Map<String, Object>) item)) {
                            updated = true;
                        }
                    }
                }
            } else if (value instanceof Map) {
                if (processJsonField((Map<String, Object>) value)) {
                    updated = true;
                }
            }
        }
        return updated;
    }

    private String migrateImage(String cloudinaryUrl) {
        try {
            byte[] imageBytes = restTemplate.getForObject(cloudinaryUrl, byte[].class);
            if (imageBytes != null && imageBytes.length > 0) {
                TemplateImage image = TemplateImage.builder()
                        .fileName(extractFileName(cloudinaryUrl))
                        .contentType("image/jpeg") // simplification
                        .data(imageBytes)
                        .build();
                image = templateImageRepository.save(image);
                return "/api/images/" + image.getId();
            }
        } catch (Exception e) {
            System.err.println("Failed to download image " + cloudinaryUrl + ": " + e.getMessage());
        }
        return null;
    }
    
    private String extractFileName(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    @Transactional
    public void revertCloudinaryMigration() {
        List<Invite> invites = inviteRepository.findAll();
        for (Invite invite : invites) {
            if (invite.getBackupData() != null) {
                try {
                    Map<String, Object> backup = invite.getBackupData();
                    if (backup.containsKey("coupleData")) invite.setCoupleData(objectMapper.convertValue(backup.get("coupleData"), new TypeReference<Map<String, Object>>(){}));
                    if (backup.containsKey("heroData")) invite.setHeroData(objectMapper.convertValue(backup.get("heroData"), new TypeReference<Map<String, Object>>(){}));
                    if (backup.containsKey("storyData")) invite.setStoryData(objectMapper.convertValue(backup.get("storyData"), new TypeReference<Map<String, Object>>(){}));
                    if (backup.containsKey("invitationData")) invite.setInvitationData(objectMapper.convertValue(backup.get("invitationData"), new TypeReference<Map<String, Object>>(){}));
                    if (backup.containsKey("eventData")) invite.setEventData(objectMapper.convertValue(backup.get("eventData"), new TypeReference<Map<String, Object>>(){}));
                    if (backup.containsKey("venueData")) invite.setVenueData(objectMapper.convertValue(backup.get("venueData"), new TypeReference<Map<String, Object>>(){}));
                    if (backup.containsKey("scheduleData")) invite.setScheduleData(objectMapper.convertValue(backup.get("scheduleData"), new TypeReference<Map<String, Object>>(){}));
                    if (backup.containsKey("rsvpData")) invite.setRsvpData(objectMapper.convertValue(backup.get("rsvpData"), new TypeReference<Map<String, Object>>(){}));
                    invite.setBackupData(null); // Clear backup after restoring
                    inviteRepository.save(invite);
                    System.out.println("Reverted migration for invite: " + invite.getId());
                } catch (Exception ex) {
                    System.err.println("Failed to revert invite " + invite.getId() + ": " + ex.getMessage());
                }
            }
        }
    }
}
