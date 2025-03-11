package com.mmt.flights.odc.util;

import com.mmt.flights.entity.pnr.retrieve.response.*;
import com.mmt.flights.odc.common.CancelType;
import com.mmt.flights.odc.search.DateChangeSearchRequest;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ODCSearchUtil {
    
    public static CancelType evaluateCancelType(OrderViewRS orderViewRS, DateChangeSearchRequest odcRequest) {
        List<Order> orderList = orderViewRS.getOrder();
        if (orderList == null || orderList.isEmpty()) {
            throw new PSErrorException(PSCommonErrorEnum.EXT_PNR_IS_NOT_VALID_FOR_DATE_CHANGE);
        }
        
        Order order = orderList.get(0);
        List<OfferItem> offerItems = order.getOfferItem();
        
        if (offerItems == null || offerItems.isEmpty()) {
            throw new PSErrorException(PSCommonErrorEnum.EXT_PNR_IS_NOT_VALID_FOR_DATE_CHANGE);
        }

        if (offerItems.size() == 1) {
            return CancelType.OW;
        } else if(odcRequest.getItineraryList().size() > 1) {
            return CancelType.BothLegOfRT;
        } else {
            String arrivalStation = odcRequest.getItineraryList().get(0).getTo();
            String departureStation = odcRequest.getItineraryList().get(0).getFrom();

            for (int i = 0; i < offerItems.size(); i++) {
                OfferItem offerItem = offerItems.get(i);
                if (offerItem.getService() != null && !offerItem.getService().isEmpty()) {
                    Service service = offerItem.getService().get(0);
                    String departureStationToCancel = service.getDepartureAirport();
                    String arrivalStationToCancel = service.getArrivalAirport();
                    
                    if (arrivalStation.equalsIgnoreCase(arrivalStationToCancel) && 
                        departureStation.equalsIgnoreCase(departureStationToCancel)) {
                        if (i == 0) {
                            return CancelType.ForwardJourneyOfRoundTrip;
                        } else if (i == 1) {
                            return CancelType.ReturnJourneyOfRoundTrip;
                        }
                    }
                }
            }
        }
        throw new PSErrorException(PSCommonErrorEnum.EXT_PNR_IS_NOT_VALID_FOR_DATE_CHANGE);
    }

    public static List<String> getCancelJourneyBookingPClass(OrderViewRS orderViewRS, CancelType cancelType) {
        List<String> allowedClasses = new ArrayList<>();
        
        if (orderViewRS.getOrder() == null || orderViewRS.getOrder().isEmpty()) {
            return allowedClasses;
        }
        
        Order order = orderViewRS.getOrder().get(0);
        List<OfferItem> offerItems = order.getOfferItem();
        
        if (offerItems == null || offerItems.isEmpty()) {
            return allowedClasses;
        }

        if (cancelType.equals(CancelType.OW)) {
            String bookingPClass = getBookingPClass(offerItems.get(0));
            if (bookingPClass != null) {
                allowedClasses.add(bookingPClass);
            }
        } else {
            if (cancelType.equals(CancelType.ForwardJourneyOfRoundTrip) || 
                cancelType.equals(CancelType.BothLegOfRT)) {
                String forwardPClass = getBookingPClass(offerItems.get(0));
                if (forwardPClass != null) {
                    allowedClasses.add(forwardPClass);
                }
            }
            if (cancelType.equals(CancelType.ReturnJourneyOfRoundTrip) || 
                cancelType.equals(CancelType.BothLegOfRT)) {
                if (offerItems.size() > 1) {
                    String returnPClass = getBookingPClass(offerItems.get(1));
                    if (returnPClass != null) {
                        allowedClasses.add(returnPClass);
                    }
                }
            }
        }
        return allowedClasses;
    }

    private static String getBookingPClass(OfferItem offerItem) {
        if (offerItem.getFareComponent() != null && !offerItem.getFareComponent().isEmpty()) {
            FareComponent fareComponent = offerItem.getFareComponent().get(0);
            if (fareComponent.getFareBasis() != null && 
                fareComponent.getFareBasis().getFareBasisCode() != null) {
                return fareComponent.getFareBasis().getFareBasisCode().getCode();
            }
        }
        return null;
    }
}