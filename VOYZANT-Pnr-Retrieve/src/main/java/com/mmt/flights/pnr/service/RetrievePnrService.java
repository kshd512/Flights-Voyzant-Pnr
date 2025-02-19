package com.mmt.flights.pnr.service;

import com.mmt.api.rxflow.FlowExecutor;
import com.mmt.flights.common.constants.CommonConstants;
import com.mmt.flights.pnr.workflow.PnrFlowHandler;
import com.mmt.flights.pnr.workflow.PnrWorkFlowBuilder;
import com.mmt.flights.supply.book.v4.response.SupplyBookingResponseDTO;
import com.mmt.flights.supply.pnr.v4.request.SupplyPnrRequestDTO;
import org.springframework.stereotype.Service;
import rx.Observable;

import java.util.Objects;

@Service
public class RetrievePnrService {


    public Observable<SupplyBookingResponseDTO> retrievePnr(SupplyPnrRequestDTO request,String lob, String src, String version) {
        return FlowExecutor
                .getBuilder(PnrWorkFlowBuilder.retrievePnr(),
                        new PnrFlowHandler(lob,src))
                .build().execute(Observable.just(request));
    }
}
