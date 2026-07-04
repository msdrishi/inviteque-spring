package com.invitique.domain.repository;

import com.invitique.domain.model.VisitorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VisitorLogRepository extends JpaRepository<VisitorLog, UUID> {

    @Query("SELECT COUNT(DISTINCT v.ipAddress) FROM VisitorLog v")
    long countUniqueVisitors();

    @Query("SELECT COUNT(DISTINCT v.ipAddress) FROM VisitorLog v WHERE v.path = '/'")
    long countUniqueHomepageVisitors();

    @Query("SELECT v.templateId, COUNT(v) FROM VisitorLog v WHERE v.templateId IS NOT NULL GROUP BY v.templateId")
    List<Object[]> countReachByTemplate();

    @Query("SELECT v.templateId, COUNT(DISTINCT v.ipAddress) FROM VisitorLog v WHERE v.templateId IS NOT NULL GROUP BY v.templateId")
    List<Object[]> countUniqueReachByTemplate();
}
