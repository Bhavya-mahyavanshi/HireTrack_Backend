package com.HireTrack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private Integer totalApplied;
    private Integer totalInterviews;
    private Integer totalOffers;
    private Integer totalRejected;
    private List<StatusCount> statusBreakdown;
    private List<ApplicationResponce> upcomingFollowUps;
}
