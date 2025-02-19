package com.mmt.flights.pnr.validateCancel.workflow.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.flightsutil.AirportDetailsUtil;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import com.mmt.flights.common.properties.TechCancelConfig;
import com.mmt.flights.common.util.PnrValidationUtil;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.postsales.logger.FunnelStep;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.navitaire_45.booking.schemas.webservices.datacontracts.booking.Booking;
import com.navitaire_45.booking.schemas.webservices.servicecontracts.bookingservice.GetBookingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.HashSet;

@Component
public class ValidateCancelValidationFromGetBookingResponse implements MapTask {

    @Autowired
    TechCancelConfig techCancelConfig;

    @Autowired
    private AirportDetailsUtil airportUtil;

    @Override
    public FlowState run(FlowState state) throws Exception {
        SupplyPnrCancelRequestDTO requestDTO = state.getValue(FlowStateKey.PNR_VALIDATE_CANCEL_REQUEST);
        GetBookingResponse getBookingResponse = state.getValue(FlowStateKey.NavGetBookingRes, GetBookingResponse.class);
        HashSet<String> passengerNoSet = new HashSet<>();
        HashSet<String> flightNoSet = new HashSet<>();
        validateResponse(getBookingResponse.getBooking(), requestDTO, passengerNoSet, flightNoSet);
        boolean isMultiMode = false;
        if(PnrValidationUtil.isMultiPayment(getBookingResponse.getBooking())){
            isMultiMode = true;
        }
        FlowState newState = state.toBuilder().addValue(FlowStateKey.NavGetBookingRes, getBookingResponse)
                .addValue(FlowStateKey.IS_MULTI_MODE_PAYMENT, isMultiMode)
                .build();
        return newState;
    }

    private void validateResponse(Booking getBookingResponse, SupplyPnrCancelRequestDTO request, HashSet<String> passengerNoSet, HashSet<String> flightNoSet) throws ParseException {
        String validatingCarrier = request.getRequestCore().getValidatingCarrier();
        if (PnrValidationUtil.isUnticketedCodeSharePnr(getBookingResponse)) {
            throw new PSErrorException(PSCommonErrorEnum.EXT_PNR_NOT_TICKETED);
        } else if (request.getRequestCore().getPaxInfoList() != null && request.getRequestCore().getPaxInfoCount() > 0 &&
                PnrValidationUtil.isPaxNotMatch(getBookingResponse, request, passengerNoSet)) {
            throw new PSErrorException(PSCommonErrorEnum.EXT_PAX_DOES_NOT_EXIST);
        }
        else if (request.getRequestCore().getFlightsList() != null && request.getRequestCore().getFlightsCount() > 0
                && PnrValidationUtil.isFlightNotMatch(getBookingResponse, request, passengerNoSet, flightNoSet)) {
            throw new PSErrorException(PSCommonErrorEnum.EXT_FLIGHT_DOES_NOT_EXIST);
        } else if (!CollectionUtils.isEmpty(request.getRequestCore().getFlightsList())
                && !PnrValidationUtil.isAllSegmentPresent(getBookingResponse, request, passengerNoSet, flightNoSet)) {
            throw new PSErrorException(PSCommonErrorEnum.EXT_MISSING_SEGMENTS_IN_JOURNEY);
        } else if (request.getRequestCore().getFlightsList() != null && request.getRequestCore().getFlightsCount() > 0
                && PnrValidationUtil.isFlightCancelled(getBookingResponse, request)) {
            throw new PSErrorException(PSCommonErrorEnum.EXT_SEGMENT_CANCELLED_BY_AIRLINE);
        } else if (request.getRequestCore().getFlightsList() != null && request.getRequestCore().getFlightsCount() > 0
                && PnrValidationUtil.isBoardingDone(getBookingResponse, request, passengerNoSet, flightNoSet)) {
            throw new PSErrorException(PSCommonErrorEnum.EXT_ALREADY_BOARDED);
        } else if (validatingCarrier.equals("6E") && PnrValidationUtil.isInternational(getBookingResponse,airportUtil) && PnrValidationUtil.isFourHoursToDeparture(getBookingResponse, request, flightNoSet, airportUtil, FunnelStep.Validate)) {
            throw new PSErrorException(PSCommonErrorEnum.EXT_PNR_IN_NO_SHOW_WINDOW);
        }
        else if (validatingCarrier.equals("6E") && !PnrValidationUtil.isInternational(getBookingResponse,airportUtil) && PnrValidationUtil.isThreeHoursToDeparture(getBookingResponse, request, flightNoSet)) {
            throw new PSErrorException(PSCommonErrorEnum.EXT_PNR_IN_NO_SHOW_WINDOW);
        }
        else if (PnrValidationUtil.isTooCloseToDeparture(getBookingResponse, request, flightNoSet, airportUtil, FunnelStep.Validate)) {
            throw new PSErrorException(PSCommonErrorEnum.EXT_PNR_IN_NO_SHOW_WINDOW);
        }
        else if (PnrValidationUtil.isPnrinNoShow(getBookingResponse, request, passengerNoSet, flightNoSet)) {
            throw new PSErrorException(PSCommonErrorEnum.EXT_PNR_IN_NO_SHOW_WINDOW);
        }
        else if (!techCancelConfig.getUndoCheckinEnabledAirlineCodes().contains(validatingCarrier) &&
                PnrValidationUtil.isCheckedInDone(getBookingResponse, request, passengerNoSet, flightNoSet)) {
            throw new PSErrorException(PSCommonErrorEnum.EXT_CHECKED_IN_PNR_CANCELLATION_UNSUPPORTED);
        }
        else if (PnrValidationUtil.hasBalanceDue(getBookingResponse)) {
            throw new PSErrorException(PSCommonErrorEnum.EXT_BALANCE_DUE_ERROR);
        } else if (PnrValidationUtil.isFLightSuspended(getBookingResponse, request) && PnrValidationUtil.isCheckedInDone(getBookingResponse, request, passengerNoSet, flightNoSet)) {
            throw new PSErrorException(PSCommonErrorEnum.EXT_FLIGHT_AIRLINE_CANCELED);
        }
        else if (request.getRequestCore().getPaxInfoList() != null && request.getRequestCore().getPaxInfoList().size() > 0 && passengerNoSet.size() == 0) {
            throw new PSErrorException(PSCommonErrorEnum.EXT_ONLY_INFANT_CANCELLATION_NOT_ALLOWED);
        } else if (request.getRequestCore().getPaxInfoList() != null && request.getRequestCore().getPaxInfoList().size() > 0 && !PnrValidationUtil.isSplitPNRSupported(request, getBookingResponse, passengerNoSet)) {
            throw new PSErrorException(PSCommonErrorEnum.EXT_CANNOT_SPLIT_PNR);
        }
        else if(request.getRequestCore().getPaxInfoList() != null && request.getRequestCore().getPaxInfoList().size() > 0 && !PnrValidationUtil.isValidPartialPaxCancelRequest(getBookingResponse, passengerNoSet)){
            throw new PSErrorException(PSCommonErrorEnum.INVALID_PARTIAL_PAX_CANCEL_REQUEST);
        }

    }
}