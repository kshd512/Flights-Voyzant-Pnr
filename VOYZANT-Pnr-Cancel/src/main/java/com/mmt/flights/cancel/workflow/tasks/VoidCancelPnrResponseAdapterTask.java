package com.mmt.flights.cancel.workflow.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.cache.aerospike.AerospikeBaseConfig;
import com.mmt.flights.cache.aerospike.AerospikeCacheService;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyPnrCancelResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VoidCancelPnrResponseAdapterTask implements MapTask {


    @Autowired
    private AerospikeCacheService cacheService;

    @Autowired
    private AerospikeBaseConfig aerospikeBaseConfig;

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        SupplyPnrCancelResponseDTO.Builder builder = SupplyPnrCancelResponseDTO.newBuilder();
        SupplyPnrCancelRequestDTO supplyPnrCancelRequestDTO = flowState.getValue(FlowStateKey.REQUEST);
        String voidCancelResponse = flowState.getValue(FlowStateKey.VOID_CANCEL_RESPONSE);

        return flowState.toBuilder().addValue(FlowStateKey.RESPONSE, builder.build()).build();
    }


}
