

package com.google.sps.data;

public class SuperfundSite {

    private String name;
    private int siteId;
    private String state;
    private String city;
    private String county;
    private String status;
    private double latitude;
    private double longitude;

    public SuperfundSite(String name,
        int siteId,
        String state,
        String city,
        String county,
        String status,
        double latitude,
        double longitude){
        this.name = name;
        this.siteId = siteId;
        this.state = state;
        this.city = city;
        this.county = county;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean isValidSite(){
        return latitude != 0 && longitude != 0 && !status.equalsIgnoreCase("Not on the NPL");
    }

    public boolean equals(Object o){
        if(this == o) return true;
        
        if (!(o instanceof SuperfundSite)) return false;

        SuperfundSite otherSite = (SuperfundSite) o;

        return name.equals(otherSite.name) &&
            state.equals(otherSite.state) &&
            city.equals(otherSite.city) &&
            county.equals(otherSite.county) &&
            latitude == otherSite.latitude &&
            longitude == otherSite.longitude;
    }

    public String toString(){
        return name+" in "+city+", "+state+" at "+latitude +", "+ longitude;
    }
    
}