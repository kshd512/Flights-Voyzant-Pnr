package com.mmt.flights.common.enums;

public enum Gender {

    G_NOT_SET("G_NOT_SET", 0),
    FEMALE("F", 2),
    MALE("M", 3),
    OTHER("OTHER", 4),
    UNRECOGNIZED("UNRECOGNIZED", -1);

    private final String genderType;
    private final int genderTypeValue;

    Gender(String genderType, int genderTypeValue) {
        this.genderType = genderType;
        this.genderTypeValue = genderTypeValue;
    }

    public String getGenderType() {
        return this.genderType;
    }

    public int getGenderTypeValue() {
        return this.genderTypeValue;
    }

    /**
     * Returns the PaxType enum corresponding to the provided paxType string.
     *
     * @param genderType the paxType string to look up
     * @return the corresponding PaxType enum, or null if no match is found
     */
    public static int fromGenderType(String genderType) {
        for (Gender type : Gender.values()) {
            if (type.getGenderType().equalsIgnoreCase(genderType)) {
                return type.genderTypeValue;
            }
        }
        return G_NOT_SET.genderTypeValue;
    }
}
