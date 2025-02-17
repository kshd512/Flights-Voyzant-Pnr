package com.mmt.flights.entity.split.request;

import lombok.Data;

import java.util.List;

@Data
public class PassengerList {
    private List<Passenger> passenger;
}