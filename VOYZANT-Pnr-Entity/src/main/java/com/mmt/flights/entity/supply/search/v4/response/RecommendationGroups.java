package com.mmt.flights.entity.supply.search.v4.response;


import com.mmt.flights.odc.v2.SimpleSearchRecommendationGroupV2;

import java.util.ArrayList;
import java.util.List;

public class RecommendationGroups {
    public final List<SimpleSearchRecommendationGroupV2> sameFareGroups = new ArrayList<>();
    public final List<SimpleSearchRecommendationGroupV2> otherFareGroups = new ArrayList<>();
}