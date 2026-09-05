package com.invitique.domain.repository;

import com.invitique.domain.model.TemplateImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TemplateImageRepository extends JpaRepository<TemplateImage, UUID> {
}
