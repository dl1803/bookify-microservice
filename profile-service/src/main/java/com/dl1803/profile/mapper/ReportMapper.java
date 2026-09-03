package com.dl1803.profile.mapper;

import org.mapstruct.Mapper;

import com.dl1803.profile.dto.request.ReportRequest;
import com.dl1803.profile.entity.Report;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    Report toReport(ReportRequest request);
}
