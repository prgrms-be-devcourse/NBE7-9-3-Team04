package com.backend.api.question.dto.response;

import java.util.List;


public record PortfolioListReadResponse(
        String title,
        Long count,
        List<PortfolioReadResponse> questions
) {
    public static PortfolioListReadResponse from(String title,Long count,List<PortfolioReadResponse> questions) {
        return new PortfolioListReadResponse(title,count, questions);
    }
}
