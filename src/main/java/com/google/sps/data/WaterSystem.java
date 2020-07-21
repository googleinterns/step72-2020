package com.google.sps.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Key;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class WaterSystem {

    public static final String EPA_WATERSYSTEM_LINK = "https://enviro.epa.gov/enviro/efservice/WATER_SYSTEM/PWSID/";
    public static final String EPA_VIOLATIONS_LINK = "https://enviro.epa.gov/enviro/efservice/SDW_CONTAM_VIOL_ZIP/";
    public static final String EPA_DATE_PARAMATER = "ENFDATE/%3E/01-JAN-12/";
    public static final String EPA_PWSID_PARAMATER = "PWSID/";
    public static final String CSV_FORMAT = "/Excel/";
    public static final String SPLITERATOR = "\",\"";
    public static final int TOTAL_CELL_COUNT = 21;

    public static final String WATER_SYSTEM_ENTITY = "WaterSystem";
    public static final String PWSID_PROPERTY = "pwsid";
    public static final String NAME_PROPERTY = "name";
    public static final String CONTAMINANTS_PROPERTY = "contaminants";
    public static final String STATE_PROPERTY = "state";
    public static final String CITY_PROPERTY = "city";
    public static final String COUNTY_PROPERTY = "county";
    public static final String POPULATION_PROPERTY = "populationServed";

    private String pwsid;
    private String name;
    private ArrayList<WaterContaminant> contaminants;
    private String state;
    private String city;
    private String county;
    private double populationServed;

    public WaterSystem(String pwsid, String name, String state, String city, String county, double populationServed) {
        this.pwsid = pwsid;
        this.name = name;
        this.contaminants = new ArrayList<WaterContaminant>();
        this.state = state;
        this.city = city;
        this.county = county;
        this.populationServed = populationServed;
    }

    /**
     * Constructs Water System from EPA API data
     */
    public WaterSystem(String pwsid) {
        this.pwsid = pwsid;
        this.contaminants = new ArrayList<WaterContaminant>();
        URL url;
        try {
            url = new URL(EPA_WATERSYSTEM_LINK + pwsid + CSV_FORMAT);
            Scanner scanner = new Scanner(url.openStream());
            scanner.nextLine();
            if (scanner.hasNextLine()) {
                String[] cells = scanner.nextLine().split(SPLITERATOR);
                this.name = cells[1];
                this.state = cells[3];
                this.city = cells[45];
                this.county = cells[46];
                this.populationServed = Integer.parseInt(cells[15]);
            } else {
                System.out.println("THere is no data for "+pwsid);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WaterSystem(String[] cells){
        this.pwsid = cells[0].replace("\"", "");
        this.name = cells[1];
        this.state = cells[3];
        this.city = cells[45];
        this.county = cells[46].replace("\"", "");
        this.populationServed = Integer.parseInt(cells[15]);
        this.contaminants = new ArrayList<WaterContaminant>();
    }

    public void addViolations(){
        HashMap<String, WaterContaminant> contaminantsMap = new HashMap<String, WaterContaminant>();
        try {
            URL url = new URL(EPA_VIOLATIONS_LINK+EPA_DATE_PARAMATER+EPA_PWSID_PARAMATER+pwsid + CSV_FORMAT);
            Scanner scanner= new Scanner(url.openStream());
            scanner.nextLine();
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                //cleans data to ignore new line characters within a cell
                while(scanner.hasNextLine() && line.split(SPLITERATOR).length < TOTAL_CELL_COUNT){
                    line += scanner.nextLine();
                }
                // System.out.println(line);
                String[] cells = line.split(SPLITERATOR);
                if(cells.length < TOTAL_CELL_COUNT) continue;
                String contaminantName = cells[7];
                contaminantsMap.putIfAbsent(contaminantName, new WaterContaminant(cells));
                String violationDate = cells[18];
                String enforcementAction = cells[17];
                contaminantsMap.get(contaminantName).addViolationInstance(violationDate, enforcementAction);
                // System.out.println(contaminantsMap.get(contaminantName));
            }
            scanner.close();
        } catch (IOException e){
            e.printStackTrace();
            System.out.println("IO Error in Finding SDW Violations");
        }
        contaminants.addAll(contaminantsMap.values());
        // System.out.println(contaminants);
    }

    public Key addToDatabase(){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity systemEntity = new Entity(WATER_SYSTEM_ENTITY);
        systemEntity.setProperty(PWSID_PROPERTY, pwsid);
        systemEntity.setProperty(NAME_PROPERTY, name);
        systemEntity.setProperty(CONTAMINANTS_PROPERTY, null); //need to add key
        systemEntity.setProperty(STATE_PROPERTY, state);
        systemEntity.setProperty(CITY_PROPERTY, city);
        systemEntity.setProperty(COUNTY_PROPERTY, county);
        systemEntity.setProperty(POPULATION_PROPERTY, populationServed);
        datastore.put(systemEntity);
        return systemEntity.getKey();
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
    

    /* This class is nested static so that Gson
    * can turn a WaterSystem into a Json 
     */
    public static class WaterContaminant {

        public static final String CONTAMINANT_ENTITY = "contaminant";
        public static final String CCODE_PROPERTY = "code";
        public static final String CNAME_PROPERTY = "name";
        public static final String SOURCES_PROPERTY = "sources";
        public static final String DEFINITION_PROPERTY = "definition";
        public static final String HEALTH_PROPERTY = "health";

        private int contaminantCode;
        private String contaminantName;
        private String sources;
        private String definition;
        private String healthEffects;
        //the key is the date and the value are the enforcement actions for that date
        private HashMap<String, ArrayList<String>> violations;
    
    
    
        public WaterContaminant(final String[] sdwViolationCells) {
            contaminantCode = Integer.parseInt(sdwViolationCells[6]);
            contaminantName = sdwViolationCells[7];
            sources = sdwViolationCells[8];
            definition = sdwViolationCells[9];
            healthEffects = sdwViolationCells[10];
            violations = new HashMap<String, ArrayList<String>>();
        }
    
        public void addViolationInstance(String violationDate, String enforcementAction) {
            if(violationDate == null || enforcementAction == null){
                System.out.println("Violation date or action is null");
                return;
            }
            violations.putIfAbsent(violationDate, new ArrayList<String>());
            if(!violations.get(violationDate).contains(enforcementAction)){
                violations.get(violationDate).add(enforcementAction);
            }
        }

        public Key addToDatabase(){
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Entity contamEntity = new Entity(CONTAMINANT_ENTITY);
            contamEntity.setProperty(CCODE_PROPERTY, contaminantCode);
            contamEntity.setProperty(CNAME_PROPERTY, contaminantName);
            contamEntity.setProperty(SOURCES_PROPERTY, sources);
            contamEntity.setProperty(DEFINITION_PROPERTY, definition);
            contamEntity.setProperty(HEALTH_PROPERTY, healthEffects);
            datastore.put(contamEntity);
            return contamEntity.getKey();
        }
    
        public String toString() {
            return contaminantName + " during " + violations.toString();
        }
    
    }
}