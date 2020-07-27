// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;
import com.google.sps.data.WaterSystem;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Servlet that returns a list of all Superfund Sites */
@WebServlet("/water")
public class WaterPollutionServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(WaterPollutionServlet.class);
    
    public static final String EPA_WATERSYSTEM_LINK = "https://enviro.epa.gov/enviro/efservice/WATER_SYSTEM/";
    public static final String EPA_CITY_PARAMETER = "CITIES_SERVED/CONTAINING/";
    public static final String EPA_BACKUP_CITY_PARAMATER = "/CITIES_SERVED/Not%20Reported/CITY_NAME/CONTAINING/";
    public static final String EPA_STATE_PARAMETER = "/PRIMACY_AGENCY_CODE/";
    public static final String MIN_DATE = "/ENFDATE/>/01-JAN-14";
    public static final String CSV_FORMAT = "/Excel/";
    public static final String SPLITERATOR = "\",\"";
    public static final int TOTAL_CELL_COUNT = 47;

    public static final String WATER_POLLUTION_ENTITY = "WaterPollution";
    public static final String WATERSYSTEMS_PROPERTY = "watersystems";

    private static final String AREA_PARAMETER = "area";
    private static final String ZIP = "zip";
    private static final String TOWN_PARAMATER = "town";
    private static final String STATE_PARAMATER = "state";

    private static final String ZIP_PARAMETER = "zip_code";

    public static final int DEFAULT_SCORE = 100;

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    public void init() {
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        Entity entity;
        try {
			entity = datastore.get(getKeyFromLocality(request.getParameter(TOWN_PARAMATER), request.getParameter(STATE_PARAMATER)));
            String json = new Gson().toJson(
                retrieveSDWViolations(entity));
            response.getWriter().write(json);
        } catch (EntityNotFoundException e1) {
            logger.error(e1.getMessage());
			try {
                ArrayList<WaterSystem> systems = retrieveSDWViolations(request.getParameter(TOWN_PARAMATER), request.getParameter(STATE_PARAMATER));
                String json = new Gson().toJson(systems);
                response.getWriter().write(json);
                Key key = getKeyFromLocality(request.getParameter(TOWN_PARAMATER), request.getParameter(STATE_PARAMATER));
                addToDatabase(key, systems);
            } catch (IOException e){
                response.sendError(500);
                logger.error(e.getMessage());
            }
		}
        
    }

    public ArrayList<WaterSystem> retrieveSDWViolations(Entity waterPollutionEntity) throws EntityNotFoundException,
            IOException {
        ArrayList<WaterSystem> systems = new ArrayList<>();

        byte[] contaminantsData = Base64.getDecoder().decode(((Text)waterPollutionEntity.getProperty(WATERSYSTEMS_PROPERTY)).getValue());
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(contaminantsData));
        ArrayList<Key> waterSystemKeys;
        try {
            waterSystemKeys = (ArrayList<Key>) ois.readObject();
        } catch (ClassNotFoundException e) {
            waterSystemKeys = new ArrayList<Key>();
            logger.error(e.getMessage());
        }
        ois.close();
        
        for(Entity waterSystemEntity: datastore.get(waterSystemKeys).values()){
            systems.add(new WaterSystem(waterSystemEntity));
        }
        logger.info("Receive violations from DB");
        return systems;
    }

    /**
    * A function that returns an arraylist of all the superfund sites in 
    * a given zip code
    * @param areaCode the zip code or state to pull data for
    * @param areaType the Zip or State format for the EPA URL
    * @return the list of superfund sites pulled from the EPA API
    */
    public ArrayList<WaterSystem> retrieveSDWViolations(String town, String state) throws IOException{
        logger.info("Start receiving water violations from EPA for "+town+", "+state);
        URL url = new URL(EPA_WATERSYSTEM_LINK+EPA_CITY_PARAMETER+town.toUpperCase()+EPA_STATE_PARAMETER+state + CSV_FORMAT);
        ArrayList<WaterSystem> systems = parseViolationsFromURL(url);
        //backup setup to pull data, leaving here in case I want to reenable it
        // URL urlPostal = new URL(EPA_WATERSYSTEM_LINK+EPA_STATE_PARAMETER+state +EPA_BACKUP_CITY_PARAMATER+town.toUpperCase() + CSV_FORMAT);
        // systems.addAll(parseViolationsFromURL(urlPostal));
        logger.info("Completed receiving water violations from EPA for "+town+", "+state);
        return systems;
    }

    public ArrayList<WaterSystem> parseViolationsFromURL(URL url) throws IOException {
        ArrayList<WaterSystem> waterSystems = new ArrayList<>();
        CSVParser csvParser = CSVParser.parse(url, Charset.defaultCharset(), CSVFormat.EXCEL.withFirstRecordAsHeader());
        for(CSVRecord csvRecord: csvParser.getRecords()){
            WaterSystem system = new WaterSystem(csvRecord);
            system.addViolations();
            waterSystems.add(system);
        }
        csvParser.close();
        return waterSystems;
    }

    public Key addToDatabase(Key key, ArrayList<WaterSystem> systems) throws IOException {
        Entity systemEntity = new Entity(WATER_POLLUTION_ENTITY, key.getName());
        ArrayList<Key> keyList = new ArrayList<Key>();
        for(WaterSystem system: systems){
            keyList.add(system.addToDatabase());
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(keyList);
        oos.close();
        systemEntity.setProperty(WATERSYSTEMS_PROPERTY, new Text(
            Base64.getEncoder().encodeToString(baos.toByteArray())));
        datastore.put(systemEntity);
        return systemEntity.getKey();
    }

    public Key getKeyFromLocality(String city, String state){
        String keyName = state+" "+city;
        return KeyFactory.createKey(WATER_POLLUTION_ENTITY, keyName);
    }

}