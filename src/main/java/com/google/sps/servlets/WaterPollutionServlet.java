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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.sps.data.WaterSystem;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.CharSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Servlet that returns a list of all Superfund Sites */
@WebServlet("/water")
public class WaterPollutionServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(WaterPollutionServlet.class);
    
    public static final String EPA_WATERSYSTEM_LINK = "https://enviro.epa.gov/enviro/efservice/WATER_SYSTEM/";
    public static final String EPA_CITY_PARAMETER = "CITIES_SERVED/CONTAINS/";
    public static final String EPA_STATE_PARAMETER = "/PRIMACY_AGENCY_CODE/";
    public static final String MIN_DATE = "/ENFDATE/>/01-JAN-14";
    public static final String CSV_FORMAT = "/Excel/";
    public static final String SPLITERATOR = "\",\"";
    public static final int TOTAL_CELL_COUNT = 47;

    private static final String AREA_PARAMETER = "area";
    private static final String ZIP = "zip";
    private static final String TOWN_PARAMATER = "town";
    private static final String STATE_PARAMATER = "state";

    private static final String ZIP_PARAMETER = "zip_code";

    public static final int DEFAULT_SCORE = 100;

    @Override
    public void init() {
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        try {
        String json = new Gson().toJson(
            retrieveSDWViolations(request.getParameter(TOWN_PARAMATER), request.getParameter(STATE_PARAMATER)));
        response.getWriter().write(json);
        } catch (IOException e){
            response.sendError(500);
            logger.error(e.getMessage());
        }
    }

    /**
    * A function that returns an arraylist of all the superfund sites in 
    * a given zip code
    * @param areaCode the zip code or state to pull data for
    * @param areaType the Zip or State format for the EPA URL
    * @return the list of superfund sites pulled from the EPA API
    */
    public ArrayList<WaterSystem> retrieveSDWViolations(String town, String state) throws IOException{
        URL url = new URL(EPA_WATERSYSTEM_LINK+EPA_CITY_PARAMETER+town.toUpperCase()+EPA_STATE_PARAMETER+state + CSV_FORMAT);
        return parseViolationsFromURL(url);
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

}