package com.mmt.flights.entity.pnr.retrieve.response;

import java.util.List;

public class Passenger {
    private String passengerID;
    private String ptc;
    private String birthDate;
    private String nameTitle;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private TravelDocument travelDocument;
    private Preference preference;
    private List<Object> loyaltyProgramAccount;
    private String contactInfoRef;

    public String getPassengerID() {
        return passengerID;
    }

    public void setPassengerID(String passengerID) {
        this.passengerID = passengerID;
    }

    public String getPtc() {
        return ptc;
    }

    public void setPtc(String ptc) {
        this.ptc = ptc;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getNameTitle() {
        return nameTitle;
    }

    public void setNameTitle(String nameTitle) {
        this.nameTitle = nameTitle;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public TravelDocument getTravelDocument() {
        return travelDocument;
    }

    public void setTravelDocument(TravelDocument travelDocument) {
        this.travelDocument = travelDocument;
    }

    public Preference getPreference() {
        return preference;
    }

    public void setPreference(Preference preference) {
        this.preference = preference;
    }

    public List<Object> getLoyaltyProgramAccount() {
        return loyaltyProgramAccount;
    }

    public void setLoyaltyProgramAccount(List<Object> loyaltyProgramAccount) {
        this.loyaltyProgramAccount = loyaltyProgramAccount;
    }

    public String getContactInfoRef() {
        return contactInfoRef;
    }

    public void setContactInfoRef(String contactInfoRef) {
        this.contactInfoRef = contactInfoRef;
    }
}