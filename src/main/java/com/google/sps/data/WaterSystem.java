package com.google.sps.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class WaterSystem {

    public static final String EPA_WATERSYSTEM_LINK = "https://enviro.epa.gov/enviro/efservice/WATER_SYSTEM/PWSID/";
    public static final String EPA_VIOLATIONS_LINK = "https://enviro.epa.gov/enviro/efservice/SDW_CONTAM_VIOL_ZIP/";
    public static final String EPA_DATE_PARAMATER = "ENFDATE/%3E/01-JAN-12/";
    public static final String EPA_PWSID_PARAMATER = "PWSID/";
    public static final String CSV_FORMAT = "/Excel/";
    public static final String SPLITERATOR = "\",\"";
    public static final int TOTAL_CELL_COUNT = 21;

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
                System.out.println("There is no data for "+pwsid);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WaterSystem(CSVRecord csvRecord){
        this.pwsid = csvRecord.get(0);
        this.name = csvRecord.get(1);
        this.state = csvRecord.get(3);
        this.city = csvRecord.get(45);
        this.county = csvRecord.get(46);
        this.populationServed = Integer.parseInt(csvRecord.get(15));
        this.contaminants = new ArrayList<WaterContaminant>();
    }

    public void addViolations(){
        HashMap<String, WaterContaminant> contaminantsMap = new HashMap<String, WaterContaminant>();
        try {
            URL url = new URL(EPA_VIOLATIONS_LINK+EPA_DATE_PARAMATER+EPA_PWSID_PARAMATER+pwsid + CSV_FORMAT);
            CSVParser csvParser = CSVParser.parse(url, Charset.defaultCharset(), CSVFormat.EXCEL.withFirstRecordAsHeader());
            for(CSVRecord csvRecord: csvParser.getRecords()){
                String contaminantName = csvRecord.get(7);
                contaminantsMap.putIfAbsent(contaminantName, new WaterContaminant(csvRecord));
                String violationDate = csvRecord.get(18);
                String enforcementAction = csvRecord.get(17);
                contaminantsMap.get(contaminantName).addViolationInstance(violationDate, enforcementAction);
            }
            csvParser.close();
        } catch (IOException e){
            e.printStackTrace();
            System.out.println("IO Error in Finding SDW Violations");
        }
        contaminants.addAll(contaminantsMap.values());
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

        private int contaminantCode;
        private String contaminantName;
        private String sources;
        private String definition;
        private String healthEffects;
        //the key is the date and the value are the enforcement actions for that date
        private HashMap<String, ArrayList<String>> violations;
    
    
    
        public WaterContaminant(CSVRecord csvRecord) {
            contaminantCode = Integer.parseInt(csvRecord.get(6));
            contaminantName = csvRecord.get(7);
            sources = csvRecord.get(8);
            definition = csvRecord.get(9);
            healthEffects = csvRecord.get(10);
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
    
        public String toString() {
            return contaminantName + " during " + violations.toString();
        }
    
    
        
    }
}