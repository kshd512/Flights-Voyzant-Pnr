package com.mmt.flights.pnr.workflow.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import org.springframework.stereotype.Component;

@Component
public class DummyTask implements MapTask {

	@Override
	public FlowState run(FlowState state) throws Exception {
		return state;
	}

}
