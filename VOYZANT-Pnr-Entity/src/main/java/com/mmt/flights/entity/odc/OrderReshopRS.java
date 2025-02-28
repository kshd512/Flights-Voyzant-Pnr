package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class OrderReshopRS {
    private Document document;
    private Party party;
    private String shoppingResponseId;
    private Success success;
    private List<ReshopOffer> reshopOffers;
    private DataLists dataLists;
    private MetaData metaData;

    public boolean isSuccess() {
        return success != null;
    }

    // Getters and setters for all fields
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

    public String getShoppingResponseId() {
        return shoppingResponseId;
    }

    public void setShoppingResponseId(String shoppingResponseId) {
        this.shoppingResponseId = shoppingResponseId;
    }

    public Success getSuccess() {
        return success;
    }

    public void setSuccess(Success success) {
        this.success = success;
    }

    public List<ReshopOffer> getReshopOffers() {
        return reshopOffers;
    }

    public void setReshopOffers(List<ReshopOffer> reshopOffers) {
        this.reshopOffers = reshopOffers;
    }

    public DataLists getDataLists() {
        return dataLists;
    }

    public void setDataLists(DataLists dataLists) {
        this.dataLists = dataLists;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }
}

class Success {
    // Empty class as per response structure
}

class ReshopOffer {
    private String offerID;
    private String owner;
    private String ownerName;
    private List<String> brandedFareOptions;
    private String instantTicket;
    private String eticket;
    private String allowHold;
    private TimeLimits timeLimits;
    private String passportRequired;
    private String bookingCurrencyCode;
    private String equivCurrencyCode;
    private String hstPercentage;
    private RewardSettings rewardSettings;
    private BookingFeeInfo bookingFeeInfo;
    private Price totalPrice;
    private Price basePrice;
    private Price taxPrice;
    private Commission commission;
    private PortalCharges portalCharges;
    private List<OfferItem> addOfferItem;
    private List<BaggageAllowance> baggageAllowance;
    private List<SplitPaymentInfo> splitPaymentInfo;
    private double bookingToEquivExRate;
    private String fopRef;

    // Getters and setters for all fields
    // ... add all getters and setters
}

class TimeLimits {
    private String offerExpirationDateTime;
    private String paymentExpirationDateTime;

    // Getters and setters
    public String getOfferExpirationDateTime() {
        return offerExpirationDateTime;
    }

    public void setOfferExpirationDateTime(String offerExpirationDateTime) {
        this.offerExpirationDateTime = offerExpirationDateTime;
    }

    public String getPaymentExpirationDateTime() {
        return paymentExpirationDateTime;
    }

    public void setPaymentExpirationDateTime(String paymentExpirationDateTime) {
        this.paymentExpirationDateTime = paymentExpirationDateTime;
    }
}

class RewardSettings {
    private String rewardAvailable;
    private List<String> pointTypes;
    private Map<String, Object> pointValues;

    // Getters and setters
}

class BookingFeeInfo {
    private String feeType;
    private double bookingCurrencyPrice;
    private double equivCurrencyPrice;

    // Getters and setters
}

class Price {
    private double bookingCurrencyPrice;
    private double equivCurrencyPrice;

    public double getBookingCurrencyPrice() {
        return bookingCurrencyPrice;
    }

    public void setBookingCurrencyPrice(double bookingCurrencyPrice) {
        this.bookingCurrencyPrice = bookingCurrencyPrice;
    }

    public double getEquivCurrencyPrice() {
        return equivCurrencyPrice;
    }

    public void setEquivCurrencyPrice(double equivCurrencyPrice) {
        this.equivCurrencyPrice = equivCurrencyPrice;
    }
}

class Commission {
    private Price agencyCommission;
    private Price agencyYqCommission;

    // Getters and setters
}

class PortalCharges {
    private Price markup;
    private Price surcharge;
    private Price discount;

    // Getters and setters
}

class OfferItem {
    private String offerItemID;
    private String refundable;
    private String passengerType;
    private int passengerQuantity;
    private TotalPriceDetail totalPriceDetail;
    private List<Service> service;
    private FareDetail fareDetail;
    private List<FareComponent> fareComponent;

    // Getters and setters
}

class TotalPriceDetail {
    private Price totalAmount;

    // Getters and setters
}

class Service {
    private String serviceID;
    private String passengerRefs;
    private String flightRefs;

    // Getters and setters
}

class FareDetail {
    private String passengerRefs;
    private Price price;

    // Getters and setters
}

class FareComponent {
    private String priceClassRef;
    private String segmentRefs;
    private FareBasis fareBasis;

    // Getters and setters
}

class FareBasis {
    private FareBasisCode fareBasisCode;
    private String rbd;
    private String cabinType;
    private String seatLeft;

    // Getters and setters
}

class FareBasisCode {
    private String refs;
    private String code;

    // Getters and setters
}

class BaggageAllowance {
    private String segmentRefs;
    private String passengerRefs;
    private String baggageAllowanceRef;

    // Getters and setters
}

class SplitPaymentInfo {
    private String airItineraryId;
    private String multipleFop;
    private int maxCardsPerPax;
    private int maxCardsPerPaxInMFOP;

    // Getters and setters
}