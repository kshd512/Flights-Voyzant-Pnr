package com.mmt.flights.flightutil.review.key;

import java.util.List;
import java.util.Map;

public class FareKey {
    private PaxFareInformation paxFareInformation;
    private FlightLevelInformation flightLevelInformation;
    private Map<Integer, List<FareKeySegment>> fareKeyMap;

    public void setPaxFareInformation(PaxFareInformation paxFareInformation) {
        this.paxFareInformation = paxFareInformation;
    }

    public void setFlightLevelInformation(FlightLevelInformation flightLevelInformation) {
        this.flightLevelInformation = flightLevelInformation;
    }

    public void setFareKeyMap(Map<Integer, List<FareKeySegment>> fareKeyMap) {
        this.fareKeyMap = fareKeyMap;
    }

    public String build() {
        StringBuilder key = new StringBuilder();
        key.append(paxFareInformation.toString());
        key.append(flightLevelInformation.toString());
        fareKeyMap.forEach((journeyIndex, segments) -> {
            segments.forEach(segment -> key.append(segment.toString()));
        });
        return key.toString();
    }
}