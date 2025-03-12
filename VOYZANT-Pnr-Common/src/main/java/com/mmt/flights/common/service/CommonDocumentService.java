package com.mmt.flights.common.service;

import com.mmt.flights.entity.common.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CommonDocumentService {

    public Document createDocument() {
        Document document = new Document();
        document.setName("MMT");
        document.setReferenceVersion("1.0");
        return document;
    }

    public Party createParty() {
        Party party = new Party();
        party.setSender(createSender());
        return party;
    }

    private Sender createSender() {
        Sender sender = new Sender();
        sender.setTravelAgencySender(createTravelAgencySender());
        return sender;
    }

    private TravelAgencySender createTravelAgencySender() {
        TravelAgencySender travelAgencySender = new TravelAgencySender();
        travelAgencySender.setName("MMT");
        travelAgencySender.setIataNumber("");
        travelAgencySender.setAgencyID("");
        travelAgencySender.setContacts(createContacts());
        return travelAgencySender;
    }

    private Contacts createContacts() {
        Contacts contacts = new Contacts();
        Contact contact = new Contact();
        contact.setEmailContact("pst@claritytts.com");
        contacts.setContact(Collections.singletonList(contact));
        return contacts;
    }
}