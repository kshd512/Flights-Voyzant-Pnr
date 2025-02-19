package com.mmt.flights.util;

import com.mmt.flights.flightsutil.AirportDetailsUtil;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class AirportDetailsService {

    private static AirportDetailsUtil airportDetailsUtil;

    @PostConstruct
    public void init() {
        try {
            airportDetailsUtil = AirportDetailsUtil.getInstance();
        } catch (IOException e) {
            // Handle the exception appropriately
            e.printStackTrace();
        }
    }

    public AirportDetailsUtil getAirportDetailsUtil() {
        return airportDetailsUtil;
    }
}
