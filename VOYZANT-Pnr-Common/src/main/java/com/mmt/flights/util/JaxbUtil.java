package com.mmt.flights.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mmt.flights.common.util.JaxbHandlerService;
import com.mmt.flights.flightsutil.JsonConvertor;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class JaxbUtil {

	private static JaxbUtil instance;
	private JaxbHandlerService service;

	private JaxbUtil() {
		service = new JaxbHandlerService();
	};

	public static JaxbUtil getInstance() {
		if (instance == null) {
			instance = new JaxbUtil();
		}
		return instance;
	}

	public String getText(String filePath) {
		InputStream input = getClass().getResourceAsStream(filePath);
		BufferedReader buffer = new BufferedReader(new InputStreamReader(input));
		return buffer.lines().collect(Collectors.joining("\n"));
	}

	public <T> T getObjectFromJson(String filePath, Class<T> respClass)
			throws JsonParseException, JsonMappingException, IOException {
		String request = getText(filePath);
		return JsonConvertor.convertJsonToObject(request, respClass);
	}
	
	public String getJsonString(Object obj) throws JsonProcessingException
	{
		return JsonConvertor.convertObjectToJson(obj);
	}
	
	public <T> T getUnMarshalledObject(String filePath, Class<T> respClass) throws JAXBException {
		return service.unMarshall(getText(filePath), respClass);
	}
	
	public String marshallObject(Object obj) throws JAXBException
	{
		return service.marshall(obj);
	}
}
