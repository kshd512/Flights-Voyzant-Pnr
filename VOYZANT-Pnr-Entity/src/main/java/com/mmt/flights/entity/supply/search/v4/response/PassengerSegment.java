package com.mmt.flights.entity.supply.search.v4.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)

public class PassengerSegment {

    @JsonProperty("seats")
    @Valid
    private List<Seat> seats = null;
    @JsonProperty("passengerKey")
    private String passengerKey;
    @JsonProperty("activityDate")
    private String activityDate;
    @JsonProperty("boardingSequence")
    private String boardingSequence;
    @JsonProperty("createdDate")
    private String createdDate;
    @JsonProperty("liftStatus")
    private LiftStatus liftStatus;
    @JsonProperty("modifiedDate")
    private String modifiedDate;
    @JsonProperty("overBookIndicator")
    private Integer overBookIndicator;
    @JsonProperty("priorityDate")
    private String priorityDate;
    @JsonProperty("timeChanged")
    private Boolean timeChanged;
    @JsonProperty("verifiedTravelDocs")
    private Object verifiedTravelDocs;
    @JsonProperty("sourcePointOfSale")
    @Valid
    private SourcePointOfSale sourcePointOfSale;
    @JsonProperty("pointOfSale")
    @Valid
    private PointOfSale pointOfSale;
    @JsonProperty("ssrs")
    @Valid
    private List<Ssr> ssrs = null;
    @JsonProperty("tickets")
    @Valid
    private List<Object> tickets = null;
    @JsonProperty("bags")
    @Valid
    private List<Object> bags = null;
    @JsonProperty("scores")
    @Valid
    private List<Object> scores = null;
    @JsonProperty("boardingPassDetail")
    private Object boardingPassDetail;
    @JsonProperty("hasInfant")
    private Boolean hasInfant;
    @JsonProperty("seatPreferences")
    @Valid
    private SeatPreferences seatPreferences;
    @JsonProperty("bundleCode")
    private String bundleCode;
    @JsonProperty("verifiedTravelDocuments")
    private Object verifiedTravelDocuments;

    @JsonProperty("seats")
    public List<Seat> getSeats() {
        return seats;
    }

    @JsonProperty("seats")
    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    @JsonProperty("passengerKey")
    public String getPassengerKey() {
        return passengerKey;
    }

    @JsonProperty("passengerKey")
    public void setPassengerKey(String passengerKey) {
        this.passengerKey = passengerKey;
    }

    @JsonProperty("activityDate")
    public String getActivityDate() {
        return activityDate;
    }

    @JsonProperty("activityDate")
    public void setActivityDate(String activityDate) {
        this.activityDate = activityDate;
    }

    @JsonProperty("boardingSequence")
    public String getBoardingSequence() {
        return boardingSequence;
    }

    @JsonProperty("boardingSequence")
    public void setBoardingSequence(String boardingSequence) {
        this.boardingSequence = boardingSequence;
    }

    @JsonProperty("createdDate")
    public String getCreatedDate() {
        return createdDate;
    }

    @JsonProperty("createdDate")
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    @JsonProperty("liftStatus")
    public LiftStatus getLiftStatus() {
        return liftStatus;
    }

    @JsonProperty("liftStatus")
    public void setLiftStatus(LiftStatus liftStatus) {
        this.liftStatus = liftStatus;
    }

    @JsonProperty("modifiedDate")
    public String getModifiedDate() {
        return modifiedDate;
    }

    @JsonProperty("modifiedDate")
    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @JsonProperty("overBookIndicator")
    public Integer getOverBookIndicator() {
        return overBookIndicator;
    }

    @JsonProperty("overBookIndicator")
    public void setOverBookIndicator(Integer overBookIndicator) {
        this.overBookIndicator = overBookIndicator;
    }

    @JsonProperty("priorityDate")
    public String getPriorityDate() {
        return priorityDate;
    }

    @JsonProperty("priorityDate")
    public void setPriorityDate(String priorityDate) {
        this.priorityDate = priorityDate;
    }

    @JsonProperty("timeChanged")
    public Boolean getTimeChanged() {
        return timeChanged;
    }

    @JsonProperty("timeChanged")
    public void setTimeChanged(Boolean timeChanged) {
        this.timeChanged = timeChanged;
    }

    @JsonProperty("verifiedTravelDocs")
    public Object getVerifiedTravelDocs() {
        return verifiedTravelDocs;
    }

    @JsonProperty("verifiedTravelDocs")
    public void setVerifiedTravelDocs(Object verifiedTravelDocs) {
        this.verifiedTravelDocs = verifiedTravelDocs;
    }

    @JsonProperty("sourcePointOfSale")
    public SourcePointOfSale getSourcePointOfSale() {
        return sourcePointOfSale;
    }

    @JsonProperty("sourcePointOfSale")
    public void setSourcePointOfSale(SourcePointOfSale sourcePointOfSale) {
        this.sourcePointOfSale = sourcePointOfSale;
    }

    @JsonProperty("pointOfSale")
    public PointOfSale getPointOfSale() {
        return pointOfSale;
    }

    @JsonProperty("pointOfSale")
    public void setPointOfSale(PointOfSale pointOfSale) {
        this.pointOfSale = pointOfSale;
    }

    @JsonProperty("ssrs")
    public List<Ssr> getSsrs() {
        return ssrs;
    }

    @JsonProperty("ssrs")
    public void setSsrs(List<Ssr> ssrs) {
        this.ssrs = ssrs;
    }

    @JsonProperty("tickets")
    public List<Object> getTickets() {
        return tickets;
    }

    @JsonProperty("tickets")
    public void setTickets(List<Object> tickets) {
        this.tickets = tickets;
    }

    @JsonProperty("bags")
    public List<Object> getBags() {
        return bags;
    }

    @JsonProperty("bags")
    public void setBags(List<Object> bags) {
        this.bags = bags;
    }

    @JsonProperty("scores")
    public List<Object> getScores() {
        return scores;
    }

    @JsonProperty("scores")
    public void setScores(List<Object> scores) {
        this.scores = scores;
    }

    @JsonProperty("boardingPassDetail")
    public Object getBoardingPassDetail() {
        return boardingPassDetail;
    }

    @JsonProperty("boardingPassDetail")
    public void setBoardingPassDetail(Object boardingPassDetail) {
        this.boardingPassDetail = boardingPassDetail;
    }

    @JsonProperty("hasInfant")
    public Boolean getHasInfant() {
        return hasInfant;
    }

    @JsonProperty("hasInfant")
    public void setHasInfant(Boolean hasInfant) {
        this.hasInfant = hasInfant;
    }

    @JsonProperty("seatPreferences")
    public SeatPreferences getSeatPreferences() {
        return seatPreferences;
    }

    @JsonProperty("seatPreferences")
    public void setSeatPreferences(SeatPreferences seatPreferences) {
        this.seatPreferences = seatPreferences;
    }

    @JsonProperty("bundleCode")
    public String getBundleCode() {
        return bundleCode;
    }

    @JsonProperty("bundleCode")
    public void setBundleCode(String bundleCode) {
        this.bundleCode = bundleCode;
    }

    @JsonProperty("verifiedTravelDocuments")
    public Object getVerifiedTravelDocuments() {
        return verifiedTravelDocuments;
    }

    @JsonProperty("verifiedTravelDocuments")
    public void setVerifiedTravelDocuments(Object verifiedTravelDocuments) {
        this.verifiedTravelDocuments = verifiedTravelDocuments;
    }

}
