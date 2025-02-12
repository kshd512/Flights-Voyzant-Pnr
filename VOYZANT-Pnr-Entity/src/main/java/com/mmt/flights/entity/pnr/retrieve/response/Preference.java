package com.mmt.flights.entity.pnr.retrieve.response;

public class Preference {
    private WheelChairPreference wheelChairPreference;
    private String seatPreference;

    public WheelChairPreference getWheelChairPreference() {
        return wheelChairPreference;
    }

    public void setWheelChairPreference(WheelChairPreference wheelChairPreference) {
        this.wheelChairPreference = wheelChairPreference;
    }

    public String getSeatPreference() {
        return seatPreference;
    }

    public void setSeatPreference(String seatPreference) {
        this.seatPreference = seatPreference;
    }
}