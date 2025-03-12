package com.mmt.flights.odc.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.entity.pnr.retrieve.response.DataLists;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import com.mmt.flights.entity.pnr.retrieve.response.Passenger;
import com.mmt.flights.entity.pnr.retrieve.response.PassengerList;
import com.mmt.flights.odc.prepayment.DateChangePrePaymentRequest;
import com.mmt.flights.odc.service.tasks.ODCExchangePriceRequestBuilderTask;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ODCExchangePriceRequestBuilderTaskTest {

    @InjectMocks
    private ODCExchangePriceRequestBuilderTask task;

    //@Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testBuildExchangePriceRequest() throws Exception {
        // Prepare test data
        DateChangePrePaymentRequest request = new DateChangePrePaymentRequest();
        request.setRKey("1721375083437470641,1227102711721375087919515147");
        request.setMmtId("MMT123");
        request.setPnr("ABC123");

        // Create OrderViewRS with test passenger data
        OrderViewRS orderViewRS = new OrderViewRS();
        DataLists dataLists = new DataLists();
        PassengerList passengerList = new PassengerList();
        List<Passenger> passengers = new ArrayList<>();
        
        Passenger passenger = new Passenger();
        passenger.setPassengerID("PAX1");
        passenger.setPtc("ADT");
        passenger.setNameTitle("Mr");
        passenger.setFirstName("John");
        passenger.setLastName("Doe");
        passenger.setBirthDate("2025-12-12");
        passenger.setGender("MALE");
        passengers.add(passenger);
        
        passengerList.setPassengers(passengers);
        dataLists.setPassengerList(passengerList);
        orderViewRS.setDataLists(dataLists);

        // Create FlowState with test data
        Map<String, Object> stateMap = new HashMap<>();
        stateMap.put(FlowStateKey.REQUEST, request);
        stateMap.put(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE, objectMapper.writeValueAsString(orderViewRS));
        FlowState flowState = new FlowState.Builder(System.currentTimeMillis()).addAll(stateMap).build();

        // Execute task
        FlowState result = task.run(flowState);

        // Print the output JSON
        String exchangePriceRequest = result.getValue(FlowStateKey.ODC_EXCHANGE_PRICE_REQUEST);
        System.out.println("Generated Exchange Price Request:");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                objectMapper.readTree(exchangePriceRequest)));
    }
}