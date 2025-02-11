package com.mmt.flights.common.logging;

import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.postsales.logger.FunnelStep;

public class LogParams {

	private String type;
	private String correlationId;
	private String lob;
	private String className;
	private String request;
	private String response;
	private String serviceName;
	private String extraInfo;
	private Long timeTaken;
	private String errorCode;
	private Throwable throwable;
	private Integer httpStatus;
	private String supplierPnr;
	private String airlinePnr;
	private String src;
	private String itineraryId;
	private String airline;

	public String getType() {
		return type;
	}

	public String getItineraryId() {
		return itineraryId;
	}

	public void setItineraryId(String itineraryId) {
		this.itineraryId = itineraryId;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public String getLob() {
		return lob;
	}

	public String getClassName() {
		return className;
	}

	public String getRequest() {
		return request;
	}

	public String getResponse() {
		return response;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getExtraInfo() {
		return extraInfo;
	}

	public Long getTimeTaken() {
		return timeTaken;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public Integer getHttpStatus() {
		return httpStatus;
	}

	public String getSupplierPnr() {
		return supplierPnr;
	}

	public String getAirlinePnr() {
		return airlinePnr;
	}

	public String getSrc() {
		return src;
	}
	public String getAirline() {
		return airline;
	}

	public static class LogParamsBuilder {
		private String correlationId;
		private String lob;
		private String className;
		private String request;
		private String response;
		private String serviceName;
		private String extraInfo;
		private Long timeTaken;
		private String errorCode;
		private Throwable throwable;
		private Integer httpStatus;
		private String supplierPnr;
		private String airlinePnr;
		private String src;
		private String itineraryId;
		private String airline;
		private String type;

		public LogParamsBuilder lobSrcAndLogKey(FlowState state) {
			this.correlationId = state.getValue(FlowStateKey.LOG_KEY);;
			this.lob= state.getValue(FlowStateKey.LOB);
			this.src = state.getValue(FlowStateKey.SOURCE);
			this.airline = state.getValue(FlowStateKey.AIRLINE);
			return this;
		}
		public LogParamsBuilder airline(String airline) {
			this.airline = airline;
			return this;
		}

		public LogParamsBuilder src(String src) {
			this.src = src;
			return this;
		}

		public LogParamsBuilder correlationId(String correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public LogParamsBuilder lob(String lob) {
			this.lob = lob;
			return this;
		}

		public LogParamsBuilder className(String className) {
			this.className = className;
			return this;
		}

		public LogParamsBuilder request(String request) {
			this.request = request;
			return this;
		}

		public LogParamsBuilder response(String response) {
			this.response = response;
			return this;
		}

		public LogParamsBuilder serviceName(String serviceName) {
			this.serviceName = serviceName;
			return this;
		}

		public LogParamsBuilder type(FunnelStep type) {
			if(type!=null)
				this.type = type.name();
			return this;
		}

		public LogParamsBuilder extraInfo(String extraInfo) {
			this.extraInfo = extraInfo;
			return this;
		}

		public LogParamsBuilder timeTaken(Long timeTaken) {
			this.timeTaken = timeTaken;
			return this;
		}

		public LogParamsBuilder errorCode(String errorCode) {
			this.errorCode = errorCode;
			return this;
		}

		public LogParamsBuilder throwable(Throwable throwable) {
			this.throwable = throwable;
			return this;
		}

		public LogParamsBuilder httpStatus(Integer httpStatus) {
			this.httpStatus = httpStatus;
			return this;
		}

		public LogParamsBuilder supplierPnr(String supplierPnr) {
			this.supplierPnr = supplierPnr;
			return this;
		}

		public LogParamsBuilder airlinePnr(String airlinePnr) {
			this.airlinePnr = airlinePnr;
			return this;
		}

		public LogParamsBuilder itineraryId(String itineraryId){
			this.itineraryId = itineraryId;
			return this;
		}

		public LogParams build() {
			return new LogParams(this);
		}
	}

	private LogParams(LogParamsBuilder builder) {
		this.correlationId = builder.correlationId;
		this.lob = builder.lob;
		this.className = builder.className;
		this.request = builder.request;
		this.response = builder.response;
		this.serviceName = builder.serviceName;
		this.extraInfo = builder.extraInfo;
		this.timeTaken = builder.timeTaken;
		this.errorCode = builder.errorCode;
		this.throwable = builder.throwable;
		this.httpStatus = builder.httpStatus;
		this.airlinePnr = builder.airlinePnr;
		this.supplierPnr = builder.supplierPnr;
		this.src = builder.src;
		this.itineraryId = builder.itineraryId;
		this.airline = builder.airline;
		this.type = builder.type;
	}

}
