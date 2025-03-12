package com.mmt.flights.pnr.service.tasks;

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
public class PnrRetrieveInvokerTask implements MapTask {

    @Autowired
    private HttpClientUtil httpClientUtil;

    @Autowired
    HiveRequestResponseLogger hive;

    @Autowired
    private ConnectorEndpoints endpoints;

    @Autowired
    private JaxbHandlerService jaxB;

    @Override
    public FlowState run(FlowState state) throws Exception {
        TaskLog taskLog = new TaskLog(SupplierStep.ORDER_DETAIL);
        String supplierPnrRequest = state.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_REQUEST);
        CMSMapHolder cmsMap = state.getValue(FlowStateKey.CMS_MAP);
        PSErrorEnum errorCode = PSCommonErrorEnum.OK;
        String supplierPNRResponse = extractAndValidateAndLogPnrRetrieveResponse(state, taskLog, supplierPnrRequest, cmsMap, errorCode);
        return state.toBuilder().addValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE, supplierPNRResponse).build();
    }

    private String extractAndValidateAndLogPnrRetrieveResponse(FlowState state, TaskLog taskLog, String supplierPnrRequest, CMSMapHolder cmsMap, PSErrorEnum errorCode) {
        String response;
        long startTime = System.currentTimeMillis();
        try {
            String url = cmsMap.getCmsMap().get(CMSConstants.HOST) + endpoints.getFetchPnrURL();
            taskLog.setUrl(url);
            taskLog.setRequest(MMTLogger.convertToJson(supplierPnrRequest));
            response = httpClientUtil.post(url, supplierPnrRequest, String.class, cmsMap.getCmsMap());
            taskLog.setResponse(MMTLogger.convertToJson(response));
        } catch (PSErrorException e) {
            errorCode = e.getPsErrorEnum();
            throw e;
        } catch (Exception e){
            errorCode = PSCommonErrorEnum.FLT_UNKNOWN_ERROR;
            throw new PSErrorException("Error while pnr retrieve Network call task" + e, PSCommonErrorEnum.FLT_UNKNOWN_ERROR);
        } finally {
            taskLog.setError(errorCode);
            logEncrypted(state, taskLog);
            MMTLogger.logTimeForNetworkCall(state, MetricServices.PNR_RETRIEVE_NETWORK_CALL_LATENCY.name(), startTime);
        }
        return response;
    }

    private void logEncrypted(FlowState state, TaskLog taskLog) {
        String logKey = state.getValue(LOG_KEY);
        try {
            String response = null;
            ScrambleUtil.encodeUserData(response,logKey);
            taskLog.setResponse(jaxB.marshall(response));
        }catch (Exception e){
            MMTLogger.error(logKey,"Error while encrypting pnr retrieve response",this.getClass().getName(),e);
        }
        hive.pushLogs(state, taskLog);
    }
}
