

package com.google.sps.data;

public class SuperfundSite {

    private String name;
    private double score;
    private String state;
    private String city;
    private String county;
    private String status;
    private double lattitude;
    private double longitude;

    public SuperfundSite(String name,
        double score,
        String state,
        String city,
        String county,
        String status,
        double lattitude,
        double longitude){
        this.name = name;
        this.score = score;
        this.state = state;
        this.city = city;
        this.county = county;
        this.status = status;
        this.lattitude = lattitude;
        this.longitude = longitude;
    }

    public boolean isValidSite(){
        return lattitude != 0 && longitude != 0 && !status.equalsIgnoreCase("Not on the NPL");
    }

    public boolean equals(Object o){
        if(this == o) return true;
        
        if (!(o instanceof SuperfundSite)) return false;

        SuperfundSite otherSite = (SuperfundSite) o;

        return name.equals(otherSite.name) &&
            state.equals(otherSite.state) &&
            city.equals(otherSite.city) &&
            county.equals(otherSite.county) &&
            lattitude == otherSite.lattitude &&
            longitude == otherSite.longitude;
    }

    public String toString(){
        return name+" in "+city+", "+state+" at "+lattitude +", "+ longitude;
    }
    
}