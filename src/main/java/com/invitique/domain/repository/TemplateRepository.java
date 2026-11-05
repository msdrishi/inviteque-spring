package com.invitique.domain.repository;

import com.invitique.domain.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, String> {
    Optional<Template> findByIdIgnoreCase(String id);
    List<Template> findByIsActiveTrue();
}

