package com.mmt.flights.cancel.service;

import com.mmt.api.rxflow.FlowExecutor;
import com.mmt.flights.cancel.workflow.*;
import com.mmt.flights.common.logging.HiveRequestResponseLogger;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyPnrCancelResponseDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyValidateCancelResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;

@Component
public class CancelPnrService {

    @Autowired
    HiveRequestResponseLogger hiveLogger;

    public Observable<SupplyValidateCancelResponseDTO> validateCancelPnr(SupplyPnrCancelRequestDTO request) {
        return FlowExecutor.getBuilder(CancelPnrWorkflowBuilder.validateCancelPnr(), new CancellationValidationHandler(hiveLogger)).build().execute(Observable.just(request));
    }

    public Observable<SupplyPnrCancelResponseDTO> cancelPnr(SupplyPnrCancelRequestDTO request) {
        return FlowExecutor.getBuilder(CancelPnrWorkflowBuilder.cancelPnr(), new CancellationHandler(hiveLogger)).build().execute(Observable.just(request));
    }

    public Observable<SupplyPnrCancelResponseDTO> partialPaxPnrCancel(SupplyPnrCancelRequestDTO request) {
        return FlowExecutor.getBuilder(CancelPnrWorkflowBuilder.partialPaxPnrCancel(), new CancellationHandler(hiveLogger)).build().execute(Observable.just(request));
    }
}
