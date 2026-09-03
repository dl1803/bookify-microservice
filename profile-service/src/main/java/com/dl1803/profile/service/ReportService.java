package com.dl1803.profile.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.dl1803.profile.dto.request.ReportRequest;
import com.dl1803.profile.entity.Report;
import com.dl1803.profile.exception.AppException;
import com.dl1803.profile.exception.ErrorCode;
import com.dl1803.profile.mapper.ReportMapper;
import com.dl1803.profile.repository.ReportRepository;
import com.dl1803.profile.repository.UserProfileRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportService {
    ReportRepository reportRepository;
    UserProfileRepository userProfileRepository;
    ReportMapper reportMapper;

    public void reportUser(String targetUserId, ReportRequest request) {
        String currentUserId =
                SecurityContextHolder.getContext().getAuthentication().getName();

        if (currentUserId.equals(targetUserId)) {
            throw new AppException(ErrorCode.CANNOT_REPORT_SELF);
        }
        if (userProfileRepository.findByUserId(targetUserId).isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        if (reportRepository.existsByReporterIdAndReportedId(currentUserId, targetUserId)) {
            throw new AppException(ErrorCode.ALREADY_REPORTED);
        }

        Report report = reportMapper.toReport(request);
        report.setReporterId(currentUserId);
        report.setReportedId(targetUserId);
        reportRepository.save(report);
    }
}
