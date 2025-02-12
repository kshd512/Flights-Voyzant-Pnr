package com.mmt.flights.entity.pnr.retrieve.request;

class Document {
    private String Name;
    private String referenceversion;

    // Getters and Setters
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getReferenceversion() {
        return referenceversion;
    }

    public void setReferenceversion(String referenceversion) {
        this.referenceversion = referenceversion;
    }
}
