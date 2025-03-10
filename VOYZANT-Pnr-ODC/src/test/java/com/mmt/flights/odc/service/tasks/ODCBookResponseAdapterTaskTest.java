package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.odc.commit.DateChangeCommitResponse;
import com.mmt.flights.odc.common.ConversionFactor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class ODCBookResponseAdapterTaskTest {

    @InjectMocks
    private ODCBookResponseAdapterTask task;
    
    private FlowState flowState;
    private String sampleJson;
    
    @Before
    public void setUp() throws IOException {
        flowState = new FlowState.Builder(System.currentTimeMillis()).build();
        
        // Load sample JSON from file
        sampleJson = "{\"OrderViewRS\":{\"Document\":{\"Name\":\"API GATEWAY\",\"ReferenceVersion\":\"1.2\"},\"Party\":{\"Sender\":{\"TravelAgencySender\":{\"Name\":\"Diva Travels\",\"IATA_Number\":\"\",\"AgencyID\":\"Diva Travels\",\"Contacts\":{\"Contact\":[{\"EmailContact\":\"vakarram@gmail.com\"}]}}}},\"ShoppingResponseId\":\"1721375083437470641\",\"Success\":{},\"Payments\":{\"Payment\":[{\"Type\":\"CHECK\",\"PassengerID\":\"ALL\",\"Amount\":740.29,\"ChequeNumber\":\"985632\"}]},\"Order\":[{\"OrderID\":\"DX6HXFPE\",\"GdsBookingReference\":\"PTE26Y\",\"OrderStatus\":\"SUCCESS\",\"PnrStatus\":\"\",\"ScheduleChangeIndicator\":false,\"NeedToTicket\":\"N\",\"OfferID\":\"1227102711721375087885644107\",\"Owner\":\"WY\",\"OwnerName\":\"Oman Air\",\"BrandedFareOptions\":[],\"InstantTicket\":\"N\",\"Eticket\":\"false\",\"TimeLimits\":{\"OfferExpirationDateTime\":\"2024-07-19T15:19:31\",\"PaymentExpirationDateTime\":\"2024-07-18 18:30:00\"},\"BookingCurrencyCode\":\"LKR\",\"EquivCurrencyCode\":\"CAD\",\"HstPercentage\":\"\",\"RewardSettings\":{\"RewardAvailable\":\"N\",\"PointTypes\":[],\"PointValues\":{}},\"BookingFeeInfo\":{\"FeeType\":\"\",\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"TotalPrice\":{\"BookingCurrencyPrice\":164510,\"EquivCurrencyPrice\":740.3},\"BasePrice\":{\"BookingCurrencyPrice\":91770,\"EquivCurrencyPrice\":412.97},\"TaxPrice\":{\"BookingCurrencyPrice\":72740,\"EquivCurrencyPrice\":327.33},\"Commission\":{\"AgencyCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"AgencyYqCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"PortalCharges\":{\"Markup\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Surcharge\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Discount\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"AgentMarkupInfo\":{\"OnflyMarkup\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"OnflyDiscount\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"OnflyHst\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"PromoDiscount\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0,\"PromoCode\":\"\"}},\"PaxSeatInfo\":[],\"OfferItem\":[{\"OfferItemID\":\"OFFERITEMID1\",\"Refundable\":\"false\",\"PassengerType\":\"ADT\",\"PassengerQuantity\":1,\"TotalPriceDetail\":{\"TotalAmount\":{\"BookingCurrencyPrice\":57110,\"EquivCurrencyPrice\":256.99}},\"Service\":[{\"ServiceID\":\"SV1\",\"PassengerRefs\":\"ADT1\",\"FlightRefs\":\"Flight1\"}],\"FareDetail\":{\"PassengerRefs\":\"ADT1\",\"Price\":{\"TotalAmount\":{\"BookingCurrencyPrice\":57110,\"EquivCurrencyPrice\":256.99},\"BaseAmount\":{\"BookingCurrencyPrice\":32760,\"EquivCurrencyPrice\":147.42},\"TaxAmount\":{\"BookingCurrencyPrice\":24350,\"EquivCurrencyPrice\":109.57},\"Commission\":{\"AgencyCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"AgencyYqCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"BookingFee\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"PortalCharges\":{\"Markup\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Surcharge\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Discount\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"Taxes\":[]}},\"FareComponent\":[{\"PriceClassRef\":\"PCR_1\",\"SegmentRefs\":\"Segment1\",\"FareBasis\":{\"FareBasisCode\":{\"Refs\":\"FG_1\",\"Code\":\"UELOIA\"},\"RBD\":\"U\",\"CabinType\":\"Y\",\"SeatLeft\":\"7\"}}]},{\"OfferItemID\":\"OFFERITEMID2\",\"Refundable\":\"false\",\"PassengerType\":\"ADT\",\"PassengerQuantity\":1,\"TotalPriceDetail\":{\"TotalAmount\":{\"BookingCurrencyPrice\":57110,\"EquivCurrencyPrice\":256.99}},\"Service\":[{\"ServiceID\":\"SV1\",\"PassengerRefs\":\"ADT1\",\"FlightRefs\":\"Flight1\"}],\"FareDetail\":{\"PassengerRefs\":\"ADT1\",\"Price\":{\"TotalAmount\":{\"BookingCurrencyPrice\":57110,\"EquivCurrencyPrice\":256.99},\"BaseAmount\":{\"BookingCurrencyPrice\":32760,\"EquivCurrencyPrice\":147.42},\"TaxAmount\":{\"BookingCurrencyPrice\":24350,\"EquivCurrencyPrice\":109.57},\"Commission\":{\"AgencyCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"AgencyYqCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"BookingFee\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"PortalCharges\":{\"Markup\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Surcharge\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Discount\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"Taxes\":[]}},\"FareComponent\":[{\"PriceClassRef\":\"PCR_1\",\"SegmentRefs\":\"Segment1\",\"FareBasis\":{\"FareBasisCode\":{\"Refs\":\"FG_1\",\"Code\":\"UELOIA\"},\"RBD\":\"U\",\"CabinType\":\"Y\",\"SeatLeft\":\"7\"}}]},{\"OfferItemID\":\"OFFERITEMID3\",\"Refundable\":\"false\",\"PassengerType\":\"CHD\",\"PassengerQuantity\":1,\"TotalPriceDetail\":{\"TotalAmount\":{\"BookingCurrencyPrice\":50290,\"EquivCurrencyPrice\":226.31}},\"Service\":[{\"ServiceID\":\"SV1\",\"PassengerRefs\":\"CHD1\",\"FlightRefs\":\"Flight1\"}],\"FareDetail\":{\"PassengerRefs\":\"CHD1\",\"Price\":{\"TotalAmount\":{\"BookingCurrencyPrice\":50290,\"EquivCurrencyPrice\":226.31},\"BaseAmount\":{\"BookingCurrencyPrice\":26250,\"EquivCurrencyPrice\":118.13},\"TaxAmount\":{\"BookingCurrencyPrice\":24040,\"EquivCurrencyPrice\":108.18},\"Commission\":{\"AgencyCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"AgencyYqCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"BookingFee\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"PortalCharges\":{\"Markup\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Surcharge\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Discount\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"Taxes\":[]}},\"FareComponent\":[{\"PriceClassRef\":\"PCR_1\",\"SegmentRefs\":\"Segment1\",\"FareBasis\":{\"FareBasisCode\":{\"Refs\":\"FG_1\",\"Code\":\"UELOIA\"},\"RBD\":\"U\",\"CabinType\":\"Y\",\"SeatLeft\":\"7\"}}]}],\"BaggageAllowance\":[{\"SegmentRefs\":\"Segment1\",\"PassengerRefs\":\"ADT1 ADT2\",\"BaggageAllowanceRef\":\"Bag1\"},{\"SegmentRefs\":\"Segment1\",\"PassengerRefs\":\"ADT1 ADT2\",\"BaggageAllowanceRef\":\"Bag1\"},{\"SegmentRefs\":\"Segment1\",\"PassengerRefs\":\"CHD1\",\"BaggageAllowanceRef\":\"Bag1\"}],\"SplitPaymentInfo\":[{\"AirItineraryId\":\"1227102711721375087885644107\",\"MultipleFop\":\"N\",\"MaxCardsPerPax\":0,\"MaxCardsPerPaxInMFOP\":0}],\"BookingToEquivExRate\":0.0045,\"FopRef\":\"FOP_0_0_0_0_ALL_ALL\"}],\"DataLists\":{\"PassengerList\":{\"Passengers\":[{\"PassengerID\":\"ADT1\",\"PTC\":\"ADT\",\"NameTitle\":\"Mr\",\"FirstName\":\"RAM\",\"MiddleName\":\"\",\"LastName\":\"KUMAR\",\"DocumentNumber\":\"9101305330348\",\"PassengerRefID\":\"ADT1\"},{\"PassengerID\":\"ADT2\",\"PTC\":\"ADT\",\"NameTitle\":\"Mr\",\"FirstName\":\"RAJ\",\"MiddleName\":\"\",\"LastName\":\"KUMAR\",\"DocumentNumber\":\"9101305330347\",\"PassengerRefID\":\"ADT2\"},{\"PassengerID\":\"CHD1\",\"PTC\":\"CHD\",\"NameTitle\":\"Mr\",\"FirstName\":\"SHANTHA\",\"MiddleName\":\"\",\"LastName\":\"KUMAR\",\"DocumentNumber\":\"9101305330349\",\"PassengerRefID\":\"CHD1\"}]},\"DisclosureList\":{\"Disclosures\":[]},\"FareList\":{\"FareGroup\":[{\"FareGroupRef\":\"FG_1\",\"FareCode\":\"70J\",\"FareBasisCode\":\"UELOIA\"}]},\"FlightSegmentList\":{\"FlightSegment\":[{\"SegmentKey\":\"Segment1\",\"Departure\":{\"AirportCode\":\"MAA\",\"Date\":\"2024-11-16\",\"Time\":\"09:40:00\",\"AirportName\":\"Chennai International Airport\",\"Terminal\":{\"Name\":\"\"}},\"Arrival\":{\"AirportCode\":\"MCT\",\"Date\":\"2024-11-16\",\"Time\":\"12:00:00\",\"AirportName\":\"Muscat International Airport\",\"Terminal\":{\"Name\":\"\"}},\"MarketingCarrier\":{\"AirlineID\":\"WY\",\"Name\":\"Oman Air\",\"FlightNumber\":\"252\"},\"OperatingCarrier\":{\"AirlineID\":\"WY\",\"Name\":\"Oman Air\",\"FlightNumber\":\"252\"},\"Equipment\":{\"AircraftCode\":\"7M8\",\"Name\":\"Boeing 737 MAX 8\"},\"Code\":{\"MarriageGroup\":\"O\"},\"FlightDetail\":{\"FlightDuration\":{\"Value\":\"3 H 50 M\"},\"Stops\":{\"Value\":0},\"InterMediate\":[],\"AirMilesFlown\":0},\"BrandId\":\"\"}]},\"FlightList\":{\"Flight\":[{\"FlightKey\":\"Flight1\",\"Journey\":{\"Time\":\"3 H 50 M\",\"Stops\":0},\"SegmentReferences\":\"Segment1\"}]},\"OriginDestinationList\":{\"OriginDestination\":[{\"OriginDestinationKey\":\"OD1\",\"DepartureCode\":\"MAA\",\"ArrivalCode\":\"MCT\",\"FlightReferences\":\"Flight1\"}]},\"PriceClassList\":{\"PriceClass\":[{\"PriceClassID\":\"PCR_1\",\"Name\":\"\",\"Code\":\"\",\"Descriptions\":{\"Description\":[]}}]},\"BaggageAllowanceList\":{\"BaggageAllowance\":[{\"BaggageAllowanceID\":\"Bag1\",\"BaggageCategory\":\"Checked\",\"AllowanceDescription\":{\"ApplicableParty\":\"Traveler\",\"Description\":\"CHECKED ALLOWANCE\"},\"PieceAllowance\":{\"ApplicableParty\":\"Traveler\",\"TotalQuantity\":\"30\",\"Unit\":\"kg\"}}]},\"FopList\":[{\"CC\":{\"Allowed\":\"N\",\"Types\":{}},\"DC\":{\"Allowed\":\"N\",\"Types\":{}},\"CHEQUE\":{\"Allowed\":\"Y\",\"Types\":{}},\"CASH\":{\"Allowed\":\"Y\",\"Types\":{}},\"ACH\":{\"Allowed\":\"N\",\"Types\":{}},\"PG\":{\"Allowed\":\"Y\",\"Types\":{}},\"FopKey\":\"FOP_0_0_0_0_ALL_ALL\"}]},\"TicketDocInfos\":{\"TicketDocInfo\":[{\"TicketDocument\":{\"TicketDocNbr\":\"9101305330350\",\"Type\":\"TKT\"},\"PassengerReference\":\"ADT2\",\"GdsBookingReference\":\"PTE26Y\"},{\"TicketDocument\":{\"TicketDocNbr\":\"9101305330351\",\"Type\":\"TKT\"},\"PassengerReference\":\"ADT1\",\"GdsBookingReference\":\"PTE26Y\"}]},\"MetaData\":{},\"threeDsResponse\":\"\"}}";
    }
    
    @Test
    public void testBookResponseConversion() throws Exception {

        // Add the sample JSON to FlowState
        flowState = flowState.toBuilder()
                .addValue(FlowStateKey.ODC_BOOK_RESPONSE, sampleJson)
                .build();
        
        // Execute the task
        FlowState resultState = task.run(flowState);
        
        // Get the converted response
        DateChangeCommitResponse commitResponse = resultState.getValue(FlowStateKey.RESPONSE);

        try {
            System.out.println(new ObjectMapper().writeValueAsString(commitResponse));
        } catch (Exception e) {
            // return ExceptionUtils.getStackTrace(e);
        }

        // Print the converted response to console
        System.out.println("=== Converted DateChangeCommitResponse ===");
        System.out.println("PNR: " + commitResponse.getPnr());
        System.out.println("Status: " + commitResponse.getStatus());
        System.out.println("Ticketing Required: " + commitResponse.getIsTicketingRequired());
        
        if (commitResponse.getConversionFactors() != null) {
            System.out.println("\nConversion Factors:");
            for (ConversionFactor factor : commitResponse.getConversionFactors()) {
                System.out.println("  From: " + factor.getFromCurrency() + 
                                   ", To: " + factor.getToCurrency() + 
                                   ", ROE: " + factor.getRoe());
            }
        }
        
        if (commitResponse.getExtraInformation() != null) {
            System.out.println("\nExtra Information:");
            for (Map.Entry<String, Object> entry : commitResponse.getExtraInformation().entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue());
            }
        }
        
        if (commitResponse.getError() != null) {
            System.out.println("\nError Details:");
            System.out.println("  Code: " + commitResponse.getError().getErrorCode());
            System.out.println("  Message: " + commitResponse.getError().getErrorMessage());
        }
    }
}