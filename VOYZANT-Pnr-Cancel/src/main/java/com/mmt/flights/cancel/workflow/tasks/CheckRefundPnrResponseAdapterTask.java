package com.mmt.flights.cancel.workflow.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.cache.aerospike.AerospikeBaseConfig;
import com.mmt.flights.cache.aerospike.AerospikeCacheService;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.postsales.logger.FunnelStep;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyPnrCancelResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CheckRefundPnrResponseAdapterTask implements MapTask {

    @Autowired
    private AerospikeCacheService cacheService;

    @Autowired
    private AerospikeBaseConfig aerospikeBaseConfig;

    @Override
    public FlowState run(FlowState state) throws Exception {
        SupplyPnrCancelResponseDTO.Builder builder = SupplyPnrCancelResponseDTO.newBuilder();
        String checkRefundResponse = state.getValue(FlowStateKey.CHECK_REFUND_RESPONSE);
        SupplyPnrCancelRequestDTO supplyPnrCancelRequestDTO = state.getValue(FlowStateKey.REQUEST);
        FunnelStep funnelStep = state.getValue(FlowStateKey.FUNNEL_STEP);


        return state.toBuilder().addValue(FlowStateKey.RESPONSE, builder.build()).build();
    }
}
