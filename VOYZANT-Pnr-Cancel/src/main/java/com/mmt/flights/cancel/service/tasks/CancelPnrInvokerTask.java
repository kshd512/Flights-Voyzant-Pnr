package com.mmt.flights.cancel.service.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.CMSConstants;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.logging.SupplierStep;
import com.mmt.flights.common.logging.TaskLog;
import com.mmt.flights.common.logging.metric.MetricServices;
import com.mmt.flights.common.util.HttpClientUtil;
import com.mmt.flights.config.ConnectorEndpoints;
import com.mmt.flights.entity.cms.CMSMapHolder;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.postsales.error.PSErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import com.mmt.flights.util.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CancelPnrInvokerTask implements MapTask {

    @Autowired
    private HttpClientUtil httpClientUtil;

    @Autowired
    private ConnectorEndpoints endpoints;

    @Autowired
    private LoggingService loggingService;

    @Override
    public FlowState run(FlowState state) throws Exception {
        TaskLog taskLog = new TaskLog(SupplierStep.CANCEL_ORDER);
        String cancelPnrRequest = state.getValue(FlowStateKey.CANCEL_PNR_REQUEST);
        CMSMapHolder cmsMap = state.getValue(FlowStateKey.CMS_MAP);
        PSErrorEnum errorCode = PSCommonErrorEnum.OK;
        String cancelPnrResponse = extractAndValidateAndCancelPnrResponse(state, taskLog, cancelPnrRequest, cmsMap, errorCode);
        return state.toBuilder().addValue(FlowStateKey.CANCEL_PNR_RESPONSE, cancelPnrResponse).build();
    }

    private String extractAndValidateAndCancelPnrResponse(FlowState state, TaskLog taskLog, String cancelPnrRequest, CMSMapHolder cmsMap, PSErrorEnum errorCode) {
        String cancelPnrResponse = null;
        long startTime = System.currentTimeMillis();
        try {
            String url = cmsMap.getCmsMap().get(CMSConstants.HOST) + endpoints.getPnrCancelURL();
            taskLog.setUrl(url);
            taskLog.setRequest(MMTLogger.convertToJson(cancelPnrRequest));
            cancelPnrResponse = httpClientUtil.post(url, cancelPnrRequest, String.class, cmsMap.getCmsMap());
            taskLog.setResponse(MMTLogger.convertToJson(cancelPnrResponse));
        } catch (PSErrorException e) {
            errorCode = e.getPsErrorEnum();
            throw e;
        } catch (Exception e) {
            errorCode = PSCommonErrorEnum.FLT_UNKNOWN_ERROR;
            throw new PSErrorException("Error while void cancel pnr Network call task" + e, PSCommonErrorEnum.FLT_UNKNOWN_ERROR);
        } finally {
            taskLog.setError(errorCode);
            loggingService.logEncrypted(state, taskLog);
            MMTLogger.logTimeForNetworkCall(state, MetricServices.PNR_CANCEL_NETWORK_CALL_LATENCY.name(), startTime);
        }
        return cancelPnrResponse;
    }
}