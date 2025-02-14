package com.mmt.flights.pnr.workflow.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.util.JaxbHandlerService;
import com.mmt.flights.entity.cms.CMSMapHolder;
import com.mmt.flights.supply.book.v4.response.SupplyBookingJourneyDTO;
import com.mmt.flights.supply.book.v4.response.SupplyBookingResponseDTO;
import com.mmt.flights.supply.book.v4.response.SupplyFareDetailDTO;
import com.mmt.flights.supply.pnr.v4.request.SupplyPnrRequestDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PnrRetrieveResponseAdapterTest {

    @InjectMocks
    private PnrRetrieveResponseAdapter adapter;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private JaxbHandlerService jaxbHandlerService;

    private String sampleResponse;
    private FlowState.Builder flowState;

    @Before
    public void setup() throws IOException, JAXBException {
        // Read the sample response from Response.txt
        //ClassPathResource resource = new ClassPathResource("Response.txt");
        sampleResponse = "{\"Document\":{\"Name\":\"API GATEWAY\",\"ReferenceVersion\":\"1.2\"},\"Party\":{\"Sender\":{\"TravelAgencySender\":{\"Name\":\"Lucky Travels\",\"IATA_Number\":\"\",\"AgencyID\":\"\",\"Contacts\":{\"Contact\":[{\"EmailContact\":\"pst@claritytts.com\"}]}}}},\"ShoppingResponseId\":\"1678878409066630254\",\"Success\":{},\"Payments\":{\"Payment\":[{\"Type\":\"CHECK\",\"PassengerID\":\"ALL\",\"Amount\":642.26,\"ChequeNumber\":\"323325\"}]},\"Order\":[{\"OrderID\":\"60ZPNZTI\",\"GdsBookingReference\":\"OUZMYO\",\"OrderStatus\":\"BOOKED\",\"PaymentStatus\":\"PAID\",\"TicketStatus\":\"NOT TICKETED\",\"NeedToTicket\":\"N\",\"OfferID\":\"137211721101678878414495216684\",\"Owner\":\"WS\",\"OwnerName\":\"Westjet\",\"IsBrandedFare\":\"Y\",\"BrandedFareOptions\":[],\"CabinOptions\":[],\"IsAdditionalCabinType\":\"N\",\"Eticket\":\"\",\"TimeLimits\":{\"OfferExpirationDateTime\":\"2023-03-15T13:20:18\"},\"BookingCurrencyCode\":\"CAD\",\"EquivCurrencyCode\":\"CAD\",\"HstPercentage\":\"\",\"RewardSettings\":{\"RewardAvailable\":\"N\",\"PointTypes\":[],\"PointValues\":{}},\"BookingFeeInfo\":{\"FeeType\":\"\",\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"TotalPrice\":{\"BookingCurrencyPrice\":232.35,\"EquivCurrencyPrice\":232.35},\"BasePrice\":{\"BookingCurrencyPrice\":195,\"EquivCurrencyPrice\":195},\"TaxPrice\":{\"BookingCurrencyPrice\":37.35,\"EquivCurrencyPrice\":37.35},\"Commission\":{\"AgencyCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"AgencyYqCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"PortalCharges\":{\"Markup\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Surcharge\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Discount\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"AgentMarkupInfo\":{\"OnflyMarkup\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"OnflyDiscount\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"OnflyHst\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"PromoDiscount\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0,\"PromoCode\":\"\"}},\"Penalty\":{\"ChangeFee\":{\"Before\":{\"BookingCurrencyPrice\":\"NA\",\"EquivCurrencyPrice\":\"NA\"},\"After\":{\"BookingCurrencyPrice\":\"NA\",\"EquivCurrencyPrice\":\"NA\"}},\"CancelationFee\":{\"Before\":{\"BookingCurrencyPrice\":\"NA\",\"EquivCurrencyPrice\":\"NA\"},\"After\":{\"BookingCurrencyPrice\":\"NA\",\"EquivCurrencyPrice\":\"NA\"}}},\"PaxSeatInfo\":[],\"OfferItem\":[{\"OfferItemID\":\"OFFERITEMID1\",\"Refundable\":\"0\",\"PassengerType\":\"ADT\",\"PassengerQuantity\":2,\"TotalPriceDetail\":{\"TotalAmount\":{\"BookingCurrencyPrice\":232.35,\"EquivCurrencyPrice\":232.35}},\"Service\":[{\"ServiceID\":\"SV1\",\"PassengerRefs\":\"ADT1,ADT2\",\"FlightRefs\":\"Flight1\"}],\"FareDetail\":{\"PassengerRefs\":\"ADT1,ADT2\",\"Price\":{\"TotalAmount\":{\"BookingCurrencyPrice\":232.35,\"EquivCurrencyPrice\":232.35},\"BaseAmount\":{\"BookingCurrencyPrice\":195,\"EquivCurrencyPrice\":195},\"TaxAmount\":{\"BookingCurrencyPrice\":37.35,\"EquivCurrencyPrice\":37.35},\"Commission\":{\"AgencyCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"AgencyYqCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"BookingFee\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"PortalCharges\":{\"Markup\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Surcharge\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Discount\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"Taxes\":[{\"TaxCode\":\"RC\",\"BookingCurrencyPrice\":15.23,\"EquivCurrencyPrice\":15.23},{\"TaxCode\":\"SQ\",\"BookingCurrencyPrice\":15,\"EquivCurrencyPrice\":15},{\"TaxCode\":\"CA\",\"BookingCurrencyPrice\":7.12,\"EquivCurrencyPrice\":7.12}]}},\"FareComponent\":[{\"PriceClassRef\":\"PCR_1\",\"SegmentRefs\":\"Segment1 Segment2\",\"FareBasis\":{\"FareBasisCode\":{\"Refs\":\"FG_1 FG_1\",\"Code\":\"ACUD0ZBJ ACUD0ZBJ\"},\"RBD\":\"A A\",\"CabinType\":\"Y Y\",\"SeatLeft\":\"9 1\"}}]},{\"OfferItemID\":\"OFFERITEMID2\",\"Refundable\":\"0\",\"PassengerType\":\"ADT\",\"PassengerQuantity\":2,\"TotalPriceDetail\":{\"TotalAmount\":{\"BookingCurrencyPrice\":232.35,\"EquivCurrencyPrice\":232.35}},\"Service\":[{\"ServiceID\":\"SV2\",\"PassengerRefs\":\"ADT1,ADT2\",\"FlightRefs\":\"Flight2\"}],\"FareDetail\":{\"PassengerRefs\":\"ADT1,ADT2\",\"Price\":{\"TotalAmount\":{\"BookingCurrencyPrice\":232.35,\"EquivCurrencyPrice\":232.35},\"BaseAmount\":{\"BookingCurrencyPrice\":195,\"EquivCurrencyPrice\":195},\"TaxAmount\":{\"BookingCurrencyPrice\":37.35,\"EquivCurrencyPrice\":37.35},\"Commission\":{\"AgencyCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"AgencyYqCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"BookingFee\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"PortalCharges\":{\"Markup\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Surcharge\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Discount\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"Taxes\":[{\"TaxCode\":\"RC\",\"BookingCurrencyPrice\":15.23,\"EquivCurrencyPrice\":15.23},{\"TaxCode\":\"SQ\",\"BookingCurrencyPrice\":15,\"EquivCurrencyPrice\":15},{\"TaxCode\":\"CA\",\"BookingCurrencyPrice\":7.12,\"EquivCurrencyPrice\":7.12}]}},\"FareComponent\":[{\"PriceClassRef\":\"PCR_1\",\"SegmentRefs\":\"Segment3 Segment4\",\"FareBasis\":{\"FareBasisCode\":{\"Refs\":\"FG_1 FG_1\",\"Code\":\"ACUD0ZBJ ACUD0ZBJ\"},\"RBD\":\"A A\",\"CabinType\":\"Y Y\",\"SeatLeft\":\"9 1\"}}]}],\"BaggageAllowance\":[{\"SegmentRefs\":\"Segment1 Segment2\",\"PassengerRefs\":\"ADT1,ADT2\",\"BaggageAllowanceRef\":\"Bag1\"}],\"SplitPaymentInfo\":[{\"AirItineraryId\":\"137211721101678878414495216684\",\"MultipleFop\":\"N\",\"MaxCardsPerPax\":0,\"MaxCardsPerPaxInMFOP\":0}],\"BookingToEquivExRate\":1,\"FopRef\":\"FOP_429_0_1172_0_ALL_PUB\"}],\"DataLists\":{\"PassengerList\":{\"Passengers\":[{\"attributes\":{\"PassengerID\":\"ADT1\"},\"PassengerID\":\"ADT1\",\"PTC\":\"ADT\",\"BirthDate\":\"1996-03-15\",\"NameTitle\":\"Mr\",\"FirstName\":\"LEBRON\",\"MiddleName\":\"\",\"LastName\":\"JAMES\",\"Gender\":\"Male\",\"TravelDocument\":{\"DocumentNumber\":\"\",\"ExpiryDate\":\"2001-01-01\",\"IssuingCountry\":\"\",\"DocumentType\":\"P\"},\"Preference\":{\"WheelChairPreference\":{\"Reason\":\"\"},\"SeatPreference\":\"any\"},\"LoyaltyProgramAccount\":[],\"ContactInfoRef\":\"CTC1\"},{\"attributes\":{\"PassengerID\":\"ADT2\"},\"PassengerID\":\"ADT2\",\"PTC\":\"ADT\",\"BirthDate\":\"1988-07-12\",\"NameTitle\":\"Ms\",\"FirstName\":\"Serena\",\"MiddleName\":\"\",\"LastName\":\"Williams\",\"Gender\":\"Female\",\"TravelDocument\":{\"DocumentNumber\":\"\",\"ExpiryDate\":\"2028-01-01\",\"IssuingCountry\":\"\",\"DocumentType\":\"P\"},\"Preference\":{\"WheelChairPreference\":{\"Reason\":\"\"},\"SeatPreference\":\"any\"},\"LoyaltyProgramAccount\":[],\"ContactInfoRef\":\"CTC2\"}]},\"DisclosureList\":{\"Disclosures\":[]},\"contactEmail\":[\"kathir@gmail.com\"],\"contactNumber\":[\"9854785465\"],\"ContactAddress\":[\"testing address1\"],\"FareList\":{\"FareGroup\":[{\"FareGroupRef\":\"FG_1\",\"FareCode\":\"70J\",\"FareBasisCode\":\"ACUD0ZBJ\"}]},\"FlightSegmentList\":{\"FlightSegment\":[{\"SegmentKey\":\"Segment1\",\"Departure\":{\"AirportCode\":\"YKF\",\"Date\":\"2023-05-19\",\"Time\":\"16:25:00\",\"AirportName\":\"Waterloo Airport\",\"Terminal\":{\"Name\":\"\"}},\"Arrival\":{\"AirportCode\":\"YYC\",\"Date\":\"2023-05-19\",\"Time\":\"18:30:00\",\"AirportName\":\"Calgary International Airport\",\"Terminal\":{\"Name\":\"\"}},\"MarketingCarrier\":{\"AirlineID\":\"WS\",\"Name\":\"Westjet\",\"FlightNumber\":\"557\"},\"OperatingCarrier\":{\"AirlineID\":\"WS\",\"Name\":\"Westjet\",\"FlightNumber\":\"557\"},\"Equipment\":{\"AircraftCode\":\"73W\",\"Name\":\"Boeing 737-700 (winglets) pax\"},\"Code\":{\"MarriageGroup\":\"\"},\"FlightDetail\":{\"FlightDuration\":{\"Value\":\"4 Hrs 5 Min\"},\"Stops\":{\"Value\":0},\"InterMediate\":[],\"AirMilesFlown\":\"1646\"},\"BrandId\":\"BASIC\"},{\"SegmentKey\":\"Segment2\",\"Departure\":{\"AirportCode\":\"YYC\",\"Date\":\"2023-05-19\",\"Time\":\"20:00:00\",\"AirportName\":\"Calgary International Airport\",\"Terminal\":{\"Name\":\"\"}},\"Arrival\":{\"AirportCode\":\"YVR\",\"Date\":\"2023-05-19\",\"Time\":\"20:32:00\",\"AirportName\":\"Vancouver International Airport\",\"Terminal\":{\"Name\":\"M\"}},\"MarketingCarrier\":{\"AirlineID\":\"WS\",\"Name\":\"Westjet\",\"FlightNumber\":\"66\"},\"OperatingCarrier\":{\"AirlineID\":\"WS\",\"Name\":\"Westjet\",\"FlightNumber\":\"66\"},\"Equipment\":{\"AircraftCode\":\"7M8\",\"Name\":\"7M8\"},\"Code\":{\"MarriageGroup\":\"\"},\"FlightDetail\":{\"FlightDuration\":{\"Value\":\"1 Hrs 32 Min\"},\"Stops\":{\"Value\":0},\"InterMediate\":[],\"AirMilesFlown\":\"428\"},\"BrandId\":\"BASIC\"},{\"SegmentKey\":\"Segment3\",\"Departure\":{\"AirportCode\":\"YVR\",\"Date\":\"2023-05-26\",\"Time\":\"06:00:00\",\"AirportName\":\"Vancouver International Airport\",\"Terminal\":{\"Name\":\"M\"}},\"Arrival\":{\"AirportCode\":\"YYC\",\"Date\":\"2023-05-26\",\"Time\":\"08:30:00\",\"AirportName\":\"Calgary International Airport\",\"Terminal\":{\"Name\":\"\"}},\"MarketingCarrier\":{\"AirlineID\":\"WS\",\"Name\":\"Westjet\",\"FlightNumber\":\"119\"},\"OperatingCarrier\":{\"AirlineID\":\"WS\",\"Name\":\"Westjet\",\"FlightNumber\":\"119\"},\"Equipment\":{\"AircraftCode\":\"7M8\",\"Name\":\"7M8\"},\"Code\":{\"MarriageGroup\":\"\"},\"FlightDetail\":{\"FlightDuration\":{\"Value\":\"1 Hrs 30 Min\"},\"Stops\":{\"Value\":0},\"InterMediate\":[],\"AirMilesFlown\":\"428\"},\"BrandId\":\"BASIC\"},{\"SegmentKey\":\"Segment4\",\"Departure\":{\"AirportCode\":\"YYC\",\"Date\":\"2023-05-26\",\"Time\":\"10:00:00\",\"AirportName\":\"Calgary International Airport\",\"Terminal\":{\"Name\":\"\"}},\"Arrival\":{\"AirportCode\":\"YKF\",\"Date\":\"2023-05-26\",\"Time\":\"15:45:00\",\"AirportName\":\"Waterloo Airport\",\"Terminal\":{\"Name\":\"\"}},\"MarketingCarrier\":{\"AirlineID\":\"WS\",\"Name\":\"Westjet\",\"FlightNumber\":\"558\"},\"OperatingCarrier\":{\"AirlineID\":\"WS\",\"Name\":\"Westjet\",\"FlightNumber\":\"558\"},\"Equipment\":{\"AircraftCode\":\"73W\",\"Name\":\"Boeing 737-700 (winglets) pax\"},\"Code\":{\"MarriageGroup\":\"\"},\"FlightDetail\":{\"FlightDuration\":{\"Value\":\"4 Hrs 45 Min\"},\"Stops\":{\"Value\":0},\"InterMediate\":[],\"AirMilesFlown\":\"1646\"},\"BrandId\":\"BASIC\"}]},\"FlightList\":{\"Flight\":[{\"FlightKey\":\"Flight1\",\"Journey\":{\"Time\":\"7 H 7 M\",\"Stops\":1},\"SegmentReferences\":\"Segment1 Segment2\"},{\"FlightKey\":\"Flight2\",\"Journey\":{\"Time\":\"6 H 45 M\",\"Stops\":1},\"SegmentReferences\":\"Segment3 Segment4\"}]},\"OriginDestinationList\":{\"OriginDestination\":[{\"OriginDestinationKey\":\"OD1\",\"DepartureCode\":\"YKF\",\"ArrivalCode\":\"YVR\",\"FlightReferences\":\"Flight1\"},{\"OriginDestinationKey\":\"OD2\",\"DepartureCode\":\"YVR\",\"ArrivalCode\":\"YKF\",\"FlightReferences\":\"Flight2\"}]},\"PriceClassList\":{\"PriceClass\":[{\"PriceClassID\":\"PCR_1\",\"Name\":\"Basic\",\"Code\":\"Basic\",\"Descriptions\":{\"Description\":[]}}]},\"BaggageAllowanceList\":{\"BaggageAllowance\":[{\"BaggageAllowanceID\":\"Bag1\",\"BaggageCategory\":\"Checked\",\"AllowanceDescription\":{\"ApplicableParty\":\"Traveler\",\"Description\":\"CHECKED ALLOWANCE\"},\"PieceAllowance\":{\"ApplicableParty\":\"Traveler\",\"TotalQuantity\":\"0\",\"Unit\":\"kg\"}}]},\"FopList\":[{\"CC\":{\"Allowed\":\"Y\",\"Types\":{\"AX\":{\"F\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"P\":0,\"Charges\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"MC\":{\"F\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"P\":0,\"Charges\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"VI\":{\"F\":{\"BookingCurrencyPrice\":\"0\",\"EquivCurrencyPrice\":\"0\"},\"P\":0,\"Charges\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}}}},\"DC\":{\"Allowed\":\"Y\",\"Types\":{\"MC\":{\"F\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"P\":0,\"Charges\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"VI\":{\"F\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"P\":0,\"Charges\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"RU\":{\"F\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"P\":0,\"Charges\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}}}},\"CASH\":{\"Allowed\":\"N\",\"Types\":{}},\"CHEQUE\":{\"Allowed\":\"Y\",\"Types\":{}},\"ACH\":{\"Allowed\":\"Y\",\"Types\":{}},\"PG\":{\"Allowed\":\"N\",\"Types\":{}},\"FopKey\":\"FOP_429_0_1172_0_ALL_PUB\"}]},\"MetaData\":{}}";
        
        // Setup flow state
        SupplyPnrRequestDTO requestDTO = SupplyPnrRequestDTO.newBuilder()
                .setSupplierPnr("OUZMYO")
                .setEnableTrace(true)
                .build();

        CMSMapHolder cmsMapHolder = new CMSMapHolder("TEST_CMS", new HashMap<>());

        flowState = new FlowState.Builder(System.currentTimeMillis())
                ;
        flowState.addValue(FlowStateKey.REQUEST, requestDTO);
        flowState.addValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE, sampleResponse);
        flowState.addValue(FlowStateKey.CMS_MAP, cmsMapHolder);
        
        when(jaxbHandlerService.marshall(any())).thenReturn("");
    }

    @Test
    public void testPnrRetrieveResponseAdapter() throws Exception {
        // Execute the adapter
        FlowState resultState = adapter.run(flowState.build());
        
        // Get the converted response
        SupplyBookingResponseDTO response = resultState.getValue(FlowStateKey.RESPONSE);

        try {
            System.out.println(JsonFormat.printer().omittingInsignificantWhitespace().print(response));
        } catch (InvalidProtocolBufferException e) {
           // return ExceptionUtils.getStackTrace(e);
        }
        
        // Verify basic booking information
        assertNotNull(response);
        assertEquals("60ZPNZTI", response.getBookingInfo().getFrInfo().getPnrGrpdFrInfoOrThrow(0).getSPnr());
        assertEquals("OUZMYO", response.getBookingInfo().getFrInfo().getPnrGrpdFrInfoOrThrow(0).getAPnr());
        
        // Verify passenger information
        assertTrue(response.getBookingInfo().getFrInfo().getPnrGrpdFrInfoOrThrow(0).getPaxFaresMap().containsKey("ADT"));
        assertEquals(2, response.getBookingInfo().getFrInfo().getPnrGrpdFrInfoOrThrow(0).getPaxFaresMap().get("ADT").getNoOfPax());
        
        // Verify flight segments
        assertEquals(2, response.getBookingInfo().getJourneysCount());
        
        // Verify first segment details
        SupplyBookingJourneyDTO firstJourney = response.getBookingInfo().getJourneys(0);
        assertEquals("2023-05-19T16:25:00", firstJourney.getDepDate());
        assertEquals("2023-05-19T18:30:00", firstJourney.getArrDate());
        
        // Verify contact information
        assertEquals("kathir@gmail.com", response.getContactInfo().getEmailId());
        assertEquals("9854785465", response.getContactInfo().getMobileNumber());
        
        // Verify pricing
        SupplyFareDetailDTO adtFare = response.getBookingInfo().getFrInfo().getPnrGrpdFrInfoOrThrow(0).getPaxFaresMap().get("ADT");
        assertEquals(232.35, adtFare.getTot(), 0.01);
        
        // Verify metadata
        assertEquals("CAD", response.getMetaData().getCurrency());
        assertNotNull(response.getMetaData().getSupplierLatency());
    }

    @Test
    public void testMiscellaneousInfo() throws Exception {
        FlowState resultState = adapter.run(flowState.build());
        SupplyBookingResponseDTO response = resultState.getValue(FlowStateKey.RESPONSE);
        
        // Verify misc info
        assertNotNull(response.getMiscData());
        assertEquals("Westjet", response.getMiscData().getIssuingAgent());
    }
}