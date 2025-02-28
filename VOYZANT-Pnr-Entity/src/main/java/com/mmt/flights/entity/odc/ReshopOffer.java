package com.mmt.flights.entity.odc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReshopOffer {
    @JsonProperty("OfferID")
    private String offerID;
    
    @JsonProperty("Owner")
    private String owner;
    
    @JsonProperty("OwnerName")
    private String ownerName;
    
    @JsonProperty("BrandedFareOptions")
    private List<String> brandedFareOptions;
    
    @JsonProperty("InstantTicket")
    private String instantTicket;
    
    @JsonProperty("Eticket")
    private String eticket;
    
    @JsonProperty("AllowHold")
    private String allowHold;
    
    @JsonProperty("TimeLimits")
    private TimeLimits timeLimits;
    
    @JsonProperty("PassportRequired")
    private String passportRequired;
    
    @JsonProperty("BookingCurrencyCode")
    private String bookingCurrencyCode;
    
    @JsonProperty("EquivCurrencyCode")
    private String equivCurrencyCode;
    
    @JsonProperty("HstPercentage")
    private String hstPercentage;
    
    @JsonProperty("RewardSettings")
    private RewardSettings rewardSettings;
    
    @JsonProperty("BookingFeeInfo")
    private BookingFeeInfo bookingFeeInfo;
    
    @JsonProperty("TotalPrice")
    private Price totalPrice;
    
    @JsonProperty("BasePrice")
    private Price basePrice;
    
    @JsonProperty("TaxPrice")
    private Price taxPrice;
    
    @JsonProperty("Commission")
    private Commission commission;
    
    @JsonProperty("PortalCharges")
    private PortalCharges portalCharges;
    
    @JsonProperty("AddOfferItem")
    private List<OfferItem> addOfferItem;
    
    @JsonProperty("BaggageAllowance")
    private List<BaggageAllowance> baggageAllowance;
    
    @JsonProperty("SplitPaymentInfo")
    private List<SplitPaymentInfo> splitPaymentInfo;
    
    @JsonProperty("BookingToEquivExRate")
    private double bookingToEquivExRate;
    
    @JsonProperty("FopRef")
    private String fopRef;

    // Getters and setters for all fields
    public String getOfferID() {
        return offerID;
    }

    public void setOfferID(String offerID) {
        this.offerID = offerID;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public List<String> getBrandedFareOptions() {
        return brandedFareOptions;
    }

    public void setBrandedFareOptions(List<String> brandedFareOptions) {
        this.brandedFareOptions = brandedFareOptions;
    }

    public String getInstantTicket() {
        return instantTicket;
    }

    public void setInstantTicket(String instantTicket) {
        this.instantTicket = instantTicket;
    }

    public String getEticket() {
        return eticket;
    }

    public void setEticket(String eticket) {
        this.eticket = eticket;
    }

    public String getAllowHold() {
        return allowHold;
    }

    public void setAllowHold(String allowHold) {
        this.allowHold = allowHold;
    }

    public TimeLimits getTimeLimits() {
        return timeLimits;
    }

    public void setTimeLimits(TimeLimits timeLimits) {
        this.timeLimits = timeLimits;
    }

    public String getPassportRequired() {
        return passportRequired;
    }

    public void setPassportRequired(String passportRequired) {
        this.passportRequired = passportRequired;
    }

    public String getBookingCurrencyCode() {
        return bookingCurrencyCode;
    }

    public void setBookingCurrencyCode(String bookingCurrencyCode) {
        this.bookingCurrencyCode = bookingCurrencyCode;
    }

    public String getEquivCurrencyCode() {
        return equivCurrencyCode;
    }

    public void setEquivCurrencyCode(String equivCurrencyCode) {
        this.equivCurrencyCode = equivCurrencyCode;
    }

    public String getHstPercentage() {
        return hstPercentage;
    }

    public void setHstPercentage(String hstPercentage) {
        this.hstPercentage = hstPercentage;
    }

    public RewardSettings getRewardSettings() {
        return rewardSettings;
    }

    public void setRewardSettings(RewardSettings rewardSettings) {
        this.rewardSettings = rewardSettings;
    }

    public BookingFeeInfo getBookingFeeInfo() {
        return bookingFeeInfo;
    }

    public void setBookingFeeInfo(BookingFeeInfo bookingFeeInfo) {
        this.bookingFeeInfo = bookingFeeInfo;
    }

    public Price getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Price totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Price getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Price basePrice) {
        this.basePrice = basePrice;
    }

    public Price getTaxPrice() {
        return taxPrice;
    }

    public void setTaxPrice(Price taxPrice) {
        this.taxPrice = taxPrice;
    }

    public Commission getCommission() {
        return commission;
    }

    public void setCommission(Commission commission) {
        this.commission = commission;
    }

    public PortalCharges getPortalCharges() {
        return portalCharges;
    }

    public void setPortalCharges(PortalCharges portalCharges) {
        this.portalCharges = portalCharges;
    }

    public List<OfferItem> getAddOfferItem() {
        return addOfferItem;
    }

    public void setAddOfferItem(List<OfferItem> addOfferItem) {
        this.addOfferItem = addOfferItem;
    }

    public List<BaggageAllowance> getBaggageAllowance() {
        return baggageAllowance;
    }

    public void setBaggageAllowance(List<BaggageAllowance> baggageAllowance) {
        this.baggageAllowance = baggageAllowance;
    }

    public List<SplitPaymentInfo> getSplitPaymentInfo() {
        return splitPaymentInfo;
    }

    public void setSplitPaymentInfo(List<SplitPaymentInfo> splitPaymentInfo) {
        this.splitPaymentInfo = splitPaymentInfo;
    }

    public double getBookingToEquivExRate() {
        return bookingToEquivExRate;
    }

    public void setBookingToEquivExRate(double bookingToEquivExRate) {
        this.bookingToEquivExRate = bookingToEquivExRate;
    }

    public String getFopRef() {
        return fopRef;
    }

    public void setFopRef(String fopRef) {
        this.fopRef = fopRef;
    }
}