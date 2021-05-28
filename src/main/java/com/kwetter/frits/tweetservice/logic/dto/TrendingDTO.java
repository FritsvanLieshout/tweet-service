package com.kwetter.frits.tweetservice.logic.dto;

import java.util.List;

public class TrendingDTO {

    private List<String> trends;

    public TrendingDTO() {}

    public TrendingDTO(List<String> trends) { this.trends = trends; }

    public List<String> getTrends() { return trends; }

    public void setTrends(List<String> trends) { this.trends = trends; }
}
