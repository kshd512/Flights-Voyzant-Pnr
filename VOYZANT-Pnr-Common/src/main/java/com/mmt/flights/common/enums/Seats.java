package com.mmt.flights.common.enums;

public enum Seats {

    SPT_NOT_SET("spt_not_set",0), AISLE("aisle", 1), MIDDLE("middle", 2), WINDOW("window", 3), ANY("any", 4), UNRECOGNIZED("unrecognized", 5);

    private final String seatType;
    private final int seatTypeValue;

    Seats(String seatType, int seatTypeValue) {
        this.seatType = seatType;
        this.seatTypeValue = seatTypeValue;
    }

    public String getSeatType() {
        return seatType;
    }

    public int getSeatTypeValue() {
        return seatTypeValue;
    }

    /**
     * Returns the PaxType enum corresponding to the provided paxType string.
     *
     * @param seatType the paxType string to look up
     * @return the corresponding PaxType enum, or null if no match is found
     */
    public static int fromSeatType(String seatType) {
        for (Seats type : Seats.values()) {
            if (type.getSeatType().equalsIgnoreCase(seatType)) {
                return type.seatTypeValue;
            }
        }
        return SPT_NOT_SET.seatTypeValue;
    }
}
