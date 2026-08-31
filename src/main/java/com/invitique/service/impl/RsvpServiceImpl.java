package com.invitique.service.impl;

import com.invitique.domain.model.*;
import com.invitique.domain.repository.*;
import com.invitique.dto.rsvp.*;
import com.invitique.service.RsvpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RsvpServiceImpl implements RsvpService {

    private final InviteRepository inviteRepository;
    private final WeddingEventRepository weddingEventRepository;
    private final GuestGroupRepository guestGroupRepository;
    private final GuestGroupEventRepository guestGroupEventRepository;
    private final InvitationLinkRepository invitationLinkRepository;
    private final RsvpRepository rsvpRepository;
    private final RsvpEventResponseRepository rsvpEventResponseRepository;

    @Override
    @Transactional
    public RsvpSubmissionResponse submitRsvp(RsvpSubmissionRequest request) {
        if (request.getWeddingCode() == null || request.getWeddingCode().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wedding code is required");
        }
        if (request.getGuestName() == null || request.getGuestName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Guest name is required");
        }

        Invite invite = inviteRepository.findByCode(request.getWeddingCode().trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wedding invitation not found"));

        syncEventsFromScheduleData(invite);

        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().trim().isEmpty()) {
            Optional<Rsvp> existingOpt = rsvpRepository.findByWeddingAndIdempotencyKey(invite, request.getIdempotencyKey().trim());
            if (existingOpt.isPresent()) {
                Rsvp existing = existingOpt.get();
                return RsvpSubmissionResponse.builder()
                        .success(true)
                        .message("RSVP submitted successfully")
                        .rsvpId(existing.getId())
                        .guestName(existing.getGuestName())
                        .attendance(existing.getAttendanceStatus())
                        .build();
            }
        }

        GuestGroup guestGroup = null;
        InvitationLink invitationLink = null;

        if (request.getInvitationLink() != null && !request.getInvitationLink().trim().isEmpty()) {
            String slug = request.getInvitationLink().trim().toLowerCase();
            Optional<InvitationLink> linkOpt = invitationLinkRepository.findByWeddingAndSlug(invite, slug);
            if (linkOpt.isPresent()) {
                invitationLink = linkOpt.get();
                guestGroup = invitationLink.getGuestGroup();
            } else {
                Optional<GuestGroup> groupOpt = guestGroupRepository.findByWeddingAndSlug(invite, slug);
                if (groupOpt.isPresent()) {
                    guestGroup = groupOpt.get();
                }
            }
        }

        String attendance = (request.getAttendance() != null) ? request.getAttendance().trim().toLowerCase() : "yes";
        int guestCount = (request.getGuestCount() != null && request.getGuestCount() > 0) ? request.getGuestCount() : 1;
        if ("no".equalsIgnoreCase(attendance)) {
            guestCount = 0;
        }

        Rsvp rsvp = Rsvp.builder()
                .wedding(invite)
                .guestGroup(guestGroup)
                .invitationLink(invitationLink)
                .guestName(request.getGuestName().trim())
                .attendanceStatus(attendance)
                .guestCount(guestCount)
                .message(request.getMessage() != null ? request.getMessage().trim() : null)
                .mealPreference(request.getMealPreference() != null ? request.getMealPreference().trim() : null)
                .accommodationNeeded(Boolean.TRUE.equals(request.getAccommodationNeeded()))
                .dietaryNotes(request.getDietaryNotes() != null ? request.getDietaryNotes().trim() : null)
                .idempotencyKey(request.getIdempotencyKey())
                .customResponses(request.getCustomResponses())
                .build();

        Rsvp savedRsvp = rsvpRepository.save(rsvp);

        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            List<WeddingEvent> allEvents = weddingEventRepository.findByWeddingOrderBySortOrderAscCreatedAtAsc(invite);
            Map<UUID, WeddingEvent> eventMap = allEvents.stream().collect(Collectors.toMap(WeddingEvent::getId, e -> e));
            Map<String, WeddingEvent> nameMap = allEvents.stream().collect(Collectors.toMap(e -> e.getName().toLowerCase(), e -> e, (a, b) -> a));

            for (RsvpSubmissionRequest.EventResponseItem item : request.getEvents()) {
                WeddingEvent event = null;
                if (item.getEventId() != null && eventMap.containsKey(item.getEventId())) {
                    event = eventMap.get(item.getEventId());
                } else if (item.getEventName() != null && nameMap.containsKey(item.getEventName().trim().toLowerCase())) {
                    event = nameMap.get(item.getEventName().trim().toLowerCase());
                }

                if (event != null) {
                    RsvpEventResponse eventResponse = RsvpEventResponse.builder()
                            .rsvp(savedRsvp)
                            .event(event)
                            .response(item.getResponse() != null ? item.getResponse().toLowerCase() : attendance)
                            .build();
                    rsvpEventResponseRepository.save(eventResponse);
                }
            }
        }

        return RsvpSubmissionResponse.builder()
                .success(true)
                .message("RSVP submitted successfully")
                .rsvpId(savedRsvp.getId())
                .guestName(savedRsvp.getGuestName())
                .attendance(savedRsvp.getAttendanceStatus())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RsvpPublicConfigResponse getPublicRsvpConfig(String code, String groupSlug) {
        Invite invite = inviteRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wedding invitation not found"));

        Map<String, Object> rsvpData = invite.getRsvpData() != null ? invite.getRsvpData() : Collections.emptyMap();

        boolean enabled = rsvpData.containsKey("enabled") ? Boolean.TRUE.equals(rsvpData.get("enabled")) : true;
        String title = (String) rsvpData.getOrDefault("title", "We'd Love to Celebrate With You");
        String description = (String) rsvpData.getOrDefault("description", "Please let us know if you will be able to join our celebration.");
        String successTitle = (String) rsvpData.getOrDefault("successTitle", "RSVP Submitted Successfully ❤️");
        String successMessage = (String) rsvpData.getOrDefault("successMessage", "Thank you for confirming. We can't wait to celebrate with you!");
        
        boolean allowMaybe = Boolean.TRUE.equals(rsvpData.get("allowMaybe"));
        boolean allowGuestCount = !Boolean.FALSE.equals(rsvpData.get("allowGuestCount"));
        boolean allowEventSelection = !Boolean.FALSE.equals(rsvpData.get("allowEventSelection"));
        boolean allowMessage = !Boolean.FALSE.equals(rsvpData.get("allowMessage"));
        boolean allowMealPreference = Boolean.TRUE.equals(rsvpData.get("allowMealPreference"));
        boolean allowAccommodation = Boolean.TRUE.equals(rsvpData.get("allowAccommodation"));
        Integer maxGuests = rsvpData.containsKey("maxGuests") ? ((Number) rsvpData.get("maxGuests")).intValue() : 10;

        List<WeddingEvent> allEvents = weddingEventRepository.findByWeddingOrderBySortOrderAscCreatedAtAsc(invite);
        if (allEvents.isEmpty()) {
            allEvents = generateEventsFromScheduleData(invite);
        }

        String groupName = null;
        String resolvedGroupSlug = null;
        List<WeddingEvent> filteredEvents = allEvents;

        if (groupSlug != null && !groupSlug.trim().isEmpty()) {
            String slugClean = groupSlug.trim().toLowerCase();
            Optional<GuestGroup> groupOpt = guestGroupRepository.findByWeddingAndSlug(invite, slugClean);
            if (groupOpt.isPresent()) {
                GuestGroup group = groupOpt.get();
                groupName = group.getName();
                resolvedGroupSlug = group.getSlug();
                List<GuestGroupEvent> groupEvents = guestGroupEventRepository.findByGuestGroup(group);
                if (!groupEvents.isEmpty()) {
                    Set<UUID> allowedEventIds = groupEvents.stream().map(ge -> ge.getEvent().getId()).collect(Collectors.toSet());
                    filteredEvents = allEvents.stream().filter(e -> allowedEventIds.contains(e.getId())).collect(Collectors.toList());
                }
            }
        }

        List<RsvpPublicConfigResponse.EventConfigDto> eventDtos = filteredEvents.stream()
                .filter(WeddingEvent::getIsActive)
                .map(e -> RsvpPublicConfigResponse.EventConfigDto.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .date(e.getEventDate())
                        .time(e.getEventTime())
                        .venue(e.getVenue())
                        .address(e.getAddress())
                        .sortOrder(e.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        return RsvpPublicConfigResponse.builder()
                .enabled(enabled)
                .title(title)
                .description(description)
                .successTitle(successTitle)
                .successMessage(successMessage)
                .allowMaybe(allowMaybe)
                .allowGuestCount(allowGuestCount)
                .allowEventSelection(allowEventSelection)
                .allowMessage(allowMessage)
                .allowMealPreference(allowMealPreference)
                .allowAccommodation(allowAccommodation)
                .maxGuestsPerSubmission(maxGuests)
                .groupName(groupName)
                .groupSlug(resolvedGroupSlug)
                .events(eventDtos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RsvpSummaryResponse getRsvpSummary(User user, String code) {
        Invite invite = verifyAndGetInvite(user, code);

        long totalResponses = rsvpRepository.countTotalByWeddingId(invite.getId());
        long attending = rsvpRepository.countByWeddingIdAndStatus(invite.getId(), "yes");
        long declined = rsvpRepository.countByWeddingIdAndStatus(invite.getId(), "no");
        long maybe = rsvpRepository.countByWeddingIdAndStatus(invite.getId(), "maybe");
        long totalAttendingGuests = rsvpRepository.sumAttendingGuestsByWeddingId(invite.getId());
        long totalPossibleGuests = rsvpRepository.sumTotalGuestsByWeddingId(invite.getId());

        List<WeddingEvent> events = weddingEventRepository.findByWeddingOrderBySortOrderAscCreatedAtAsc(invite);
        List<RsvpSummaryResponse.EventCountDto> eventCounts = new ArrayList<>();

        for (WeddingEvent ev : events) {
            long evAttending = rsvpEventResponseRepository.countAttendingByEventId(ev.getId());
            long evDeclined = rsvpEventResponseRepository.countDeclinedByEventId(ev.getId());
            long evMaybe = rsvpEventResponseRepository.countMaybeByEventId(ev.getId());

            eventCounts.add(RsvpSummaryResponse.EventCountDto.builder()
                    .eventId(ev.getId())
                    .eventName(ev.getName())
                    .eventDate(ev.getEventDate())
                    .eventTime(ev.getEventTime())
                    .attending(evAttending)
                    .declined(evDeclined)
                    .maybe(evMaybe)
                    .build());
        }

        return RsvpSummaryResponse.builder()
                .totalResponses(totalResponses)
                .attending(attending)
                .declined(declined)
                .maybe(maybe)
                .totalAttendingGuests(totalAttendingGuests)
                .totalPossibleGuests(totalPossibleGuests)
                .eventCounts(eventCounts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RsvpDetailResponse> getRsvps(User user, String code, String status, UUID groupId, String search, Pageable pageable) {
        Invite invite = verifyAndGetInvite(user, code);

        String statusParam = (status != null && !status.trim().isEmpty() && !"all".equalsIgnoreCase(status)) ? status.trim().toLowerCase() : null;
        String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        Page<Rsvp> page;
        if (statusParam == null && groupId == null && searchParam == null) {
            page = rsvpRepository.findByWeddingOrderBySubmittedAtDesc(invite, pageable);
        } else {
            page = rsvpRepository.findFilteredRsvps(invite.getId(), statusParam, groupId, searchParam, pageable);
        }

        List<UUID> rsvpIds = page.getContent().stream().map(Rsvp::getId).collect(Collectors.toList());
        Map<UUID, List<RsvpEventResponse>> eventResponsesMap = Collections.emptyMap();
        if (!rsvpIds.isEmpty()) {
            List<RsvpEventResponse> allResponses = rsvpEventResponseRepository.findByRsvpIds(rsvpIds);
            eventResponsesMap = allResponses.stream().collect(Collectors.groupingBy(r -> r.getRsvp().getId()));
        }

        final Map<UUID, List<RsvpEventResponse>> finalEventMap = eventResponsesMap;

        List<RsvpDetailResponse> dtos = page.getContent().stream()
                .map(r -> mapToDetailResponse(r, finalEventMap.getOrDefault(r.getId(), Collections.emptyList())))
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public RsvpDetailResponse getRsvpById(User user, String code, UUID rsvpId) {
        Invite invite = verifyAndGetInvite(user, code);

        Rsvp rsvp = rsvpRepository.findById(rsvpId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RSVP not found"));

        if (!rsvp.getWedding().getId().equals(invite.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "RSVP does not belong to this wedding");
        }

        List<RsvpEventResponse> eventResponses = rsvpEventResponseRepository.findByRsvp(rsvp);
        return mapToDetailResponse(rsvp, eventResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public String exportRsvpsCsv(User user, String code) {
        Invite invite = verifyAndGetInvite(user, code);

        List<Rsvp> rsvps = rsvpRepository.findByWeddingOrderBySubmittedAtDesc(invite);
        List<UUID> rsvpIds = rsvps.stream().map(Rsvp::getId).collect(Collectors.toList());
        Map<UUID, List<RsvpEventResponse>> eventResponsesMap = Collections.emptyMap();
        if (!rsvpIds.isEmpty()) {
            List<RsvpEventResponse> allResponses = rsvpEventResponseRepository.findByRsvpIds(rsvpIds);
            eventResponsesMap = allResponses.stream().collect(Collectors.groupingBy(r -> r.getRsvp().getId()));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Guest Name,Attendance Status,Guest Count,Guest Group,Invitation Link,Events Attended,Meal Preference,Accommodation Needed,Dietary Notes,Message,Submitted At\n");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Rsvp r : rsvps) {
            String groupName = r.getGuestGroup() != null ? r.getGuestGroup().getName() : "General";
            String linkSlug = r.getInvitationLink() != null ? r.getInvitationLink().getSlug() : "";
            
            List<RsvpEventResponse> evList = eventResponsesMap.getOrDefault(r.getId(), Collections.emptyList());
            String eventsAttended = evList.stream()
                    .filter(e -> "yes".equalsIgnoreCase(e.getResponse()))
                    .map(e -> e.getEvent().getName())
                    .collect(Collectors.joining("; "));

            String submitted = r.getSubmittedAt() != null ? r.getSubmittedAt().format(dtf) : "";

            sb.append(escapeCsv(r.getGuestName())).append(",")
                    .append(escapeCsv(r.getAttendanceStatus())).append(",")
                    .append(r.getGuestCount()).append(",")
                    .append(escapeCsv(groupName)).append(",")
                    .append(escapeCsv(linkSlug)).append(",")
                    .append(escapeCsv(eventsAttended)).append(",")
                    .append(escapeCsv(r.getMealPreference() != null ? r.getMealPreference() : "")).append(",")
                    .append(Boolean.TRUE.equals(r.getAccommodationNeeded()) ? "Yes" : "No").append(",")
                    .append(escapeCsv(r.getDietaryNotes() != null ? r.getDietaryNotes() : "")).append(",")
                    .append(escapeCsv(r.getMessage() != null ? r.getMessage() : "")).append(",")
                    .append(escapeCsv(submitted)).append("\n");
        }

        return sb.toString();
    }

    @Override
    @Transactional
    public GuestGroupDto createGuestGroup(User user, String code, GuestGroupRequest request) {
        Invite invite = verifyAndGetInvite(user, code);

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name is required");
        }

        String rawSlug = (request.getSlug() != null && !request.getSlug().trim().isEmpty())
                ? request.getSlug().trim()
                : request.getName().trim();

        String slug = rawSlug.toLowerCase().replaceAll("[^a-z0-9-]", "-").replaceAll("-+", "-").replaceAll("^-|-$", "");
        if (slug.isEmpty()) {
            slug = "group-" + System.currentTimeMillis();
        }

        // Check if group slug already exists for this wedding
        Optional<GuestGroup> existingGroup = guestGroupRepository.findByWeddingAndSlug(invite, slug);
        GuestGroup group;
        if (existingGroup.isPresent()) {
            group = existingGroup.get();
            group.setName(request.getName().trim());
        } else {
            group = GuestGroup.builder()
                    .wedding(invite)
                    .name(request.getName().trim())
                    .slug(slug)
                    .build();
        }

        GuestGroup saved = guestGroupRepository.save(group);

        Optional<InvitationLink> existingLink = invitationLinkRepository.findByWeddingAndSlug(invite, slug);
        if (existingLink.isEmpty()) {
            InvitationLink link = InvitationLink.builder()
                    .wedding(invite)
                    .guestGroup(saved)
                    .slug(slug)
                    .label(saved.getName() + " Link")
                    .isActive(true)
                    .build();
            invitationLinkRepository.save(link);
        }

        if (request.getAllowedEventIds() != null && !request.getAllowedEventIds().isEmpty()) {
            guestGroupEventRepository.deleteByGuestGroupId(saved.getId());
            List<WeddingEvent> events = weddingEventRepository.findAllById(request.getAllowedEventIds());
            for (WeddingEvent ev : events) {
                if (ev.getWedding().getId().equals(invite.getId())) {
                    GuestGroupEvent gge = GuestGroupEvent.builder()
                            .guestGroup(saved)
                            .event(ev)
                            .build();
                    guestGroupEventRepository.save(gge);
                }
            }
        }

        return GuestGroupDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .slug(saved.getSlug())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuestGroupDto> getGuestGroups(User user, String code) {
        Invite invite = verifyAndGetInvite(user, code);
        List<GuestGroup> groups = guestGroupRepository.findByWedding(invite);
        return groups.stream()
                .map(g -> GuestGroupDto.builder()
                        .id(g.getId())
                        .name(g.getName())
                        .slug(g.getSlug())
                        .createdAt(g.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void syncEventsFromScheduleData(Invite invite) {
        List<WeddingEvent> existing = weddingEventRepository.findByWeddingOrderBySortOrderAscCreatedAtAsc(invite);
        if (!existing.isEmpty()) {
            return;
        }
        generateEventsFromScheduleData(invite);
    }

    private List<WeddingEvent> generateEventsFromScheduleData(Invite invite) {
        List<WeddingEvent> created = new ArrayList<>();
        if (invite.getScheduleData() != null) {
            Object itemsObj = invite.getScheduleData().get("items");
            if (itemsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) itemsObj;
                int order = 0;
                for (Map<String, Object> item : items) {
                    String title = (String) item.get("title");
                    String time = (String) item.get("time");
                    String date = (String) item.get("date");
                    String desc = (String) item.get("description");

                    if (title != null && !title.trim().isEmpty()) {
                        WeddingEvent ev = WeddingEvent.builder()
                                .wedding(invite)
                                .name(title.trim())
                                .eventDate(date)
                                .eventTime(time)
                                .address(desc)
                                .sortOrder(order++)
                                .isActive(true)
                                .build();
                        created.add(weddingEventRepository.save(ev));
                    }
                }
            }
        }

        if (created.isEmpty()) {
            String date = null;
            if (invite.getHeroData() != null) {
                date = invite.getHeroData().get("weddingDate") + " " + invite.getHeroData().get("weddingMonth") + " " + invite.getHeroData().get("weddingYear");
            }
            String time = invite.getHeroData() != null ? (String) invite.getHeroData().get("weddingTime") : "09:00 AM";
            String venue = invite.getVenueData() != null ? (String) invite.getVenueData().get("mahalName") : "Wedding Venue";
            String address = invite.getVenueData() != null ? (String) invite.getVenueData().get("venueAddress") : "";

            WeddingEvent defaultEvent = WeddingEvent.builder()
                    .wedding(invite)
                    .name("Wedding Ceremony & Reception")
                    .eventDate(date)
                    .eventTime(time)
                    .venue(venue)
                    .address(address)
                    .sortOrder(0)
                    .isActive(true)
                    .build();
            created.add(weddingEventRepository.save(defaultEvent));
        }

        return created;
    }

    private Invite verifyAndGetInvite(User user, String code) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wedding code is required");
        }

        Invite invite = inviteRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wedding invitation not found"));

        boolean isAdmin = user.getRoles() != null && user.getRoles().contains(Role.ADMIN);
        boolean isOwner = invite.getUser() != null && invite.getUser().getId().equals(user.getId());

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to view RSVPs for this wedding");
        }

        return invite;
    }

    private RsvpDetailResponse mapToDetailResponse(Rsvp rsvp, List<RsvpEventResponse> eventResponses) {
        List<RsvpDetailResponse.EventResponseDetail> eventDetails = eventResponses.stream()
                .map(er -> RsvpDetailResponse.EventResponseDetail.builder()
                        .eventId(er.getEvent().getId())
                        .eventName(er.getEvent().getName())
                        .eventDate(er.getEvent().getEventDate())
                        .eventTime(er.getEvent().getEventTime())
                        .venue(er.getEvent().getVenue())
                        .response(er.getResponse())
                        .build())
                .collect(Collectors.toList());

        return RsvpDetailResponse.builder()
                .id(rsvp.getId())
                .weddingId(rsvp.getWedding().getId())
                .weddingCode(rsvp.getWedding().getCode())
                .guestName(rsvp.getGuestName())
                .attendanceStatus(rsvp.getAttendanceStatus())
                .guestCount(rsvp.getGuestCount())
                .message(rsvp.getMessage())
                .mealPreference(rsvp.getMealPreference())
                .accommodationNeeded(rsvp.getAccommodationNeeded())
                .dietaryNotes(rsvp.getDietaryNotes())
                .guestGroupName(rsvp.getGuestGroup() != null ? rsvp.getGuestGroup().getName() : "General")
                .guestGroupSlug(rsvp.getGuestGroup() != null ? rsvp.getGuestGroup().getSlug() : null)
                .invitationLinkSlug(rsvp.getInvitationLink() != null ? rsvp.getInvitationLink().getSlug() : null)
                .submittedAt(rsvp.getSubmittedAt())
                .eventResponses(eventDetails)
                .customResponses(rsvp.getCustomResponses())
                .build();
    }

    private String escapeCsv(String val) {
        if (val == null) return "\"\"";
        String s = val.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}
