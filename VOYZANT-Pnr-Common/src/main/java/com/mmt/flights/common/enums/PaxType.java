package com.mmt.flights.common.enums;

/**
 */
public enum PaxType {

    PT_NOT_SET("PT_NOT_SET",0), ADULT("ADT", 1), CHILD("CHD", 2), INFANT("INF", 3);

    private final String paxtype;
    private final int paxTypeValue;

    PaxType(String paxtype, int paxTypeValue) {
        this.paxtype = paxtype;
        this.paxTypeValue = paxTypeValue;
    }

    public String getPaxType() {
        return this.paxtype;
    }

    /**
     * @return the complete paxTypeName eg- "Adult"
     */
    public int getPaxTypeName() {
        return this.paxTypeValue;
    }

    /**
     * Returns the PaxType enum corresponding to the provided paxType string.
     *
     * @param paxType the paxType string to look up
     * @return the corresponding PaxType enum, or null if no match is found
     */
    public static int fromPaxType(String paxType) {
        for (PaxType type : PaxType.values()) {
            if (type.getPaxType().equalsIgnoreCase(paxType)) {
                return type.paxTypeValue;
            }
        }
        return PT_NOT_SET.paxTypeValue;
    }

}