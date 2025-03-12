package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ODCBookInvokerTask implements MapTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ODCBookInvokerTask.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HttpClientUtil httpClientUtil;

    @Autowired
    private ConnectorEndpoints endpoints;

    @Override
    public FlowState run(FlowState state) throws Exception {
        TaskLog taskLog = new TaskLog(SupplierStep.ORDER_CHANGE_BOOK);
        String bookRequest = state.getValue(FlowStateKey.ODC_BOOK_REQUEST);
        CMSMapHolder cmsMap = state.getValue(FlowStateKey.CMS_MAP);
        PSErrorEnum errorCode = PSCommonErrorEnum.OK;
        String bookResponse = null;
        long startTime = System.currentTimeMillis();

        try {
            // Get the URL from the CMS map and endpoints
            String url = cmsMap.getCmsMap().get(CMSConstants.HOST) + endpoints.getPnrSplitURL();
            taskLog.setUrl(url);
            taskLog.setRequest(MMTLogger.convertToJson(bookRequest));
            
            LOGGER.info("Making ODC book API call to URL: {}", url);
            
            // Make the API call
            bookResponse = httpClientUtil.post(url, bookRequest, String.class, cmsMap.getCmsMap());
            
            LOGGER.info("Received ODC book response");
            taskLog.setResponse(MMTLogger.convertToJson(bookResponse));
        } catch (PSErrorException e) {
            LOGGER.error("PSErrorException during ODC book network call", e);
            errorCode = e.getPsErrorEnum();
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception during ODC book network call", e);
            errorCode = PSCommonErrorEnum.FLT_UNKNOWN_ERROR;
            throw new PSErrorException("Error during ODC book network call: " + e.getMessage(), 
                                      PSCommonErrorEnum.FLT_UNKNOWN_ERROR);
        } finally {
            taskLog.setError(errorCode);
            MMTLogger.logTimeForNetworkCall(state, MetricServices.REQUEST_LATENCY.name(), startTime);
        }

        // Add the response to the flow state for the adapter task to process
        return state.toBuilder()
                .addValue(FlowStateKey.ODC_BOOK_RESPONSE, bookResponse)
                .build();
    }
}