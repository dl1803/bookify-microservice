package com.dl1803.profile.entity;

import java.time.Instant;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import com.dl1803.profile.enums.ReportReason;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Node("report")
public class Report {
    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    String id;

    @Property("reporterId")
    String reporterId; // userId của người report

    @Property("reportedId")
    String reportedId; // userId của người bị report

    ReportReason reason;

    String description;

    @Builder.Default
    Instant createdAt = Instant.now();
}
