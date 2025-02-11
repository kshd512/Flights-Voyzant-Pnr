package com.mmt.flights.cancel.workflow.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.CMSConstants;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.logging.HiveRequestResponseLogger;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.logging.SupplierStep;
import com.mmt.flights.common.logging.TaskLog;
import com.mmt.flights.common.logging.metric.MetricServices;
import com.mmt.flights.common.util.HttpClientUtil;
import com.mmt.flights.common.util.JaxbHandlerService;
import com.mmt.flights.common.util.ScrambleUtil;
import com.mmt.flights.config.ConnectorEndpoints;
import com.mmt.flights.entity.cms.CMSMapHolder;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.postsales.error.PSErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.mmt.flights.common.constants.FlowStateKey.LOG_KEY;

@Component
public class CancelReleaseNetworkCallTask implements MapTask {

    @Autowired
    HiveRequestResponseLogger hive;

    @Autowired
    private HttpClientUtil httpClientUtil;

    @Autowired
    private ConnectorEndpoints endpoints;

    @Autowired
    private JaxbHandlerService jaxB;


    @Override
    public FlowState run(FlowState state) throws Exception {
        TaskLog taskLog = new TaskLog(SupplierStep.CANCEL_RELEASE);
        String cancelReleaseRequest = state.getValue(FlowStateKey.CANCEL_RELEASE_REQUEST);
        CMSMapHolder cmsMap = state.getValue(FlowStateKey.CMS_MAP);
        PSErrorEnum errorCode = PSCommonErrorEnum.OK;
        String cancelReleaseResponse = extractAndValidateAndCancelReleaseResponse(state, taskLog, cancelReleaseRequest, cmsMap, errorCode);
        return state.toBuilder().addValue(FlowStateKey.CANCEL_RELEASE_RESPONSE, cancelReleaseResponse).build();
    }

    // Extracts, validates and logs the response of the void cancel pnr network call
    private String extractAndValidateAndCancelReleaseResponse(FlowState state, TaskLog taskLog, String cancelReleaseRequest, CMSMapHolder cmsMap, PSErrorEnum errorCode) {
        String cancelReleaseResponse = null;
        Long startTime = System.currentTimeMillis();
        try {
            String url = cmsMap.getCmsMap().get(CMSConstants.HOST) + endpoints.getCancelReleaseUrl();
            taskLog.setUrl(url);
            taskLog.setRequest(MMTLogger.convertToJson(cancelReleaseRequest));
            cancelReleaseResponse = httpClientUtil.post(url, cancelReleaseRequest, String.class, cmsMap.getCmsMap());
            taskLog.setResponse(MMTLogger.convertToJson(cancelReleaseResponse));
            validateResponse(cancelReleaseResponse);
        } catch (PSErrorException e) {
            errorCode = e.getPsErrorEnum();
            throw e;
        } catch (Exception e) {
            errorCode = PSCommonErrorEnum.FLT_UNKNOWN_ERROR;
            throw new PSErrorException("Error while void cancel pnr Network call task" + e, PSCommonErrorEnum.FLT_UNKNOWN_ERROR);
        } finally {
            taskLog.setError(errorCode);
            logEncrypted(state, taskLog);
            MMTLogger.logTimeForNetworkCall(state, MetricServices.PNR_CANCEL_RELEASE_NETWORK_CALL_LATENCY.name(), startTime);
        }
        return cancelReleaseResponse;
    }

    private void validateResponse(String checkRefundResponse) {
    }

    // Encrypts the response of the void cancel pnr network call
    private void logEncrypted(FlowState state, TaskLog taskLog) {
        String logKey = state.getValue(LOG_KEY);
        try {
            String response = jaxB.unMarshall(taskLog.getResponse(), String.class);
            ScrambleUtil.encodeUserData(response, logKey);
            taskLog.setResponse(jaxB.marshall(response));
        } catch (Exception e) {
            MMTLogger.error(logKey, "Error while encrypting void cancel release response", this.getClass().getName(), e);
        }
        hive.pushLogs(state, taskLog);
    }
}
