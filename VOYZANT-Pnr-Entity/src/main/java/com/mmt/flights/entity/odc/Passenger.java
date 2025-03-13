package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mmt.flights.entity.pnr.retrieve.response.TravelDocument;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    @JsonProperty("LoyaltyProgramAccount")
    private List<Object> loyaltyProgramAccount;

    @JsonProperty("ContactInfoRef")
    private String contactInfoRef;

    @JsonProperty("attributes")
    private PassengerAttributes attributes;

    @JsonProperty("DocumentNumber")
    private String documentNumber;

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

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }
}