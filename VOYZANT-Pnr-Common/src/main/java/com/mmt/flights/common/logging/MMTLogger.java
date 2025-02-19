package com.mmt.flights.common.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.logging.metric.MetricServices;
import com.mmt.flights.common.logging.metric.MetricType;
import com.mmt.flights.common.util.AdapterUtil;
import com.mmt.flights.flightsutil.JsonConvertor;
import com.mmt.flights.logger.FlightCustomLogger;
import com.mmt.flights.logger.FlightLogger;
import com.mmt.flights.logger.factory.FlightLoggerFactory;
import com.mmt.flights.logger.factory.JsonLoggerImpl;
import com.mmt.flights.metrics.entity.MetricCustomTags;
import com.mmt.flights.pii.PiiEncryptionAdapter;
import com.mmt.flights.pii.exception.PiiEncryptionException;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.mmt.flights.supply.pnr.v4.request.SupplyCurrentFareRequestDTO;
import com.mmt.flights.supply.pnr.v4.request.SupplyPnrRequestDTO;
import com.mmt.flights.supply.pnr.v4.request.SupplySplitPnrRequestDTO;
import com.mmt.flights.util.CancelPiiUtil;
import com.mmt.flights.util.UndoCheckinPiiUtil;
import com.mmt.flights.util.WebCheckinPiiUtil;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Serializer.Property;
import org.apache.axis2.databinding.ADBBean;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 *
 */
public class MMTLogger {

	private static FlightLogger flightsLogger = FlightLoggerFactory.getLogger(JsonLoggerImpl.V2);
	private static final String METRIC_COMPONENT_NAME = "Flights-VOYZANT-Pnr";

	private static final Logger conLogger = LoggerFactory.getLogger(MMTLogger.class);

	/**
	 * converts object to json string ignoring jsonproperty annotations
	 * 
	 * @param obj
	 *            to be converted to JSON
	 * @return String JSON serialized value of Object
	 */
	public static String convertToJson(Object obj) {
		try {
			return JsonConvertor.convertObjectToJson(obj);
		} catch (JsonProcessingException e) {
			return obj.toString();
		}
	}

	public static void logRequestResponse(String logKey, String request, String response, String step) {
		conLogger.debug("{}, Step: {}, Request: {}", logKey, step, request);
		conLogger.debug("{}, Step: {}, Response: {}", logKey, step, response);
	}

	public static String convertProtoToJson(Message m) {
		try {
            return JsonFormat.printer().includingDefaultValueFields().print(m);
        } catch (InvalidProtocolBufferException e) {
            MMTLogger.error("", "Error in convert proto obj to string", MMTLogger.class.getName(), e);
            return convertToJson(m);
        }
	}

	public static String convertProtoToJson(SupplyPnrCancelRequestDTO m){
		String crId = m.getRequestConfig().getCorrelationId();
		try {
			try {
				return PiiEncryptionAdapter.encodeProto(m, crId);
			} catch (PiiEncryptionException e) {
				MMTLogger.error(crId, "Error in convert proto obj to string", MMTLogger.class.getName(), e);

				return JsonFormat.printer().includingDefaultValueFields().print(m);
			}
		} catch (InvalidProtocolBufferException ex) {
			MMTLogger.error(crId, "Invalid proto", MMTLogger.class.getName(), ex);
			return "";
		}
	}
	
	public static String getXMLString(ADBBean axisObject,boolean indent) {

		String printString = "";
		if (null != axisObject) {
			try {
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				Configuration config = new Configuration();
				Processor processor = new Processor(config);
				Serializer serializer = processor.newSerializer();
				serializer.setOutputProperty(Property.METHOD, "xml");
				serializer.setOutputProperty(Property.INDENT, indent?"yes":"no");
				serializer.setOutputProperty(Property.OMIT_XML_DECLARATION, "yes");
				serializer.setOutputStream(outStream);
				XMLStreamWriter writer = serializer.getXMLStreamWriter();
				 writer.setNamespaceContext(new NamespaceContext(){

					@Override
					public String getNamespaceURI(String arg0) {
						return null;
					}

					@Override
					public String getPrefix(String arg0) {
						return null;
					}

					@Override
					public Iterator getPrefixes(String arg0) {
						return null;
					}
					 
				 });
				axisObject.serialize(new QName(axisObject.getClass().getName()), writer);
				writer.flush();
				writer.close();
				printString = outStream.toString();
			} catch (Exception e) {
				MMTLogger.error("", "Error while getting string from xml object.", MMTLogger.class.getName(), e);
			}
		}
		return printString;
	}
	public static String getXMLString(ADBBean axisObject){
	 return getXMLString(axisObject,true);
	}

	private static String fetchMetricType(MetricType... metricTypes) {
		if (null == metricTypes || metricTypes.length == 0) {
			return null;
		}
		return Arrays.stream(metricTypes).map(MetricType::name).collect(Collectors.joining(","));
	}

	private static String getExceptionName(Throwable e) {
		return null != e ? e.getClass().getSimpleName() : null;
	}

	private static String getStackTrace(Throwable e) {
		return null != e ? ExceptionUtils.getStackTrace(e) : null;
	}

	private static String getInternalServiceName(String serviceName, String errorCode, Integer httpStatus) {
		if (serviceName == null)
			return null;
		StringBuilder metricName = new StringBuilder();

		metricName.append(serviceName);
		if (httpStatus != null) {
			metricName.append("-");
			metricName.append(httpStatus);
		}
		if (errorCode != null) {
			metricName.append("-");
			metricName.append(errorCode);
		}
		return metricName.toString();
	}

	public static void info(LogParams logParams, MetricType... metricTypes) {
		FlightCustomLogger logger = flightsLogger.info();
		populateLogParams(logger, logParams, metricTypes);
		logger.log();
	}

	public static void info(String logKey, String logMessage, String className) {
		MMTLogger.info((new LogParams.LogParamsBuilder()).correlationId(logKey).className(className)
				.extraInfo(logMessage).build(), MetricType.LOG_FILE);
	}


	public static void debug(String logKey, String logMessage, String className) {
		MMTLogger.debug((new LogParams.LogParamsBuilder()).correlationId(logKey).className(className)
				.extraInfo(logMessage).build(), MetricType.LOG_FILE);
	}

	public static void warn(LogParams logParams, MetricType... metricTypes) {
		FlightCustomLogger logger = flightsLogger.warn();
		populateLogParams(logger, logParams, metricTypes);
		logger.log();
	}

	public static void warn(String logKey, String logMessage, String className) {
		MMTLogger.warn((new LogParams.LogParamsBuilder()).correlationId(logKey).className(className)
				.extraInfo(logMessage).build(), MetricType.LOG_FILE);
	}

	public static void debug(LogParams logParams, MetricType... metricTypes) {
		FlightCustomLogger logger = flightsLogger.debug();
		populateLogParams(logger, logParams, metricTypes);
		logger.log();
	}

	public static void debug(SupplyCurrentFareRequestDTO pnrRequest, String logMessage, String className) {
		MMTLogger.debug((new LogParams.LogParamsBuilder())
				.correlationId(AdapterUtil.getSupplyInfo(pnrRequest).getSupplierPnr())
				.itineraryId(pnrRequest.getRequestConfig().getItineraryId())
				.src(pnrRequest.getRequestConfig().getSource())
				.lob(pnrRequest.getRequestConfig().getLob())
				.lob(pnrRequest.getRequestConfig().getLob()).className(className).extraInfo(logMessage).build(),
				MetricType.LOG_FILE);

	}

	public static void debug(SupplySplitPnrRequestDTO request, String logMessage,
							 String className) {
		MMTLogger.debug((new LogParams.LogParamsBuilder())
						.correlationId(request.getRequestCore().getSupplierPnr())
						.className(className).extraInfo(logMessage).build(),
				MetricType.LOG_FILE);

	}

	public static void error(LogParams logParams, MetricType... metricTypes) {
		FlightCustomLogger logger = flightsLogger.error();
		populateLogParams(logger, logParams, metricTypes);
		Throwable e = logParams.getThrowable();
		logger.exception(getExceptionName(e)).stackTrace(getStackTrace(e), true).servNm(getInternalServiceName(
				logParams.getServiceName(), logParams.getErrorCode(), logParams.getHttpStatus()));
		logger.log();
	}

	public static void error(String logKey, String logMessage, String className, Throwable e) {
		MMTLogger.error((new LogParams.LogParamsBuilder()).correlationId(logKey).className(className)
				.extraInfo(logMessage).throwable(e).build(), MetricType.LOG_FILE);
	}

	public static void error(SupplyCurrentFareRequestDTO pnrRequest, String logMessage, String className, Throwable e) {
		MMTLogger.error(
				(new LogParams.LogParamsBuilder()).correlationId(AdapterUtil.getSupplyInfo(pnrRequest).getSupplierPnr())
						.src(pnrRequest.getRequestConfig().getSource())
						.supplierPnr(AdapterUtil.getSupplyInfo(pnrRequest).getSupplierPnr())
						.itineraryId(pnrRequest.getRequestConfig().getItineraryId())
						.lob(pnrRequest.getRequestConfig().getLob()).className(className).throwable(e)
						.extraInfo(logMessage).throwable(e).build(),
				MetricType.LOG_FILE);
	}

	private static void populateLogParams(FlightCustomLogger logger, LogParams logParams, MetricType... metricTypes) {
		logger.asyncLogging(false).className(logParams.getClassName()).crId(logParams.getCorrelationId())
				.lob(logParams.getLob()).req(logParams.getRequest()).res(logParams.getResponse())
				.source(logParams.getSrc()).itId(logParams.getItineraryId())
				.metricType(fetchMetricType(metricTypes)).servNm(logParams.getServiceName())
				.extraInfo(logParams.getExtraInfo()).componentName(METRIC_COMPONENT_NAME).timeTaken(logParams.getTimeTaken())
				.metricTags(getMetricTags(logParams, metricTypes));
	}

	private static List<Pair<MetricCustomTags, String>> getMetricTags(LogParams logParams, MetricType[] metricTypes) {
		if (metricTypes == null || metricTypes.length == 0 || !hasMetricLogging(metricTypes)) {
			return null;
		}
		List<Pair<MetricCustomTags, String>> tags = new ArrayList<>();

		if (logParams.getLob() != null) {
			Pair<MetricCustomTags, String> lob = Pair.of(MetricCustomTags.LOB, logParams.getLob());
			tags.add(lob);
		}
		if (logParams.getSrc() != null) {
			Pair<MetricCustomTags, String> lob = Pair.of(MetricCustomTags.SOURCE, logParams.getSrc());
			tags.add(lob);
		}

		if (logParams.getAirline() != null) {
			Pair<MetricCustomTags, String> airline = Pair.of(MetricCustomTags.AIRLINE, logParams.getAirline());
			tags.add(airline);
		}

		if (logParams.getType() != null) {
			Pair<MetricCustomTags, String> type = Pair.of(MetricCustomTags.TYPE, logParams.getType());
			tags.add(type);
		}
		return tags;
	}

	private static boolean hasMetricLogging(MetricType[] metricTypes) {
		for (MetricType metricType : metricTypes) {
			if (MetricType.LOG_COUNTER == metricType || MetricType.LOG_TIME == metricType) {
				return true;
			}
		}
		return false;
	}

	public static void debug(SupplyPnrRequestDTO request, String logMessage,
			String className) {
		MMTLogger.debug((new LogParams.LogParamsBuilder())
				.className(className).extraInfo(logMessage).build(),
				MetricType.LOG_FILE);
	}

	public static void debug(SupplyPnrCancelRequestDTO request, String logMessage,
							 String className) {
		MMTLogger.debug((new LogParams.LogParamsBuilder())
						.className(className).extraInfo(logMessage).build(),
				MetricType.LOG_FILE);
	}

	public static void error(SupplyPnrRequestDTO request, String logMessage,
			String className, Throwable e) {
		MMTLogger.error(
				(new LogParams.LogParamsBuilder())
						.className(className).throwable(e).extraInfo(logMessage).throwable(e).build(),
				MetricType.LOG_FILE);
	}

	public static void error(SupplyPnrCancelRequestDTO request, String logMessage,
							 String className, Throwable e) {
		MMTLogger.error(
				(new LogParams.LogParamsBuilder())
						.className(className).throwable(e).extraInfo(logMessage).throwable(e).build(),
				MetricType.LOG_FILE);
	}
	public static void logTime(FlowState state, String serviceName, long duration) {
		MMTLogger.info(
				logBuilder(state,serviceName)
						.timeTaken(duration)

						.build(),
				MetricType.LOG_FILE, MetricType.LOG_TIME);
	}


	public static LogParams.LogParamsBuilder logBuilder(FlowState state){
		return new LogParams.LogParamsBuilder()
				.correlationId(state.getValue(FlowStateKey.CRID))
				.src(state.getValue(FlowStateKey.SRC))
				.lob(state.getValue(FlowStateKey.LOB))
				;
	}

	public static LogParams.LogParamsBuilder logBuilder(FlowState state, String serviceName){
		return logBuilder(state)
				.serviceName(serviceName);
	}

	public static void logTimeForNetworkCall(FlowState state, String serviceName, Long startTime) {
		long endTime = System.currentTimeMillis();
		MMTLogger.logTime(state, serviceName, endTime-startTime);
	}

}
