
package com.mmt.flights.entity.supply.search.v4.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.validation.Valid;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "serviceCharges",
    "discountCode",
    "fareDiscountCode",
    "passengerType"
})
public class PassengerFare {

    @JsonProperty("serviceCharges")
    @Valid
    private List<ServiceCharge> serviceCharges = null;
    @JsonProperty("discountCode")
    private String discountCode;
    @JsonProperty("fareDiscountCode")
    private Object fareDiscountCode;
    @JsonProperty("passengerType")
    private String passengerType;

    @JsonProperty("serviceCharges")
    public List<ServiceCharge> getServiceCharges() {
        return serviceCharges;
    }

    @JsonProperty("serviceCharges")
    public void setServiceCharges(List<ServiceCharge> serviceCharges) {
        this.serviceCharges = serviceCharges;
    }

    @JsonProperty("discountCode")
    public String getDiscountCode() {
        return discountCode;
    }

    @JsonProperty("discountCode")
    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    @JsonProperty("fareDiscountCode")
    public Object getFareDiscountCode() {
        return fareDiscountCode;
    }

    @JsonProperty("fareDiscountCode")
    public void setFareDiscountCode(Object fareDiscountCode) {
        this.fareDiscountCode = fareDiscountCode;
    }

    @JsonProperty("passengerType")
    public String getPassengerType() {
        return passengerType;
    }

    @JsonProperty("passengerType")
    public void setPassengerType(String passengerType) {
        this.passengerType = passengerType;
    }

}
