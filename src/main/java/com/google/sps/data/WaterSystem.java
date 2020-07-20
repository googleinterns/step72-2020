package com.google.sps.data;

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

    // private void addViolation(final String[] violationCells, final HashMap<String, WaterContaminant> contaminantsMap){
    //     if(violationCells.length < TOTAL_CELL_COUNT){
    //         System.out.println("Too few cells");
    //         return;
    //     }
    //     System.out.println(violationCells[7] + " in "+pwsid);
    //     String contaminantName = violationCells[7];
    //     contaminantsMap.putIfAbsent(contaminantName, new WaterContaminant(violationCells));
    //     String violationDate = violationCells[18];
    //     String enforcementAction = violationCells[17];
    //     contaminantsMap.get(contaminantName).addViolationInstance(violationDate, enforcementAction);
    //     System.out.println(contaminantsMap.get(contaminantName));
    // }

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
    
        public String toString() {
            return contaminantName + " during " + violations.toString();
        }
    
    
        
    }
}