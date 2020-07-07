package com.google.sps.data;

import java.util.HashMap;

public class WaterSystem {

    private String pwsid;
    private String name;
    private HashMap<String, WaterViolation> violations;
    private String state;
    private String city;
    private String county;
    private double populationServed;

    public WaterSystem(String pwsid,
        String name,
        String state,
        String city,
        String county,
        double populationServed){
        this.pwsid = pwsid;
        this.name = name;
        this.violations = new HashMap<String, WaterViolation>();
        this.state = state;
        this.city = city;
        this.county = county;
        this.populationServed = populationServed;
    }

    public void addViolation(String[] violationCells){
        
    }

    public boolean equals(Object o){
        if(this == o) return true;
        
        if (!(o instanceof WaterSystem)) return false;

        WaterSystem otherSite = (WaterSystem) o;

        return pwsid.equals(otherSite.pwsid);
    }

    public String toString(){
        return name+" in "+city+", "+state+" of ID "+ pwsid;
    }
    
}