package com.mmt.flights.common.util;


import com.mmt.flights.common.constants.CommonConstants;
import com.mmt.flights.odc.common.ErrorDetails;
import com.mmt.flights.odc.search.SimpleFlight;
import com.mmt.flights.odc.search.SimpleJourney;
import com.mmt.flights.odc.search.SimpleTechnicalStop;
import com.mmt.flights.postsales.error.PSErrorEnum;
import com.mmt.flights.supply.book.v4.response.SupplyBookingResponseDTO;
import com.mmt.flights.supply.book.v4.response.SupplyBookingResponseMetaDataDTO;
import com.mmt.flights.supply.book.v4.response.SupplyFlightDTO;
import com.mmt.flights.supply.book.v4.response.SupplyTechnicalStopDTO;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyPnrCancelResponseDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyPnrCancelResponseMetaDataDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyValidateCancelResponseDTO;
import com.mmt.flights.supply.common.SupplyErrorDetailDTO;
import com.mmt.flights.supply.common.enums.SupplyStatus;
import com.mmt.flights.supply.pnr.v4.request.SupplyBookingInfoDTO;
import com.mmt.flights.supply.pnr.v4.request.SupplyCurrentFareRequestDTO;
import com.mmt.flights.supply.pnr.v4.request.SupplyPaxInfo;
import com.mmt.flights.supply.pnr.v4.response.SupplySplitPnrResponseDTO;
import com.mmt.flights.util.DateUtil;
import org.springframework.http.HttpStatus;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdapterUtil {

    public static ErrorDetails getErrorDetails(PSErrorEnum errorCode, String errorDetail) {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setErrorCode(errorCode.getCode());
        errorDetails.setErrorMessage(errorCode.getMessage());
        errorDetails.setErrorDescription(errorDetail);
        errorDetails.setServiceName("VOYZANT-PNR");
        return errorDetails;
    }

    public static SupplyErrorDetailDTO getSupplyErrorDetail(PSErrorEnum errorCode, String errorDesc, SupplyErrorDetailDTO downStreamError) {
        SupplyErrorDetailDTO.Builder error = SupplyErrorDetailDTO.newBuilder();
        error.setEm(errorCode.getMessage());
        error.setEc(errorCode.getCode());
        error.setStatusCode(errorCode.getHttpStatus().value() + "");
        error.setSn(CommonConstants.SERVICE_NAME);
        if (errorDesc != null) error.setEd(errorDesc);
        if (downStreamError != null) {
            error.addDownStreamErrors(downStreamError);
        }
        return error.build();
    }

    public static String getMetricServiceName(String METRIC_SERVICE_NAME_PREFIX, PSErrorEnum errorEnum) {
        return METRIC_SERVICE_NAME_PREFIX + "_" + errorEnum.getHttpStatus().value() + "_" + errorEnum.getCode();
    }

    public static SupplyBookingResponseDTO getErroneousResponse(HttpStatus status, String errorCode,
                                                                String errorMessage, String errorDescription) {
        String statusCode = String.valueOf(status.value());
        SupplyErrorDetailDTO ed = getErrorDetail(statusCode, errorCode, errorMessage, errorDescription);
        SupplyBookingResponseMetaDataDTO.Builder meta = SupplyBookingResponseMetaDataDTO.newBuilder();
        meta.setServiceName(CommonConstants.SERVICE_NAME);
        meta.setIpAddress(IPAddressUtil.getIPAddress());
        SupplyBookingResponseDTO response = SupplyBookingResponseDTO.newBuilder().setStatus(SupplyStatus.FAILURE)
                .addErr(ed).setMetaData(meta.build()).build();
        return response;
    }

    public static SupplySplitPnrResponseDTO getErroneousSplitResponse(HttpStatus status, String errorCode,
                                                                      String errorMessage, String errorDescription) {
        SupplySplitPnrResponseDTO response = SupplySplitPnrResponseDTO.newBuilder().setStatus(SupplyStatus.FAILURE)
                .addErr(getErrorDetail(String.valueOf(status), errorCode, errorMessage, errorDescription)).build();
        return response;
    }

    public static SupplyPnrCancelResponseDTO getErroneousResponse_CancelPnr(HttpStatus status, String errorCode,
                                                                            String errorMessage, String errorDescription) {
        String statusCode = String.valueOf(status.value());
        SupplyErrorDetailDTO ed = getErrorDetail(statusCode, errorCode, errorMessage, errorDescription);
        SupplyPnrCancelResponseMetaDataDTO.Builder meta = SupplyPnrCancelResponseMetaDataDTO.newBuilder();
        meta.setServiceName(CommonConstants.SERVICE_NAME);
        meta.setSupplierName(CommonConstants.SUPPLIER_NAME);
        meta.setIpAddress(IPAddressUtil.getIPAddress());
        SupplyPnrCancelResponseDTO response = SupplyPnrCancelResponseDTO.newBuilder().setCancellationStatus(SupplyStatus.FAILURE)
                .addErr(ed)
                .setMeta(meta.build())
                .build();
        return response;
    }

    public static SupplyErrorDetailDTO getErrorDetail(String statusCode, String errorCode,
                                                      String errorMessage, String errorDescription) {
        SupplyErrorDetailDTO.Builder bd = SupplyErrorDetailDTO.newBuilder();
        if (statusCode != null) {
            bd.setStatusCode(statusCode);
        }
        if (errorCode != null) {
            bd.setEc(errorCode);
        }
        if (errorMessage != null) {
            bd.setEm(errorMessage);
        }
        if (errorDescription != null) {
            bd.setEd(errorDescription);
        }
        bd.setSn(CommonConstants.SERVICE_NAME);
        return bd.build();
    }

    public static SupplyErrorDetailDTO getErrorDetail(String statusCode, String errorCode, String errorMessage,
                                                      String errorDescription, String serviceName) {
        SupplyErrorDetailDTO.Builder bd = SupplyErrorDetailDTO.newBuilder();
        if (statusCode != null) {
            bd.setStatusCode(statusCode);
        }
        if (errorCode != null) {
            bd.setEc(errorCode);
        }
        if (errorMessage != null) {
            bd.setEm(errorMessage);
        }
        if (errorDescription != null) {
            bd.setEd(errorDescription);
        }
        if (serviceName != null) {
            bd.setSn(serviceName);
        }
        return bd.build();
    }


    public static SupplyBookingInfoDTO.SupplyPnrInfo getSupplyInfo(SupplyCurrentFareRequestDTO request) {
        Map<Integer, SupplyBookingInfoDTO.SupplyPnrInfo> map = request.getRequestCore().getBookingInfo().getPnrGrpdBookingInfoMap();
        SupplyBookingInfoDTO.SupplyPnrInfo pnrInfo = map.get(getPnrGroupNo(request));
        return pnrInfo;
    }

    public static Integer getPnrGroupNo(SupplyCurrentFareRequestDTO request) {
        Map<Integer, SupplyBookingInfoDTO.SupplyPnrInfo> map = request.getRequestCore().getBookingInfo().getPnrGrpdBookingInfoMap();
        return map.keySet().iterator().next();
    }


    public static SupplyValidateCancelResponseDTO getErroneousResponseValidateCancel(HttpStatus status, String errorCode,
                                                                                     String errorMessage, String errorDescription) {
        String statusCode = String.valueOf(status.value());
        SupplyErrorDetailDTO ed = getErrorDetail(statusCode, errorCode, errorMessage, errorDescription);
        SupplyValidateCancelResponseDTO response = SupplyValidateCancelResponseDTO.newBuilder().setStatus(SupplyStatus.FAILURE)
                .addErr(ed)
                .build();
        return response;
    }

    public static String generateTransactionId(String pnr) {
        return getRandomUUID() + "_" + pnr;
    }

    public static String getRandomUUID() {
        Base64.Encoder encoder = Base64.getUrlEncoder();
        UUID uuid = UUID.randomUUID();
        // Create byte[] for base64 from uuid
        byte[] src = ByteBuffer.wrap(new byte[16])
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
        // Encode to Base64 and remove trailing ==
        return encoder.encodeToString(src).substring(0, 22);
    }

    public static String getJourneyKey(List<SupplyFlightDTO> flights) {
        StringBuilder keyBuilder = new StringBuilder();
        for (int i = 0; i < flights.size(); i++) {
            SupplyFlightDTO flight = flights.get(i);
            if (i > 0) {
                keyBuilder.append('|');
            }
            keyBuilder.append(flight.getDepInfo().getArpCd()).append("$");
            List<SupplyTechnicalStopDTO> technicalStopovers = flight.getTchStpList();
            if (technicalStopovers != null) {
                for (SupplyTechnicalStopDTO technicalStopover : technicalStopovers) {
                    keyBuilder.append(technicalStopover.getLocInfo().getArpCd()).append("$");
                }
            }
            keyBuilder.append(flight.getArrInfo().getArpCd()).append("$");
            keyBuilder.append(flight.getDepTime()).append("$");
            keyBuilder.append(flight.getMrkAl() + "-" + flight.getFltNo());
        }
        return keyBuilder.toString();
    }

    public static String getAerospikeKeyForCancellation(SupplyPnrCancelRequestDTO requestDTO){
        String key = CommonConstants.CANCEL_CACHE_KEY + requestDTO.getRequestCore().getSupplierPnr();
        StringBuilder name = new StringBuilder();
        if(!requestDTO.getRequestCore().getPaxInfoList().isEmpty()){
            for(SupplyPaxInfo paxInfo : requestDTO.getRequestCore().getPaxInfoList()){
                name.append(paxInfo.getFname()).append(paxInfo.getLname());
            }
        }
        key = key + name;
        return key;
    }

    public static String getAerospikeKeyForVoid(SupplyPnrCancelRequestDTO requestDTO){
        String key = CommonConstants.VOID_CACHE_KEY + requestDTO.getRequestCore().getSupplierPnr();
        StringBuilder name = new StringBuilder();
        if(!requestDTO.getRequestCore().getPaxInfoList().isEmpty()){
            for(SupplyPaxInfo paxInfo : requestDTO.getRequestCore().getPaxInfoList()){
                name.append(paxInfo.getFname()).append(paxInfo.getLname());
            }
        }
        key = key + name;
        return key;
    }

}
