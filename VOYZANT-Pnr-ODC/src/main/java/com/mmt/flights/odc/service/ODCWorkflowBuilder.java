package com.mmt.flights.odc.service;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.WorkFlow;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.i5.odc.service.prepayment.*;
import com.mmt.flights.pnr.retrieve.workflow.tasks.CMSManagerTask;
import com.mmt.flights.pnr.retrieve.workflow.tasks.CommonDummyTask;
import com.mmt.flights.pnr.retrieve.workflow.tasks.LoginTask;
import com.mmt.flights.pnr.retrieve.workflow.tasks.RetrievePnrTask;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.postsales.error.PSErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mmt.api.rxflow.rule.Rules.*;

@Component
public class ODCWorkflowBuilder {

    public static WorkFlow odcSearchV1() {
        return new WorkFlow.Builder()
                .defineMap(CMSManagerTask.class)
                .toMap(LoginTask.class,retry(2).onError(ODCWorkflowBuilder::retryErrors))
                .toMap(RetrievePnrTask.class)
                .toMap(SearchRequestBuilderTaskV2.class)
                .toMap(SimpleSearchInvokerTask.class)
                .toMap(ODCSearchResponseAdapter.class, completeFlow())
                .build();
    }

    private static boolean retryErrors(Throwable e) {
        if (e instanceof PSErrorException) {
            PSErrorEnum errorCode = ((PSErrorException) e).getPsErrorEnum();
            return errorCode == PSCommonErrorEnum.LOGIN_REQUEST_ERROR || errorCode == PSCommonErrorEnum.FLT_UNKNOWN_ERROR || errorCode == PSCommonErrorEnum.UNDO_CHECKIN_ERROR;
        }
        return false;
    }
}
