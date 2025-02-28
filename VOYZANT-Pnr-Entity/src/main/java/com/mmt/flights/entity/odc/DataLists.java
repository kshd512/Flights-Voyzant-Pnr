package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataLists {
    @JsonProperty("PassengerList")
    private PassengerList passengerList;

    @JsonProperty("DisclosureList")
    private DisclosureList disclosureList;

    @JsonProperty("FareList")
    private FareList fareList;

    @JsonProperty("FlightSegmentList")
    private FlightSegmentList flightSegmentList;

    @JsonProperty("FlightList")
    private FlightList flightList;

    @JsonProperty("OriginDestinationList")
    private OriginDestinationList originDestinationList;

    @JsonProperty("PriceClassList")
    private PriceClassList priceClassList;

    @JsonProperty("BaggageAllowanceList")
    private BaggageAllowanceList baggageAllowanceList;

    @JsonProperty("FopList")
    private List<FopList> fopList;

    public PassengerList getPassengerList() {
        return passengerList;
    }

    public void setPassengerList(PassengerList passengerList) {
        this.passengerList = passengerList;
    }

    public DisclosureList getDisclosureList() {
        return disclosureList;
    }

    public void setDisclosureList(DisclosureList disclosureList) {
        this.disclosureList = disclosureList;
    }

    public FareList getFareList() {
        return fareList;
    }

    public void setFareList(FareList fareList) {
        this.fareList = fareList;
    }

    public FlightSegmentList getFlightSegmentList() {
        return flightSegmentList;
    }

    public void setFlightSegmentList(FlightSegmentList flightSegmentList) {
        this.flightSegmentList = flightSegmentList;
    }

    public FlightList getFlightList() {
        return flightList;
    }

    public void setFlightList(FlightList flightList) {
        this.flightList = flightList;
    }

    public OriginDestinationList getOriginDestinationList() {
        return originDestinationList;
    }

    public void setOriginDestinationList(OriginDestinationList originDestinationList) {
        this.originDestinationList = originDestinationList;
    }

    public PriceClassList getPriceClassList() {
        return priceClassList;
    }

    public void setPriceClassList(PriceClassList priceClassList) {
        this.priceClassList = priceClassList;
    }

    public BaggageAllowanceList getBaggageAllowanceList() {
        return baggageAllowanceList;
    }

    public void setBaggageAllowanceList(BaggageAllowanceList baggageAllowanceList) {
        this.baggageAllowanceList = baggageAllowanceList;
    }

    public List<FopList> getFopList() {
        return fopList;
    }

    public void setFopList(List<FopList> fopList) {
        this.fopList = fopList;
    }
}

class PassengerList {
    private List<Passenger> passenger = new ArrayList<>();

    public List<Passenger> getPassenger() {
        return passenger;
    }

    public void setPassenger(List<Passenger> passenger) {
        this.passenger = passenger;
    }
}

class Passenger {
    private String passengerID;
    private String pTC;
    private String nameTitle;
    private String firstName;
    private String middleName;
    private String lastName;
    private String documentNumber;

    // Getters and setters
    public String getPassengerID() {
        return passengerID;
    }

    public void setPassengerID(String passengerID) {
        this.passengerID = passengerID;
    }

    public String getPTC() {
        return pTC;
    }

    public void setPTC(String pTC) {
        this.pTC = pTC;
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

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }
}

class DisclosureList {
    private List<Object> disclosures = new ArrayList<>();

    public List<Object> getDisclosures() {
        return disclosures;
    }

    public void setDisclosures(List<Object> disclosures) {
        this.disclosures = disclosures;
    }
}

class FareList {
    private List<FareGroup> fareGroup = new ArrayList<>();

    public List<FareGroup> getFareGroup() {
        return fareGroup;
    }

    public void setFareGroup(List<FareGroup> fareGroup) {
        this.fareGroup = fareGroup;
    }
}

class FareGroup {
    private String fareGroupRef;
    private String fareCode;
    private String fareBasisCode;

    // Getters and setters
    public String getFareGroupRef() {
        return fareGroupRef;
    }

    public void setFareGroupRef(String fareGroupRef) {
        this.fareGroupRef = fareGroupRef;
    }

    public String getFareCode() {
        return fareCode;
    }

    public void setFareCode(String fareCode) {
        this.fareCode = fareCode;
    }

    public String getFareBasisCode() {
        return fareBasisCode;
    }

    public void setFareBasisCode(String fareBasisCode) {
        this.fareBasisCode = fareBasisCode;
    }
}

class FlightSegmentList {
    private List<FlightSegment> flightSegment = new ArrayList<>();

    public List<FlightSegment> getFlightSegment() {
        return flightSegment;
    }

    public void setFlightSegment(List<FlightSegment> flightSegment) {
        this.flightSegment = flightSegment;
    }
}

class FlightSegment {
    private String segmentKey;
    private AirportInfo departure;
    private AirportInfo arrival;
    private Carrier marketingCarrier;
    private Carrier operatingCarrier;
    private Equipment equipment;
    private FlightCode code;
    private FlightDetail flightDetail;
    private String brandId;

    // Getters and setters
    // ... add getters and setters for all fields
}

class AirportInfo {
    private String airportCode;
    private String date;
    private String time;
    private String airportName;
    private Terminal terminal;

    // Getters and setters
}

class Terminal {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

class Carrier {
    private String airlineID;
    private String name;
    private String flightNumber;

    // Getters and setters
}

class Equipment {
    private String aircraftCode;
    private String name;

    // Getters and setters
}

class FlightCode {
    private String marriageGroup;

    public String getMarriageGroup() {
        return marriageGroup;
    }

    public void setMarriageGroup(String marriageGroup) {
        this.marriageGroup = marriageGroup;
    }
}

class FlightDetail {
    private FlightDuration flightDuration;
    private Stops stops;
    private List<Object> interMediate = new ArrayList<>();
    private int airMilesFlown;

    // Getters and setters
}

class FlightDuration {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

class Stops {
    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}

class FlightList {
    private List<Flight> flight = new ArrayList<>();

    public List<Flight> getFlight() {
        return flight;
    }

    public void setFlight(List<Flight> flight) {
        this.flight = flight;
    }
}

class Flight {
    private String flightKey;
    private Journey journey;
    private String segmentReferences;

    // Getters and setters
}

class Journey {
    private String time;
    private int stops;

    // Getters and setters
}

class OriginDestinationList {
    private List<ODInfo> originDestination = new ArrayList<>();

    public List<ODInfo> getOriginDestination() {
        return originDestination;
    }

    public void setOriginDestination(List<ODInfo> originDestination) {
        this.originDestination = originDestination;
    }
}

class ODInfo {
    private String originDestinationKey;
    private String departureCode;
    private String arrivalCode;
    private String flightReferences;

    // Getters and setters
}

class PriceClassList {
    private List<PriceClass> priceClass = new ArrayList<>();

    public List<PriceClass> getPriceClass() {
        return priceClass;
    }

    public void setPriceClass(List<PriceClass> priceClass) {
        this.priceClass = priceClass;
    }
}

class PriceClass {
    private String priceClassID;
    private String name;
    private String code;
    private Descriptions descriptions;

    // Getters and setters
}

class Descriptions {
    private List<String> description = new ArrayList<>();

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }
}

class BaggageAllowanceList {
    private List<BaggageAllowanceInfo> baggageAllowance = new ArrayList<>();

    public List<BaggageAllowanceInfo> getBaggageAllowance() {
        return baggageAllowance;
    }

    public void setBaggageAllowance(List<BaggageAllowanceInfo> baggageAllowance) {
        this.baggageAllowance = baggageAllowance;
    }
}

class BaggageAllowanceInfo {
    private String baggageAllowanceID;
    private String baggageCategory;
    private AllowanceDescription allowanceDescription;
    private PieceAllowance pieceAllowance;

    // Getters and setters
}

class AllowanceDescription {
    private String applicableParty;
    private String description;

    // Getters and setters
}

class PieceAllowance {
    private String applicableParty;
    private String totalQuantity;
    private String unit;

    // Getters and setters
}

class FopList {
    private CC cc;
    private DC dc;
    private PaymentMethod cheque;
    private PaymentMethod cash;
    private PaymentMethod ach;
    private PaymentMethod pg;
    private String fopKey;

    // Getters and setters
}

class CC {
    private String allowed;
    private Object types;

    // Getters and setters
}

class DC {
    private String allowed;
    private Object types;

    // Getters and setters
}

class PaymentMethod {
    private String allowed;
    private Object types;

    // Getters and setters
}