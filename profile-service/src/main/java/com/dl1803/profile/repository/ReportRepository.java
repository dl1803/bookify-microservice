package com.dl1803.profile.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.dl1803.profile.entity.Report;

@Repository
public interface ReportRepository extends Neo4jRepository<Report, String> {

    boolean existsByReporterIdAndReportedId(String reporterId, String reportedId);
}
