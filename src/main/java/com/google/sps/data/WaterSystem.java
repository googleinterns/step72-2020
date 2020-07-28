package com.google.sps.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaterSystem {
    private static final Logger logger = LoggerFactory.getLogger(WaterSystem.class);

    public static final String EPA_WATERSYSTEM_LINK = "https://enviro.epa.gov/enviro/efservice/WATER_SYSTEM/PWSID/";
    public static final String EPA_VIOLATIONS_LINK = "https://enviro.epa.gov/enviro/efservice/SDW_CONTAM_VIOL_ZIP/";
    public static final String EPA_DATE_PARAMATER = "ENFDATE/%3E/01-JAN-12/";
    public static final String EPA_PWSID_PARAMATER = "PWSID/";
    public static final String CSV_FORMAT = "/Excel/";
    public static final String SPLITERATOR = "\",\"";
    public static final int TOTAL_CELL_COUNT = 21;

    private static final int PUBLIC_WATER_SYSTEM_ID_COLUMN = 0;
    private static final int NAME_COLUMN = 1;
    private static final int STATE_COLUMN = 3;
    private static final int CITY_COLUMN = 45;
    private static final int COUNTY_COLUMN = 46;
    private static final int POPULATION_COLUMN = 15;

    private static final int CONTAMINANT_NAME_COLUMN = 7;
    private static final int VIOLATION_COLUMN = 18;
    private static final int ENFORCEMENT_ACTION_COLUMN = 17;

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
    private int populationServed;

    public WaterSystem(String pwsid, String name, String state, String city, String county, int populationServed) {
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
            CSVParser csvParser = CSVParser.parse(url, Charset.defaultCharset(),
            CSVFormat.EXCEL.withFirstRecordAsHeader());
            try {
                CSVRecord csvRecord = csvParser.getRecords().get(0);
                this.name = csvRecord.get(NAME_COLUMN);
                this.state = csvRecord.get(STATE_COLUMN);
                this.city = csvRecord.get(CITY_COLUMN);
                this.county = csvRecord.get(STATE_COLUMN);
                this.populationServed = Integer.parseInt(csvRecord.get(POPULATION_COLUMN));
                this.contaminants = new ArrayList<WaterContaminant>();
            } catch (IndexOutOfBoundsException e) {
                logger.error("There is no data for " + pwsid);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WaterSystem(CSVRecord csvRecord) {
        this.pwsid = csvRecord.get(PUBLIC_WATER_SYSTEM_ID_COLUMN);
        this.name = csvRecord.get(NAME_COLUMN);
        this.state = csvRecord.get(STATE_COLUMN);
        this.city = csvRecord.get(CITY_COLUMN);
        this.county = csvRecord.get(COUNTY_COLUMN);
        this.populationServed = Integer.parseInt(csvRecord.get(POPULATION_COLUMN));
        this.contaminants = new ArrayList<WaterContaminant>();
    }

    public WaterSystem(Entity entity) throws EntityNotFoundException, IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        this.pwsid = (String) entity.getProperty(PWSID_PROPERTY);
        this.name = (String) entity.getProperty(NAME_PROPERTY);
        this.state = (String) entity.getProperty(STATE_PROPERTY);
        this.city = (String) entity.getProperty(CITY_PROPERTY);
        this.county = (String) entity.getProperty(COUNTY_PROPERTY);
        this.populationServed = ((Long) entity.getProperty(POPULATION_PROPERTY)).intValue();
        this.contaminants = new ArrayList<WaterContaminant>();
        byte[] contaminantsData = Base64.getDecoder().decode(((Text)entity.getProperty(CONTAMINANTS_PROPERTY)).getValue());
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(contaminantsData));
        ArrayList<Key> contaminantKeys;
        try {
            contaminantKeys = (ArrayList<Key>) ois.readObject();
        } catch (ClassNotFoundException e) {
            contaminantKeys = new ArrayList<Key>();
            // logger.error(e.getMessage());
            e.printStackTrace();
        }
        ois.close();

        logger.info(pwsid+" contaminant keys "+contaminantKeys);

        for (Entity contaminantEntity : datastore.get(contaminantKeys).values()) {
            contaminants.add(new WaterContaminant(contaminantEntity));
        }
    }

    public void addViolations() {
        HashMap<String, WaterContaminant> contaminantsMap = new HashMap<String, WaterContaminant>();
        try {
            URL url = new URL(EPA_VIOLATIONS_LINK + EPA_DATE_PARAMATER + EPA_PWSID_PARAMATER + pwsid + CSV_FORMAT);
            CSVParser csvParser = CSVParser.parse(url, Charset.defaultCharset(),
                    CSVFormat.EXCEL.withFirstRecordAsHeader());
            for (CSVRecord csvRecord : csvParser.getRecords()) {
                String contaminantName = csvRecord.get(CONTAMINANT_NAME_COLUMN);
                contaminantsMap.putIfAbsent(contaminantName, new WaterContaminant(csvRecord));
                String violationDate = csvRecord.get(VIOLATION_COLUMN);
                String enforcementAction = csvRecord.get(ENFORCEMENT_ACTION_COLUMN);
                contaminantsMap.get(contaminantName).addViolationInstance(violationDate, enforcementAction);
            }
            csvParser.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IO Error in Finding SDW Violations");
        }
        contaminants.addAll(contaminantsMap.values());
    }

    public Key addToDatabase() throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity systemEntity = new Entity(WATER_SYSTEM_ENTITY, pwsid);
        systemEntity.setProperty(PWSID_PROPERTY, pwsid);
        systemEntity.setProperty(NAME_PROPERTY, name);
        ArrayList<Key> keyList = new ArrayList<Key>();
        for (WaterContaminant contaminant : contaminants) {
            keyList.add(contaminant.addToDatabase());
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(keyList);
        oos.close();
            
        systemEntity.setProperty(CONTAMINANTS_PROPERTY, new Text(
            Base64.getEncoder().encodeToString(baos.toByteArray())));
        systemEntity.setProperty(STATE_PROPERTY, state);
        systemEntity.setProperty(CITY_PROPERTY, city);
        systemEntity.setProperty(COUNTY_PROPERTY, county);
        systemEntity.setProperty(POPULATION_PROPERTY, populationServed);
        datastore.put(systemEntity);
        return systemEntity.getKey();
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof WaterSystem))
            return false;

        WaterSystem otherSite = (WaterSystem) o;

        return pwsid.equals(otherSite.pwsid);
    }

    public String toString() {
        return name + " in " + city + ", " + state + " of ID " + pwsid;
    }

    /*
     * This class is nested static so that Gson can turn a WaterSystem into a Json
     */
    public static class WaterContaminant {
        private static final Logger logger = LoggerFactory.getLogger(WaterContaminant.class);

        public static final String CONTAMINANT_ENTITY = "contaminant";
        public static final String VIOLATIONS_ENTITY = "violations";
        public static final String CCODE_PROPERTY = "code";
        public static final String CNAME_PROPERTY = "name";
        public static final String SOURCES_PROPERTY = "sources";
        public static final String DEFINITION_PROPERTY = "definition";
        public static final String HEALTH_PROPERTY = "health";
        public static final String VIOLATIONS_PROPERTY = "violations";

        private static final int CONTAMINANT_CODE_COLUMN = 6;
        private static final int CONTAMINANT_NAME_COLUMN = 7;
        private static final int SOURCES_COLUMN = 8;
        private static final int DEFINITION_COLUMN = 9;
        private static final int HEALTH_EFFECTS_COLUMN = 10;

        private int contaminantCode;
        private String contaminantName;
        private String sources;
        private String definition;
        private String healthEffects;
        // the key is the date and the value are the enforcement actions for that date
        private HashMap<String, ArrayList<String>> violations;

        public WaterContaminant(CSVRecord csvRecord) {
            contaminantCode = Integer.parseInt(csvRecord.get(CONTAMINANT_CODE_COLUMN));
            contaminantName = csvRecord.get(CONTAMINANT_NAME_COLUMN);
            sources = csvRecord.get(SOURCES_COLUMN);
            definition = csvRecord.get(DEFINITION_COLUMN);
            healthEffects = csvRecord.get(HEALTH_EFFECTS_COLUMN);
            violations = new HashMap<String, ArrayList<String>>();
        }

        public WaterContaminant(Entity violationEntity) throws IOException {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            contaminantCode = ((Long) violationEntity.getProperty(CCODE_PROPERTY)).intValue();
            Key contamKey = KeyFactory.createKey(CONTAMINANT_ENTITY, contaminantCode);
            Entity contamEntity;
			try {
				contamEntity = datastore.get(contamKey);
                contaminantName = (String) contamEntity.getProperty(CNAME_PROPERTY);
                sources = (String) contamEntity.getProperty(SOURCES_PROPERTY);
                definition = (String) contamEntity.getProperty(DEFINITION_PROPERTY);
                healthEffects = (String) contamEntity.getProperty(HEALTH_PROPERTY);
                byte[] violationData = Base64.getDecoder().decode(((Text)violationEntity.getProperty(VIOLATIONS_PROPERTY)).getValue());
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(violationData));
                try {
                    violations = (HashMap<String, ArrayList<String>>) ois.readObject();
                } catch (ClassNotFoundException e) {
                    violations = new HashMap<String, ArrayList<String>>();
                    // logger.error(e.getMessage());
                    e.printStackTrace();
                }
                ois.close();
			} catch (EntityNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }

        public void addViolationInstance(String violationDate, String enforcementAction) {
            if (violationDate == null || enforcementAction == null) {
                logger.error("Violation date or action is null");
                return;
            }
            violations.putIfAbsent(violationDate, new ArrayList<String>());
            if (!violations.get(violationDate).contains(enforcementAction)) {
                violations.get(violationDate).add(enforcementAction);
            }
        }

        public Key addToDatabase() throws IOException {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Key contamKey = KeyFactory.createKey(CONTAMINANT_ENTITY, contaminantCode);
            try {
                datastore.get(contamKey);
            } catch (EntityNotFoundException e) {
                Entity contamEntity = new Entity(CONTAMINANT_ENTITY, contaminantCode);
                contamEntity.setProperty(CCODE_PROPERTY, contaminantCode);
                contamEntity.setProperty(CNAME_PROPERTY, contaminantName);
                contamEntity.setProperty(SOURCES_PROPERTY, sources);
                contamEntity.setProperty(DEFINITION_PROPERTY, definition);
                contamEntity.setProperty(HEALTH_PROPERTY, healthEffects);
                datastore.put(contamEntity);
            }
            Entity violationEntity = new Entity(VIOLATIONS_ENTITY);
            violationEntity.setProperty(CCODE_PROPERTY, contaminantCode);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(violations);
            oos.close();
            violationEntity.setProperty(VIOLATIONS_PROPERTY, new Text(
                Base64.getEncoder().encodeToString(baos.toByteArray())));
            datastore.put(violationEntity);
            return violationEntity.getKey();
        }
    
        public String toString() {
            return contaminantName + " during " + violations.toString();
        }
    
    }
}