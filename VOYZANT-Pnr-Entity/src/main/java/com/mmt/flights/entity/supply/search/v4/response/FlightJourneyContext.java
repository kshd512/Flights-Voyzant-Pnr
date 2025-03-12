package com.mmt.flights.entity.supply.search.v4.response;

import com.mmt.flights.odc.search.SimpleJourney;

import java.util.*;

public class FlightJourneyContext {
    private final Map<String, Integer> flightKeyToJourneyIndex = new HashMap<>();
    private final List<SimpleJourney> allJourneys = new ArrayList<>();
    private int journeyIndex = 0;

    public Map<String, Integer> getFlightKeyToJourneyIndex() {
        return flightKeyToJourneyIndex;
    }

    public List<SimpleJourney> getAllJourneys() {
        return allJourneys;
    }

    public List<List<SimpleJourney>> getItineraryJourneyList() {
        return Collections.singletonList(allJourneys);
    }

    public void addJourney(String flightRef, SimpleJourney journey) {
        allJourneys.add(journey);
        flightKeyToJourneyIndex.put(flightRef, journeyIndex++);
    }
}
