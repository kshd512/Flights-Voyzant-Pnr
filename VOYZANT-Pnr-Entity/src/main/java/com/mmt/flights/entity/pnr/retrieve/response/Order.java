package com.mmt.flights.entity.pnr.retrieve.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order {
    @JsonProperty("OrderID")
    private String orderID;
    @JsonProperty("GdsBookingReference")
    private String gdsBookingReference;
    @JsonProperty("OrderStatus")
    private String orderStatus;
    @JsonProperty("PaymentStatus")
    private String paymentStatus;
    @JsonProperty("TicketStatus")
    private String ticketStatus;
    @JsonProperty("NeedToTicket")
    private String needToTicket;
    @JsonProperty("OfferID")
    private String offerID;
    @JsonProperty("Owner")
    private String owner;
    @JsonProperty("OwnerName")
    private String ownerName;
    @JsonProperty("IsBrandedFare")
    private String isBrandedFare;
    @JsonProperty("BrandedFareOptions")
    private List<Object> brandedFareOptions;
    @JsonProperty("CabinOptions")
    private List<Object> cabinOptions;
    @JsonProperty("IsAdditionalCabinType")
    private String isAdditionalCabinType;
    @JsonProperty("Eticket")
    private String eticket;
    @JsonProperty("TimeLimits")
    private TimeLimits timeLimits;
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
    private PriceDetail totalPrice;
    @JsonProperty("BasePrice")
    private PriceDetail basePrice;
    @JsonProperty("TaxPrice")
    private PriceDetail taxPrice;
    @JsonProperty("Commission")
    private Commission commission;
    @JsonProperty("PortalCharges")
    private PortalCharges portalCharges;
    @JsonProperty("AgentMarkupInfo")
    private AgentMarkupInfo agentMarkupInfo;
    @JsonProperty("Penalty")
    private Penalty penalty;
    @JsonProperty("PaxSeatInfo")
    private List<Object> paxSeatInfo;
    @JsonProperty("OfferItem")
    private List<OfferItem> offerItem;
    @JsonProperty("BaggageAllowance")
    private List<BaggageAllowanceData> baggageAllowance;
    @JsonProperty("SplitPaymentInfo")
    private List<SplitPaymentInfo> splitPaymentInfo;
    @JsonProperty("BookingToEquivExRate")
    private Double bookingToEquivExRate;
    @JsonProperty("FopRef")
    private String fopRef;

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getGdsBookingReference() {
        return gdsBookingReference;
    }

    public void setGdsBookingReference(String gdsBookingReference) {
        this.gdsBookingReference = gdsBookingReference;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public String getNeedToTicket() {
        return needToTicket;
    }

    public void setNeedToTicket(String needToTicket) {
        this.needToTicket = needToTicket;
    }

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

    public String getIsBrandedFare() {
        return isBrandedFare;
    }

    public void setIsBrandedFare(String isBrandedFare) {
        this.isBrandedFare = isBrandedFare;
    }

    public List<Object> getBrandedFareOptions() {
        return brandedFareOptions;
    }

    public void setBrandedFareOptions(List<Object> brandedFareOptions) {
        this.brandedFareOptions = brandedFareOptions;
    }

    public List<Object> getCabinOptions() {
        return cabinOptions;
    }

    public void setCabinOptions(List<Object> cabinOptions) {
        this.cabinOptions = cabinOptions;
    }

    public String getIsAdditionalCabinType() {
        return isAdditionalCabinType;
    }

    public void setIsAdditionalCabinType(String isAdditionalCabinType) {
        this.isAdditionalCabinType = isAdditionalCabinType;
    }

    public String getEticket() {
        return eticket;
    }

    public void setEticket(String eticket) {
        this.eticket = eticket;
    }

    public TimeLimits getTimeLimits() {
        return timeLimits;
    }

    public void setTimeLimits(TimeLimits timeLimits) {
        this.timeLimits = timeLimits;
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

    public PriceDetail getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(PriceDetail totalPrice) {
        this.totalPrice = totalPrice;
    }

    public PriceDetail getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(PriceDetail basePrice) {
        this.basePrice = basePrice;
    }

    public PriceDetail getTaxPrice() {
        return taxPrice;
    }

    public void setTaxPrice(PriceDetail taxPrice) {
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

    public AgentMarkupInfo getAgentMarkupInfo() {
        return agentMarkupInfo;
    }

    public void setAgentMarkupInfo(AgentMarkupInfo agentMarkupInfo) {
        this.agentMarkupInfo = agentMarkupInfo;
    }

    public Penalty getPenalty() {
        return penalty;
    }

    public void setPenalty(Penalty penalty) {
        this.penalty = penalty;
    }

    public List<Object> getPaxSeatInfo() {
        return paxSeatInfo;
    }

    public void setPaxSeatInfo(List<Object> paxSeatInfo) {
        this.paxSeatInfo = paxSeatInfo;
    }

    public List<OfferItem> getOfferItem() {
        return offerItem;
    }

    public void setOfferItem(List<OfferItem> offerItem) {
        this.offerItem = offerItem;
    }

    public List<BaggageAllowanceData> getBaggageAllowance() {
        return baggageAllowance;
    }

    public void setBaggageAllowance(List<BaggageAllowanceData> baggageAllowance) {
        this.baggageAllowance = baggageAllowance;
    }

    public List<SplitPaymentInfo> getSplitPaymentInfo() {
        return splitPaymentInfo;
    }

    public void setSplitPaymentInfo(List<SplitPaymentInfo> splitPaymentInfo) {
        this.splitPaymentInfo = splitPaymentInfo;
    }

    public Double getBookingToEquivExRate() {
        return bookingToEquivExRate;
    }

    public void setBookingToEquivExRate(Double bookingToEquivExRate) {
        this.bookingToEquivExRate = bookingToEquivExRate;
    }

    public String getFopRef() {
        return fopRef;
    }

    public void setFopRef(String fopRef) {
        this.fopRef = fopRef;
    }
}