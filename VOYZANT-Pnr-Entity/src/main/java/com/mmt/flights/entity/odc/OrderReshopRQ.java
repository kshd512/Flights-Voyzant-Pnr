package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderReshopRQ {
    @JsonProperty("Document")
    private Document document;

    @JsonProperty("Party")
    private Party party;

    @JsonProperty("Query")
    private Query query;

    @JsonProperty("DataLists")
    private DataLists dataLists;

    @JsonProperty("Preference")
    private Preference preference;

    @JsonProperty("MetaData")
    private MetaData metaData;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public DataLists getDataLists() {
        return dataLists;
    }

    public void setDataLists(DataLists dataLists) {
        this.dataLists = dataLists;
    }

    public Preference getPreference() {
        return preference;
    }

    public void setPreference(Preference preference) {
        this.preference = preference;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }
}

class Document {
    private String name;
    private String referenceVersion;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReferenceVersion() {
        return referenceVersion;
    }

    public void setReferenceVersion(String referenceVersion) {
        this.referenceVersion = referenceVersion;
    }
}

class Party {
    private Sender sender;

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }
}

class Sender {
    @JsonProperty("TravelAgencySender")
    private TravelAgencySender travelAgencySender;

    public TravelAgencySender getTravelAgencySender() {
        return travelAgencySender;
    }

    public void setTravelAgencySender(TravelAgencySender travelAgencySender) {
        this.travelAgencySender = travelAgencySender;
    }
}

class TravelAgencySender {
    private String name;
    private String IATA_Number = "";
    private String agencyID;
    private Contact contacts;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIATA_Number() {
        return IATA_Number;
    }

    public void setIATA_Number(String IATA_Number) {
        this.IATA_Number = IATA_Number;
    }

    public String getAgencyID() {
        return agencyID;
    }

    public void setAgencyID(String agencyID) {
        this.agencyID = agencyID;
    }

    public Contact getContacts() {
        return contacts;
    }

    public void setContacts(Contact contacts) {
        this.contacts = contacts;
    }
}

class Contact {
    private List<EmailContact> contact = new ArrayList<>();

    public List<EmailContact> getContact() {
        return contact;
    }

    public void setContact(List<EmailContact> contact) {
        this.contact = contact;
    }
}

class EmailContact {
    private String emailContact;

    public String getEmailContact() {
        return emailContact;
    }

    public void setEmailContact(String emailContact) {
        this.emailContact = emailContact;
    }
}

class Query {
    private String orderID;
    private List<String> gdsBookingReference = new ArrayList<>();
    private Reshop reshop;

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public List<String> getGdsBookingReference() {
        return gdsBookingReference;
    }

    public void setGdsBookingReference(List<String> gdsBookingReference) {
        this.gdsBookingReference = gdsBookingReference;
    }

    public Reshop getReshop() {
        return reshop;
    }

    public void setReshop(Reshop reshop) {
        this.reshop = reshop;
    }
}

class Reshop {
    private OrderServicing orderServicing;

    public OrderServicing getOrderServicing() {
        return orderServicing;
    }

    public void setOrderServicing(OrderServicing orderServicing) {
        this.orderServicing = orderServicing;
    }
}

class OrderServicing {
    private Add add = new Add();

    public Add getAdd() {
        return add;
    }

    public void setAdd(Add add) {
        this.add = add;
    }
}

class Add {
    private FlightQuery flightQuery;

    public FlightQuery getFlightQuery() {
        return flightQuery;
    }

    public void setFlightQuery(FlightQuery flightQuery) {
        this.flightQuery = flightQuery;
    }
}

class FlightQuery {
    private OriginDestinations originDestinations;

    public OriginDestinations getOriginDestinations() {
        return originDestinations;
    }

    public void setOriginDestinations(OriginDestinations originDestinations) {
        this.originDestinations = originDestinations;
    }
}

class OriginDestinations {
    private List<OriginDestination> originDestination = new ArrayList<>();

    public List<OriginDestination> getOriginDestination() {
        return originDestination;
    }

    public void setOriginDestination(List<OriginDestination> originDestination) {
        this.originDestination = originDestination;
    }
}

class OriginDestination {
    private Airport previousDeparture;
    private Airport previousArrival;
    private String previousCabinType;
    private Airport departure;
    private Airport arrival;
    private String cabinType;

    public Airport getPreviousDeparture() {
        return previousDeparture;
    }

    public void setPreviousDeparture(Airport previousDeparture) {
        this.previousDeparture = previousDeparture;
    }

    public Airport getPreviousArrival() {
        return previousArrival;
    }

    public void setPreviousArrival(Airport previousArrival) {
        this.previousArrival = previousArrival;
    }

    public String getPreviousCabinType() {
        return previousCabinType;
    }

    public void setPreviousCabinType(String previousCabinType) {
        this.previousCabinType = previousCabinType;
    }

    public Airport getDeparture() {
        return departure;
    }

    public void setDeparture(Airport departure) {
        this.departure = departure;
    }

    public Airport getArrival() {
        return arrival;
    }

    public void setArrival(Airport arrival) {
        this.arrival = arrival;
    }

    public String getCabinType() {
        return cabinType;
    }

    public void setCabinType(String cabinType) {
        this.cabinType = cabinType;
    }
}

class Airport {
    private String airportCode;
    private String date;

    public Airport(String airportCode, String date) {
        this.airportCode = airportCode;
        this.date = date;
    }

    public String getAirportCode() {
        return airportCode;
    }

    public void setAirportCode(String airportCode) {
        this.airportCode = airportCode;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}