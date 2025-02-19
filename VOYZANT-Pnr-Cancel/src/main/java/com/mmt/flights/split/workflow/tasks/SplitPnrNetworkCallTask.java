package com.mmt.flights.split.workflow.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.CMSConstants;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
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
import com.mmt.flights.postsales.error.PSErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.mmt.flights.common.constants.FlowStateKey.LOG_KEY;

@Component
public class SplitPnrNetworkCallTask implements MapTask {

    @Autowired
    private HiveRequestResponseLogger hive;

    @Autowired
    private HttpClientUtil httpClientUtil;

    @Autowired
    private ConnectorEndpoints endpoints;

    @Autowired
    private JaxbHandlerService jaxB;

    @Override
    public FlowState run(FlowState state) throws Exception {
        TaskLog taskLog = new TaskLog(SupplierStep.SPLIT_ORDER);
        String splitPnrRequest = state.getValue(FlowStateKey.SPLIT_PNR_REQUEST);
        CMSMapHolder cmsMap = state.getValue(FlowStateKey.CMS_MAP);
        PSErrorEnum errorCode = ErrorEnum.EXT_SPLIT_PNR_FAILED;
        String splitPnrResponse = executeSplitPnrRequest(state, taskLog, splitPnrRequest, cmsMap, errorCode);
        return state.toBuilder().addValue(FlowStateKey.SPLIT_PNR_RESPONSE, splitPnrResponse).build();
    }

    private String executeSplitPnrRequest(FlowState state, TaskLog taskLog, String splitPnrRequest, 
                                        CMSMapHolder cmsMap, PSErrorEnum errorCode) {
        String splitPnrResponse = null;
        long startTime = System.currentTimeMillis();
        try {
            String url = cmsMap.getCmsMap().get(CMSConstants.HOST) + endpoints.getPnrSplitURL();
            taskLog.setUrl(url);
            taskLog.setRequest(MMTLogger.convertToJson(splitPnrRequest));
            
            splitPnrResponse = httpClientUtil.post(url, splitPnrRequest, String.class, cmsMap.getCmsMap());
            taskLog.setResponse(MMTLogger.convertToJson(splitPnrResponse));
            
            validateResponse(splitPnrResponse);
            errorCode = null; // Reset error code on success
        } catch (PSErrorException e) {
            errorCode = e.getPsErrorEnum();
            throw e;
        } catch (Exception e) {
            errorCode = ErrorEnum.EXT_SPLIT_PNR_FAILED;
            throw new PSErrorException("Error during split PNR network call: " + e.getMessage(), errorCode);
        } finally {
            taskLog.setError(errorCode);
            logEncrypted(state, taskLog);
            MMTLogger.logTimeForNetworkCall(state, MetricServices.PNR_SPLIT_NETWORK_CALL_LATENCY.name(), startTime);
        }
        return splitPnrResponse;
    }

    private void validateResponse(String splitPnrResponse) {
        if (splitPnrResponse == null || splitPnrResponse.trim().isEmpty()) {
            throw new PSErrorException("Empty response received from split PNR API", ErrorEnum.EXT_SPLIT_PNR_FAILED);
        }
    }

    private void logEncrypted(FlowState state, TaskLog taskLog) {
        String logKey = state.getValue(LOG_KEY);
        try {
            String response = jaxB.unMarshall(taskLog.getResponse(), String.class);
            ScrambleUtil.encodeUserData(response, logKey);
            taskLog.setResponse(jaxB.marshall(response));
        } catch (Exception e) {
            MMTLogger.error(logKey, "Error while encrypting split PNR response", this.getClass().getName(), e);
        }
        hive.pushLogs(state, taskLog);
    }
}