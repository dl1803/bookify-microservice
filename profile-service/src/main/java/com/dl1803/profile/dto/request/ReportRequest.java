package com.dl1803.profile.dto.request;

import jakarta.validation.constraints.NotNull;

import com.dl1803.profile.enums.ReportReason;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportRequest {
    @NotNull(message = "INVALID_KEY")
    ReportReason reason;

    String description;
}
