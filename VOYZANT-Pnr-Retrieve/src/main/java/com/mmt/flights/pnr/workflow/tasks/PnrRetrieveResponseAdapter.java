package com.mmt.flights.pnr.workflow.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.constants.VOYZANTConstants;
import com.mmt.flights.common.enums.Gender;
import com.mmt.flights.common.enums.PaxType;
import com.mmt.flights.common.enums.Seats;
import com.mmt.flights.common.logging.LogParams;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.logging.metric.MetricType;
import com.mmt.flights.common.util.AdapterUtil;
import com.mmt.flights.common.util.JaxbHandlerService;
import com.mmt.flights.flightsutil.AirportDetailsUtil;
import com.mmt.flights.util.DateUtil;
import com.mmt.flights.common.util.ResponseUtil;
import com.mmt.flights.supply.book.v4.common.SupplyFlightDetailDTO;
import com.mmt.flights.supply.book.v4.common.SupplyPassportInformation;
import com.mmt.flights.supply.book.v4.response.*;
import com.mmt.flights.supply.common.enums.*;
import com.mmt.flights.supply.pnr.v4.request.SupplyPnrRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.mmt.flights.common.constants.CommonConstants.NON_NUMBERS;


@Component
public class PnrRetrieveResponseAdapter implements MapTask {

    public static final String YYYY_MM_DD_T_HH_MM_SS_SSS = "yyyy-MM-dd'T'HH:mm:ss.sss";
    public static final String YYYY_MM_DD_SPACE_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String TIMEZONE_OFFSET = "+05:30";

    @Autowired
    JaxbHandlerService jaxbHandlerService;

    @Autowired
    private ResponseUtil responseUtil;

    @Override
    public FlowState run(FlowState state) throws Exception {
        SupplyBookingResponseDTO.Builder supplyBookingResponseDTO = SupplyBookingResponseDTO.newBuilder();
        String pnrResponseData = state.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE, String.class);
        SupplyPnrRequestDTO supplyPnrRequestDTO = state.getValue(FlowStateKey.REQUEST);

        supplyBookingResponseDTO.setStatus(SupplyStatus.SUCCESS);

        return state.toBuilder().addValue(FlowStateKey.RESPONSE, supplyBookingResponseDTO.build())
                .build();
    }
}