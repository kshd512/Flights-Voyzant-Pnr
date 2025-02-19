package com.mmt.flights.entity.pnr.retrieve.response;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Passenger {
    @JsonProperty("PassengerID")
    private String passengerID;
    @JsonProperty("PTC")
    private String ptc;
    @JsonProperty("BirthDate")
    private String birthDate;
    @JsonProperty("NameTitle")
    private String nameTitle;
    @JsonProperty("FirstName")
    private String firstName;
    @JsonProperty("MiddleName")
    private String middleName;
    @JsonProperty("LastName")
    private String lastName;
    @JsonProperty("Gender")
    private String gender;
    @JsonProperty("TravelDocument")
    private TravelDocument travelDocument;
    @JsonProperty("Preference")
    private Preference preference;
    @JsonProperty("LoyaltyProgramAccount")
    private List<Object> loyaltyProgramAccount;
    @JsonProperty("ContactInfoRef")
    private String contactInfoRef;
    @JsonProperty("attributes")
    private PassengerAttributes attributes;

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

    public PassengerAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(PassengerAttributes attributes) {
        this.attributes = attributes;
    }
}