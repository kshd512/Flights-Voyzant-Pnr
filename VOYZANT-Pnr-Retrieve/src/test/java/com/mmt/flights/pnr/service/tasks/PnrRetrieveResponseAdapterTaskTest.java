package com.mmt.flights.pnr.service.tasks;

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

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PnrRetrieveResponseAdapterTaskTest {

    @InjectMocks
    private PnrRetrieveResponseAdapterTask adapter;

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
        sampleResponse = "{\"Document\":{\"Name\":\"API GATEWAY\",\"ReferenceVersion\":\"1.2\"},\"Party\":{\"Sender\":{\"TravelAgencySender\":{\"Name\":\"MY FLIGHT SUPPORTS LLC\",\"IATA_Number\":\"\",\"AgencyID\":\"MY FLIGHT SUPPORTS LLC\",\"Contacts\":{\"Contact\":[{\"EmailContact\":\"support@myflightsupports.com\"}]}}}},\"ShoppingResponseId\":\"1721307706375684774\",\"Success\":{},\"Order\":[{\"OrderID\":\"9D49X757\",\"GdsBookingReference\":\"OQBYOX\",\"OrderStatus\":\"SUCCESS\",\"PnrStatus\":\"\",\"ScheduleChangeIndicator\":false,\"NeedToTicket\":\"N\",\"OptionalServiceStatus\":\"SUCCESS\",\"OfferID\":\"115298911721307713920557565\",\"Owner\":\"WY\",\"OwnerName\":\"Oman Air\",\"IsBrandedFare\":\"N\",\"BrandedFareOptions\":[],\"InstantTicket\":\"N\",\"CabinOptions\":[],\"IsAdditionalCabinType\":\"N\",\"Eticket\":\"true\",\"TimeLimits\":{\"OfferExpirationDateTime\":\"2024-07-18T15:15:01\",\"PaymentExpirationDateTime\":\"2024-07-19 04:00:00\"},\"BookingCurrencyCode\":\"USD\",\"EquivCurrencyCode\":\"USD\",\"HstPercentage\":\"\",\"RewardSettings\":{\"RewardAvailable\":\"N\",\"PointTypes\":[],\"PointValues\":{}},\"BookingFeeInfo\":{\"FeeType\":\"\",\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"TotalPrice\":{\"BookingCurrencyPrice\":1431.68,\"EquivCurrencyPrice\":1431.68},\"BasePrice\":{\"BookingCurrencyPrice\":785,\"EquivCurrencyPrice\":785},\"TaxPrice\":{\"BookingCurrencyPrice\":646.68,\"EquivCurrencyPrice\":646.68},\"Commission\":{\"AgencyCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"AgencyYqCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"PortalCharges\":{\"Markup\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Surcharge\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Discount\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"AgentMarkupInfo\":{\"OnflyMarkup\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"OnflyDiscount\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"OnflyHst\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"PromoDiscount\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0,\"PromoCode\":\"\"}},\"Penalty\":{\"ChangeFee\":{\"Before\":{\"BookingCurrencyPrice\":72,\"EquivCurrencyPrice\":72},\"After\":{\"BookingCurrencyPrice\":72,\"EquivCurrencyPrice\":72}},\"CancelationFee\":{\"Before\":{\"BookingCurrencyPrice\":108,\"EquivCurrencyPrice\":108},\"After\":{\"BookingCurrencyPrice\":108,\"EquivCurrencyPrice\":108}}},\"PaxSeatInfo\":[{\"SeatNumber\":\"10A\",\"SegmentNumber\":1,\"Origin\":\"MAA\",\"Destination\":\"MCT\",\"PaxRef\":\"ADT1\",\"Status\":\"FAILED\"},{\"SeatNumber\":\"10B\",\"SegmentNumber\":1,\"Origin\":\"MAA\",\"Destination\":\"MCT\",\"PaxRef\":\"ADT2\",\"Status\":\"FAILED\"},{\"SeatNumber\":\"10C\",\"SegmentNumber\":1,\"Origin\":\"MAA\",\"Destination\":\"MCT\",\"PaxRef\":\"CHD1\",\"Status\":\"SUCCESS\"}],\"OfferItem\":[{\"OfferItemID\":\"OFFERITEMID1\",\"Refundable\":true,\"PassengerType\":\"ADT\",\"PassengerQuantity\":2,\"TotalPriceDetail\":{\"TotalAmount\":{\"BookingCurrencyPrice\":922.72,\"EquivCurrencyPrice\":922.72}},\"Service\":[{\"ServiceID\":\"SV1\",\"PassengerRefs\":\"ADT1 ADT2\",\"FlightRefs\":\"Flight1\"},{\"ServiceID\":\"SV2\",\"PassengerRefs\":\"ADT1 ADT2\",\"FlightRefs\":\"Flight2\"}],\"FareDetail\":{\"PassengerRefs\":\"ADT1 ADT2\",\"Price\":{\"TotalAmount\":{\"BookingCurrencyPrice\":922.72,\"EquivCurrencyPrice\":922.72},\"BaseAmount\":{\"BookingCurrencyPrice\":504,\"EquivCurrencyPrice\":504},\"TaxAmount\":{\"BookingCurrencyPrice\":418.72,\"EquivCurrencyPrice\":418.72},\"Commission\":{\"AgencyCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"AgencyYqCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"BookingFee\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"PortalCharges\":{\"Markup\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Surcharge\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Discount\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"Taxes\":[{\"TaxCode\":\"YQ\",\"BookingCurrencyPrice\":170.4,\"EquivCurrencyPrice\":170.4},{\"TaxCode\":\"YR\",\"BookingCurrencyPrice\":12,\"EquivCurrencyPrice\":12},{\"TaxCode\":\"K3\",\"BookingCurrencyPrice\":34.4,\"EquivCurrencyPrice\":34.4},{\"TaxCode\":\"IN\",\"BookingCurrencyPrice\":17.4,\"EquivCurrencyPrice\":17.4},{\"TaxCode\":\"I2\",\"BookingCurrencyPrice\":10.4,\"EquivCurrencyPrice\":10.4},{\"TaxCode\":\"OM\",\"BookingCurrencyPrice\":52,\"EquivCurrencyPrice\":52},{\"TaxCode\":\"AE\",\"BookingCurrencyPrice\":40.8,\"EquivCurrencyPrice\":40.8},{\"TaxCode\":\"TP\",\"BookingCurrencyPrice\":2.8,\"EquivCurrencyPrice\":2.8},{\"TaxCode\":\"ZR\",\"BookingCurrencyPrice\":5.6,\"EquivCurrencyPrice\":5.6},{\"TaxCode\":\"F6\",\"BookingCurrencyPrice\":21.8,\"EquivCurrencyPrice\":21.8},{\"TaxCode\":\"P2\",\"BookingCurrencyPrice\":28.32,\"EquivCurrencyPrice\":28.32},{\"TaxCode\":\"S6\",\"BookingCurrencyPrice\":22.8,\"EquivCurrencyPrice\":22.8}]}},\"FareComponent\":[{\"PriceClassRef\":\"PCR_1\",\"SegmentRefs\":\"Segment1 Segment2\",\"FareBasis\":{\"FareBasisCode\":{\"Refs\":\"FG_1 FG_1\",\"Code\":\"NELRIA NELRIA\"},\"RBD\":\"N N\",\"CabinType\":\"Y Y\",\"SeatLeft\":\"9 9\"}},{\"PriceClassRef\":\"PCR_1\",\"SegmentRefs\":\"Segment3 Segment4\",\"FareBasis\":{\"FareBasisCode\":{\"Refs\":\"FG_2 FG_2\",\"Code\":\"QECRIA QECRIA\"},\"RBD\":\"Q Q\",\"CabinType\":\"Y Y\",\"SeatLeft\":\"9 9\"}}]},{\"OfferItemID\":\"OFFERITEMID2\",\"Refundable\":true,\"PassengerType\":\"CHD\",\"PassengerQuantity\":1,\"TotalPriceDetail\":{\"TotalAmount\":{\"BookingCurrencyPrice\":405.76,\"EquivCurrencyPrice\":405.76}},\"Service\":[{\"ServiceID\":\"SV1\",\"PassengerRefs\":\"CHD1\",\"FlightRefs\":\"Flight1\"},{\"ServiceID\":\"SV2\",\"PassengerRefs\":\"CHD1\",\"FlightRefs\":\"Flight2\"}],\"FareDetail\":{\"PassengerRefs\":\"CHD1\",\"Price\":{\"TotalAmount\":{\"BookingCurrencyPrice\":405.76,\"EquivCurrencyPrice\":405.76},\"BaseAmount\":{\"BookingCurrencyPrice\":199,\"EquivCurrencyPrice\":199},\"TaxAmount\":{\"BookingCurrencyPrice\":206.76,\"EquivCurrencyPrice\":206.76},\"Commission\":{\"AgencyCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"AgencyYqCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"BookingFee\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"PortalCharges\":{\"Markup\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Surcharge\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Discount\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"Taxes\":[{\"TaxCode\":\"YQ\",\"BookingCurrencyPrice\":85.2,\"EquivCurrencyPrice\":85.2},{\"TaxCode\":\"YR\",\"BookingCurrencyPrice\":6,\"EquivCurrencyPrice\":6},{\"TaxCode\":\"K3\",\"BookingCurrencyPrice\":14.6,\"EquivCurrencyPrice\":14.6},{\"TaxCode\":\"IN\",\"BookingCurrencyPrice\":8.7,\"EquivCurrencyPrice\":8.7},{\"TaxCode\":\"I2\",\"BookingCurrencyPrice\":5.2,\"EquivCurrencyPrice\":5.2},{\"TaxCode\":\"OM\",\"BookingCurrencyPrice\":26,\"EquivCurrencyPrice\":26},{\"TaxCode\":\"AE\",\"BookingCurrencyPrice\":20.4,\"EquivCurrencyPrice\":20.4},{\"TaxCode\":\"TP\",\"BookingCurrencyPrice\":1.4,\"EquivCurrencyPrice\":1.4},{\"TaxCode\":\"ZR\",\"BookingCurrencyPrice\":2.8,\"EquivCurrencyPrice\":2.8},{\"TaxCode\":\"F6\",\"BookingCurrencyPrice\":10.9,\"EquivCurrencyPrice\":10.9},{\"TaxCode\":\"P2\",\"BookingCurrencyPrice\":14.16,\"EquivCurrencyPrice\":14.16},{\"TaxCode\":\"S6\",\"BookingCurrencyPrice\":11.4,\"EquivCurrencyPrice\":11.4}]}},\"FareComponent\":[{\"PriceClassRef\":\"PCR_1\",\"SegmentRefs\":\"Segment1 Segment2\",\"FareBasis\":{\"FareBasisCode\":{\"Refs\":\"FG_1 FG_1\",\"Code\":\"NELRIA NELRIA\"},\"RBD\":\"N N\",\"CabinType\":\"Y Y\",\"SeatLeft\":\"9 9\"}},{\"PriceClassRef\":\"PCR_1\",\"SegmentRefs\":\"Segment3 Segment4\",\"FareBasis\":{\"FareBasisCode\":{\"Refs\":\"FG_2 FG_2\",\"Code\":\"QECRIA QECRIA\"},\"RBD\":\"Q Q\",\"CabinType\":\"Y Y\",\"SeatLeft\":\"9 9\"}}]},{\"OfferItemID\":\"OFFERITEMID3\",\"Refundable\":true,\"PassengerType\":\"INF\",\"PassengerQuantity\":1,\"TotalPriceDetail\":{\"TotalAmount\":{\"BookingCurrencyPrice\":103.2,\"EquivCurrencyPrice\":103.2}},\"Service\":[{\"ServiceID\":\"SV1\",\"PassengerRefs\":\"INF1\",\"FlightRefs\":\"Flight1\"},{\"ServiceID\":\"SV2\",\"PassengerRefs\":\"INF1\",\"FlightRefs\":\"Flight2\"}],\"FareDetail\":{\"PassengerRefs\":\"INF1\",\"Price\":{\"TotalAmount\":{\"BookingCurrencyPrice\":103.2,\"EquivCurrencyPrice\":103.2},\"BaseAmount\":{\"BookingCurrencyPrice\":82,\"EquivCurrencyPrice\":82},\"TaxAmount\":{\"BookingCurrencyPrice\":21.2,\"EquivCurrencyPrice\":21.2},\"Commission\":{\"AgencyCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"AgencyYqCommission\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"BookingFee\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"PortalCharges\":{\"Markup\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Surcharge\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Discount\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"Taxes\":[{\"TaxCode\":\"YR\",\"BookingCurrencyPrice\":5.4,\"EquivCurrencyPrice\":5.4},{\"TaxCode\":\"K3\",\"BookingCurrencyPrice\":4.4,\"EquivCurrencyPrice\":4.4},{\"TaxCode\":\"S6\",\"BookingCurrencyPrice\":11.4,\"EquivCurrencyPrice\":11.4}]}},\"FareComponent\":[{\"PriceClassRef\":\"PCR_1\",\"SegmentRefs\":\"Segment1 Segment2\",\"FareBasis\":{\"FareBasisCode\":{\"Refs\":\"FG_1 FG_1\",\"Code\":\"NELRIA NELRIA\"},\"RBD\":\"N N\",\"CabinType\":\"Y Y\",\"SeatLeft\":\"9 9\"}},{\"PriceClassRef\":\"PCR_1\",\"SegmentRefs\":\"Segment3 Segment4\",\"FareBasis\":{\"FareBasisCode\":{\"Refs\":\"FG_2 FG_2\",\"Code\":\"QECRIA QECRIA\"},\"RBD\":\"Q Q\",\"CabinType\":\"Y Y\",\"SeatLeft\":\"9 9\"}}]},{\"OfferItemID\":\"SRV-OfferItem-1\",\"Status\":\"SUCCESS\",\"Price\":{\"Base\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Tax\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Total\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"ServiceDefinitionRef\":\"SRV-1\"},{\"OfferItemID\":\"SRV-OfferItem-2\",\"Status\":\"SUCCESS\",\"Price\":{\"Base\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Tax\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Total\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"ServiceDefinitionRef\":\"SRV-1\"},{\"OfferItemID\":\"SRV-OfferItem-3\",\"Status\":\"SUCCESS\",\"Price\":{\"Base\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Tax\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"Total\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"ServiceDefinitionRef\":\"SRV-1\"}],\"BaggageAllowance\":[{\"SegmentRefs\":\"Segment1 Segment2 Segment3 Segment4\",\"PassengerRefs\":\"T1 T2\",\"BaggageAllowanceRef\":\"Bag1\"},{\"SegmentRefs\":\"Segment1 Segment2 Segment3 Segment4\",\"PassengerRefs\":\"T3\",\"BaggageAllowanceRef\":\"Bag1\"},{\"SegmentRefs\":\"Segment1 Segment2 Segment3 Segment4\",\"PassengerRefs\":\"T4\",\"BaggageAllowanceRef\":\"Bag2\"}],\"SplitPaymentInfo\":[{\"AirItineraryId\":\"115298911721307713920557565\",\"MultipleFop\":\"N\",\"MaxCardsPerPax\":0,\"MaxCardsPerPaxInMFOP\":0}],\"BookingToEquivExRate\":1,\"FopRef\":\"FOP_4_0_989_0_ALL_PUB\"}],\"DataLists\":{\"PassengerList\":{\"Passengers\":[{\"PassengerID\":\"T1\",\"PTC\":\"ADT\",\"OnflyMarkup\":\"0.00\",\"OnflyDiscount\":\"0.00\",\"BirthDate\":\"1991-07-21\",\"NameTitle\":\"Mrs\",\"FirstName\":\"reena\",\"MiddleName\":\"\",\"LastName\":\"demo\",\"Gender\":\"Female\",\"ContactInfoRef\":\"CTC1\",\"attributes\":{\"PassengerID\":\"T1\"},\"PassengerRefID\":\"ADT1\"},{\"PassengerID\":\"T2\",\"PTC\":\"ADT\",\"OnflyMarkup\":\"0.00\",\"OnflyDiscount\":\"0.00\",\"BirthDate\":\"1991-07-21\",\"NameTitle\":\"Mrs\",\"FirstName\":\"TEST\",\"MiddleName\":\"\",\"LastName\":\"demo\",\"Gender\":\"Female\",\"ContactInfoRef\":\"CTC1\",\"attributes\":{\"PassengerID\":\"T2\"},\"PassengerRefID\":\"ADT2\"},{\"PassengerID\":\"T3\",\"PTC\":\"CHD\",\"OnflyMarkup\":\"0.00\",\"OnflyDiscount\":\"0.00\",\"BirthDate\":\"2019-08-21\",\"NameTitle\":\"Mstr\",\"FirstName\":\"vedik\",\"MiddleName\":\"\",\"LastName\":\"demo\",\"Gender\":\"Male\",\"ContactInfoRef\":\"CTC1\",\"attributes\":{\"PassengerID\":\"T3\"},\"PassengerRefID\":\"CHD1\"},{\"PassengerID\":\"T4\",\"PTC\":\"INF\",\"OnflyMarkup\":\"0.00\",\"OnflyDiscount\":\"0.00\",\"BirthDate\":\"2023-09-21\",\"NameTitle\":\"Mstr\",\"FirstName\":\"DEVA\",\"MiddleName\":\"\",\"LastName\":\"sankh\",\"Gender\":\"Male\",\"ContactInfoRef\":\"CTC1\",\"attributes\":{\"PassengerID\":\"T4\"},\"PassengerRefID\":\"INF1\"}]},\"DisclosureList\":{\"Disclosures\":[]},\"FareList\":{\"FareGroup\":[{\"FareGroupRef\":\"FG_1\",\"FareCode\":\"70J\",\"FareBasisCode\":\"NELRIA\"},{\"FareGroupRef\":\"FG_2\",\"FareCode\":\"70J\",\"FareBasisCode\":\"QECRIA\"}]},\"FlightSegmentList\":{\"FlightSegment\":[{\"SegmentKey\":\"Segment1\",\"Departure\":{\"AirportCode\":\"MAA\",\"Date\":\"2024-09-11\",\"Time\":\"15:30:00\",\"AirportName\":\"Chennai International Airport\",\"Terminal\":{\"Name\":\"2\"}},\"Arrival\":{\"AirportCode\":\"MCT\",\"Date\":\"2024-09-11\",\"Time\":\"17:50:00\",\"AirportName\":\"Muscat International Airport\",\"Terminal\":{\"Name\":\"\"}},\"MarketingCarrier\":{\"AirlineID\":\"WY\",\"Name\":\"Oman Air\",\"FlightNumber\":\"254\"},\"OperatingCarrier\":{\"AirlineID\":\"WY\",\"Name\":\"Oman Air\",\"FlightNumber\":\"254\"},\"Equipment\":{\"AircraftCode\":\"7M8\",\"Name\":\"Boeing 737 MAX 8\"},\"Code\":{\"MarriageGroup\":\"O\"},\"FlightDetail\":{\"FlightDuration\":{\"Value\":\"3 H 50 M\"},\"Stops\":{\"Value\":0},\"InterMediate\":[],\"AirMilesFlown\":1610},\"BrandId\":\"\"},{\"SegmentKey\":\"Segment2\",\"Departure\":{\"AirportCode\":\"MCT\",\"Date\":\"2024-09-11\",\"Time\":\"20:15:00\",\"AirportName\":\"Muscat International Airport\",\"Terminal\":{\"Name\":\"\"}},\"Arrival\":{\"AirportCode\":\"DXB\",\"Date\":\"2024-09-11\",\"Time\":\"21:25:00\",\"AirportName\":\"Dubai International Airport\",\"Terminal\":{\"Name\":\"1\"}},\"MarketingCarrier\":{\"AirlineID\":\"WY\",\"Name\":\"Oman Air\",\"FlightNumber\":\"611\"},\"OperatingCarrier\":{\"AirlineID\":\"WY\",\"Name\":\"Oman Air\",\"FlightNumber\":\"611\"},\"Equipment\":{\"AircraftCode\":\"7M8\",\"Name\":\"Boeing 737 MAX 8\"},\"Code\":{\"MarriageGroup\":\"I\"},\"FlightDetail\":{\"FlightDuration\":{\"Value\":\"1 H 10 M\"},\"Stops\":{\"Value\":0},\"InterMediate\":[],\"AirMilesFlown\":217},\"BrandId\":\"\"},{\"SegmentKey\":\"Segment3\",\"Departure\":{\"AirportCode\":\"DXB\",\"Date\":\"2024-09-30\",\"Time\":\"22:45:00\",\"AirportName\":\"Dubai International Airport\",\"Terminal\":{\"Name\":\"1\"}},\"Arrival\":{\"AirportCode\":\"MCT\",\"Date\":\"2024-09-30\",\"Time\":\"23:55:00\",\"AirportName\":\"Muscat International Airport\",\"Terminal\":{\"Name\":\"\"}},\"MarketingCarrier\":{\"AirlineID\":\"WY\",\"Name\":\"Oman Air\",\"FlightNumber\":\"612\"},\"OperatingCarrier\":{\"AirlineID\":\"WY\",\"Name\":\"Oman Air\",\"FlightNumber\":\"612\"},\"Equipment\":{\"AircraftCode\":\"7M8\",\"Name\":\"Boeing 737 MAX 8\"},\"Code\":{\"MarriageGroup\":\"O\"},\"FlightDetail\":{\"FlightDuration\":{\"Value\":\"1 H 10 M\"},\"Stops\":{\"Value\":0},\"InterMediate\":[],\"AirMilesFlown\":217},\"BrandId\":\"\"},{\"SegmentKey\":\"Segment4\",\"Departure\":{\"AirportCode\":\"MCT\",\"Date\":\"2024-10-01\",\"Time\":\"02:05:00\",\"AirportName\":\"Muscat International Airport\",\"Terminal\":{\"Name\":\"\"}},\"Arrival\":{\"AirportCode\":\"MAA\",\"Date\":\"2024-10-01\",\"Time\":\"07:25:00\",\"AirportName\":\"Chennai International Airport\",\"Terminal\":{\"Name\":\"2\"}},\"MarketingCarrier\":{\"AirlineID\":\"WY\",\"Name\":\"Oman Air\",\"FlightNumber\":\"251\"},\"OperatingCarrier\":{\"AirlineID\":\"WY\",\"Name\":\"Oman Air\",\"FlightNumber\":\"251\"},\"Equipment\":{\"AircraftCode\":\"7M8\",\"Name\":\"Boeing 737 MAX 8\"},\"Code\":{\"MarriageGroup\":\"I\"},\"FlightDetail\":{\"FlightDuration\":{\"Value\":\"3 H 50 M\"},\"Stops\":{\"Value\":0},\"InterMediate\":[],\"AirMilesFlown\":1610},\"BrandId\":\"\"}]},\"FlightList\":{\"Flight\":[{\"FlightKey\":\"Flight1\",\"Journey\":{\"Time\":\"7 H 25 M\",\"Stops\":1},\"SegmentReferences\":\"Segment1 Segment2\"},{\"FlightKey\":\"Flight2\",\"Journey\":{\"Time\":\"7 H 10 M\",\"Stops\":1},\"SegmentReferences\":\"Segment3 Segment4\"}]},\"OriginDestinationList\":{\"OriginDestination\":[{\"OriginDestinationKey\":\"OD1\",\"DepartureCode\":\"MAA\",\"ArrivalCode\":\"DXB\",\"FlightReferences\":\"Flight1\"},{\"OriginDestinationKey\":\"OD2\",\"DepartureCode\":\"DXB\",\"ArrivalCode\":\"MAA\",\"FlightReferences\":\"Flight2\"}]},\"PriceClassList\":{\"PriceClass\":[{\"PriceClassID\":\"PCR_1\",\"Name\":\"\",\"Code\":\"\",\"Descriptions\":{\"Description\":[]}}]},\"BaggageAllowanceList\":{\"BaggageAllowance\":[{\"BaggageAllowanceID\":\"Bag1\",\"BaggageCategory\":\"Checked\",\"AllowanceDescription\":{\"ApplicableParty\":\"Traveler\",\"Description\":\"CHECKED ALLOWANCE\"},\"PieceAllowance\":{\"ApplicableParty\":\"Traveler\",\"TotalQuantity\":\"30\",\"Unit\":\"Pieces\"}},{\"BaggageAllowanceID\":\"Bag2\",\"BaggageCategory\":\"Checked\",\"AllowanceDescription\":{\"ApplicableParty\":\"Traveler\",\"Description\":\"CHECKED ALLOWANCE\"},\"PieceAllowance\":{\"ApplicableParty\":\"Traveler\",\"TotalQuantity\":\"10\",\"Unit\":\"Pieces\"}}]},\"ServiceDefinitionList\":{\"ServiceDefinition\":[{\"ServiceDefinitionID\":\"SRV-1\",\"ServiceType\":\"MEAL\",\"ServiceCode\":\"YJ MEAL RQST_SPML\",\"ServiceName\":\"YJ MEAL RQST\"}]},\"FopList\":[{\"CC\":{\"Allowed\":\"Y\",\"Types\":{\"AX\":{\"F\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"P\":0,\"Charges\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"MC\":{\"F\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"P\":0,\"Charges\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}},\"VI\":{\"F\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0},\"P\":0,\"Charges\":{\"BookingCurrencyPrice\":0,\"EquivCurrencyPrice\":0}}}},\"DC\":{\"Allowed\":\"N\",\"Types\":{}},\"CASH\":{\"Allowed\":\"Y\",\"Types\":{}},\"CHEQUE\":{\"Allowed\":\"Y\",\"Types\":{}},\"ACH\":{\"Allowed\":\"N\",\"Types\":{}},\"PG\":{\"Allowed\":\"Y\",\"Types\":{}},\"FopKey\":\"FOP_4_0_989_0_ALL_PUB\"}]},\"TicketDocInfos\":{\"TicketDocInfo\":[{\"TicketDocument\":{\"TicketDocNbr\":\"2322472517170\",\"Type\":\"TKT\"},\"PassengerReference\":\"T2\",\"GdsBookingReference\":\"OQBYOX\"},{\"TicketDocument\":{\"TicketDocNbr\":\"2322472517171\",\"Type\":\"TKT\"},\"PassengerReference\":\"T1\",\"GdsBookingReference\":\"OQBYOX\"},{\"TicketDocument\":{\"TicketDocNbr\":\"2322472517172\",\"Type\":\"TKT\"},\"PassengerReference\":\"T3\",\"GdsBookingReference\":\"OQBYOX\"},{\"TicketDocument\":{\"TicketDocNbr\":\"2324559766050\",\"Type\":\"TKT\"},\"PassengerReference\":\"T4\",\"GdsBookingReference\":\"OQBYOX\"}]},\"MetaData\":{},\"threeDsResponse\":\"\"}";
        
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
        assertEquals("2023-05-19T20:32:00", firstJourney.getArrDate());
        
        // Verify contact information
        assertEquals("kathir@gmail.com", response.getContactInfo().getEmailId());
        assertEquals("9854785465", response.getContactInfo().getMobileNumber());
        
        // Verify pricing
        SupplyFareDetailDTO adtFare = response.getBookingInfo().getFrInfo().getPnrGrpdFrInfoOrThrow(0).getPaxFaresMap().get("ADT");
        assertEquals(460, adtFare.getTot(), 0.01);
        
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