package com.invitique.web.controller;

import com.invitique.domain.model.Template;
import com.invitique.domain.repository.TemplateRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@Slf4j
public class TemplateController {

    private final TemplateRepository templateRepository;

    /**
     * Get all active templates with their configured pricing.
     */
    @GetMapping
    public ResponseEntity<List<TemplateDto>> getAllTemplates() {
        List<TemplateDto> list = templateRepository.findByIsActiveTrue().stream()
                .map(TemplateDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * Get a specific template by its ID or slug.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TemplateDto> getTemplateById(@PathVariable String id) {
        return templateRepository.findByIdIgnoreCase(id.trim())
                .map(t -> ResponseEntity.ok(TemplateDto.fromEntity(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update template pricing dynamically (can be modified later by admin or config).
     */
    @PutMapping("/{id}/price")
    public ResponseEntity<?> updateTemplatePrice(
            @PathVariable String id,
            @RequestBody UpdatePriceRequest request) {
        return templateRepository.findByIdIgnoreCase(id.trim())
                .map(t -> {
                    Integer newPriceInPaise;
                    if (request.getPriceInPaise() != null) {
                        newPriceInPaise = request.getPriceInPaise();
                    } else if (request.getPriceInRupees() != null) {
                        newPriceInPaise = (int) Math.round(request.getPriceInRupees() * 100);
                    } else if (request.getPrice() != null) {
                        // If provided number is < 10000 assume it's in rupees, otherwise paise
                        newPriceInPaise = request.getPrice() < 10000 ? request.getPrice() * 100 : request.getPrice();
                    } else {
                        return ResponseEntity.badRequest().body(Map.of("message", "Price is required"));
                    }

                    t.setPrice(newPriceInPaise);
                    templateRepository.save(t);
                    log.info("Updated price for template {} to {} paise", id, newPriceInPaise);
                    return ResponseEntity.ok(TemplateDto.fromEntity(t));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Data
    public static class TemplateDto {
        private String id;
        private String name;
        private String description;
        private Integer priceInPaise;
        private Double priceInRupees;
        private String thumbnailUrl;
        private Boolean isActive;

        public static TemplateDto fromEntity(Template t) {
            TemplateDto dto = new TemplateDto();
            dto.setId(t.getId());
            dto.setName(t.getName());
            dto.setDescription(t.getDescription());
            dto.setPriceInPaise(t.getPrice());
            dto.setPriceInRupees(t.getPrice() != null ? t.getPrice() / 100.0 : 2499.0);
            dto.setThumbnailUrl(t.getThumbnailUrl());
            dto.setIsActive(t.getIsActive());
            return dto;
        }
    }

    @Data
    public static class UpdatePriceRequest {
        private Integer price;
        private Integer priceInPaise;
        private Double priceInRupees;
    }
}

