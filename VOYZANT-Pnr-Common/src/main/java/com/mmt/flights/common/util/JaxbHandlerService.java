package com.mmt.flights.common.util;

import org.springframework.stereotype.Service;

import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 *
 */
@Service
public class JaxbHandlerService {

    private final Map<Class<?>, JAXBContext> jaxbContextMap = new ConcurrentHashMap<>();

    private static boolean isXmlRootElement(Class clazz) {
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() != null && annotation.annotationType().getName().contains(XmlRootElement.class.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Use this method for general use.
     */
    public <T> T unMarshall(String xml, Class<? extends T> clazz) throws JAXBException {
        return unMarshall(xml, clazz, false);
    }

    /**
     * Use this method only if you want to pass third parameter as true.
     */
    @SuppressWarnings("unchecked")
    public <T> T unMarshall(String xml, Class<? extends T> clazz, boolean castFromJaxB) throws JAXBException {
        JAXBContext jaxbUnMarshallerContext = getJaxbMarshallerContext(clazz);
        Unmarshaller unmarshaller = jaxbUnMarshallerContext.createUnmarshaller();

        StringReader reader = new StringReader(xml);
        if (isXmlRootElement(clazz)) {
            if (castFromJaxB) {
                return ((JAXBElement<T>) unmarshaller.unmarshal(reader)).getValue();
            }
            return (T) unmarshaller.unmarshal(reader);
        } else {
            JAXBElement root = unmarshaller.unmarshal(new StreamSource(reader), clazz);
            return (T) root.getValue();
        }
    }

    public String marshall(Object obj) throws JAXBException {
        Class<?> clazz = obj.getClass();
        JAXBContext jaxbMarshallerContext = getJaxbMarshallerContext(clazz);
        Marshaller marshaller = jaxbMarshallerContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

        StringWriter stringWriter = new StringWriter();
        if (isXmlRootElement(clazz)) {
            marshaller.marshal(obj, stringWriter);
        } else {
            QName qName = new QName(clazz.getName(), clazz.getSimpleName());
            JAXBElement root = new JAXBElement(qName, clazz, obj);
            marshaller.marshal(root, stringWriter);
        }
        return stringWriter.toString();
    }

    public JAXBContext getJaxbMarshallerContext(Class<?> clazz) throws JAXBException {
        JAXBContext jaxbMarshallerContext = jaxbContextMap.get(clazz);
        if (jaxbMarshallerContext == null) {
            jaxbMarshallerContext = JAXBContext.newInstance(clazz);
            jaxbContextMap.put(clazz, jaxbMarshallerContext);
        }
        return jaxbMarshallerContext;
    }
}