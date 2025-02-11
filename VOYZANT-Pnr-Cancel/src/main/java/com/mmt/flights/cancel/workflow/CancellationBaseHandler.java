package com.mmt.flights.cancel.workflow;


import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.logging.HiveRequestResponseLogger;
import com.mmt.flights.common.logging.LogParams;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.logging.metric.MetricServices;
import com.mmt.flights.common.logging.metric.MetricType;
import com.mmt.flights.common.util.AdapterUtil;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.postsales.error.PSErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import com.mmt.flights.postsales.logger.FunnelStep;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.mmt.flights.supply.common.SupplyErrorDetailDTO;

import java.util.HashMap;

public class CancellationBaseHandler {

    private final HiveRequestResponseLogger hiveLogger;
    private final FunnelStep funnelStep;

    public CancellationBaseHandler(HiveRequestResponseLogger hiveLogger, FunnelStep funnelStep) {
        this.hiveLogger = hiveLogger;
        this.funnelStep = funnelStep;
    }

    public HashMap<String, Object> getMap(SupplyPnrCancelRequestDTO request) {
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put(FlowStateKey.REQUEST, request);
        newMap.put(FlowStateKey.SUPPLIER_PNR, request.getRequestCore().getSupplierPnr());
        newMap.put(FlowStateKey.LOG_KEY, request.getRequestConfig().getCorrelationId());
        newMap.put(FlowStateKey.LOB, request.getRequestConfig().getLob());
        newMap.put(FlowStateKey.SOURCE, request.getRequestConfig().getSource());
        newMap.put(FlowStateKey.CMS_ID, request.getRequestConfig().getCredentialId());
        newMap.put(FlowStateKey.FUNNEL_STEP, funnelStep);
        newMap.put(FlowStateKey.AIRLINE, request.getRequestCore().getValidatingCarrier());
        newMap.put(FlowStateKey.UUID, AdapterUtil.generateTransactionId(request.getRequestCore().getSupplierPnr()));
        return newMap;
    }


    public SupplyErrorDetailDTO logAndGetError(FlowState state) {

        Throwable t = state.getError();

        PSErrorEnum errorCode = PSCommonErrorEnum.FLT_UNKNOWN_ERROR;
        SupplyErrorDetailDTO downStreamError = null;
        if (t instanceof PSErrorException) {
            PSErrorException fce = (PSErrorException) t;
            if (fce.getPsErrorEnum() != null) {
                errorCode = fce.getPsErrorEnum();
                if (errorCode == PSCommonErrorEnum.FLT_UNKNOWN_ERROR) {
                    errorCode = PSCommonErrorEnum.RETRIABLE_ERROR_FROM_SUPPLIER;
                }
            }
        }
        MMTLogger.error(
                (new LogParams.LogParamsBuilder())
                        .lobSrcAndLogKey(state)
                        .serviceName(AdapterUtil.getMetricServiceName(MetricServices.PNR_CANCEL_REQUEST_ERROR.name(), errorCode))
                        .throwable(t)
                        .className(this.getClass().getName())
                        .build(),
                MetricType.LOG_FILE, MetricType.LOG_COUNTER);
        SupplyErrorDetailDTO supplyErrorDetail = AdapterUtil.getSupplyErrorDetail(errorCode, t.getMessage(), downStreamError);


        return supplyErrorDetail;
    }
}
