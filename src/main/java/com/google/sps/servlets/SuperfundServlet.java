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
import java.util.Scanner;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.sps.data.SuperfundSite;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Servlet that returns a list of all Superfund Sites */
@WebServlet("/superfund")
public class SuperfundServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SuperfundServlet.class);
    
    public static final String EPA_API_LINK = "https://enviro.epa.gov/enviro/efservice/SEMS_ACTIVE_SITES/";
    public static final String EPA_ZIP_FORMAT = "SITE_ZIP_CODE/";
    public static final String EPA_STATE_FORMAT = "SITE_STATE/";
    public static final String CSV_FORMAT = "/Excel/";
    public static final String SPLITERATOR = "\",\"";
    public static final int TOTAL_CELL_COUNT = 18;

    private static final String AREA_PARAMETER = "area";
    private static final String ZIP = "zip";
    private static final String STATE = "state";

    private static final String NAME_COLUMN = "SEMS_ACTIVE_SITES.SITE_NAME";
    private static final String ID_COLUMN = "SEMS_ACTIVE_SITES.SITE_ID";
    private static final String STATE_COLUMN = "SEMS_ACTIVE_SITES.SITE_STATE";
    private static final String CITY_COLUMN = "SEMS_ACTIVE_SITES.SITE_CITY_NAME";
    private static final String COUNTY_COLUMN = "SEMS_ACTIVE_SITES.SITE_CNTY_NAME";
    private static final String STATUS_COLUMN = "SEMS_ACTIVE_SITES.NPL";
    private static final String LATITUDE_COLUMN = "SEMS_ACTIVE_SITES.LATITUDE";
    private static final String LONGITUDE_COLUMN = "SEMS_ACTIVE_SITES.LONGITUDE";


    private static final String ZIP_PARAMETER = "zip_code";

    public static final int DEFAULT_HAZARD_SCORE = 100;

    @Override
    public void init() {
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String area = request.getParameter(AREA_PARAMETER);
        response.setContentType("application/json");
        if(area.equals(ZIP)){
            String json = new Gson().toJson(
                retrieveSuperfundData(request.getParameter(ZIP_PARAMETER), EPA_ZIP_FORMAT));
            response.getWriter().write(json);
        } else if(area.equals(STATE)) {
            String json = new Gson().toJson(
                retrieveSuperfundData(request.getParameter(ZIP_PARAMETER), EPA_STATE_FORMAT));
            response.getWriter().write(json);
        }
    }

    /**
    * A function that returns an arraylist of all the superfund sites in 
    * a given zip code
    * @param areaCode the zip code or state to pull data for
    * @param areaType the Zip or State format for the EPA URL
    * @return the list of superfund sites pulled from the EPA API
    */
    public ArrayList<SuperfundSite> retrieveSuperfundData(String areaCode, String areaType){
        ArrayList<SuperfundSite> sites = new ArrayList<>();
        try {
            URL url = new URL(EPA_API_LINK + areaType + areaCode + CSV_FORMAT);
            sites = parseSuperfundsFromURL(url);
        } catch (IOException e){
            logger.error(e.getMessage());
        }

        //to be stored in database
        ArrayList<SuperfundSite> invalidSites = cleanSuperfundData(sites);
        
        return sites;
    }

    public ArrayList<SuperfundSite> parseSuperfundsFromURL(URL url) throws IOException{
        ArrayList<SuperfundSite> sites = new ArrayList<>();

        CSVParser csvParser = CSVParser.parse(url, Charset.defaultCharset(),
        CSVFormat.EXCEL.withFirstRecordAsHeader());
        for (CSVRecord csvRecord : csvParser.getRecords()) {
            String name = csvRecord.get(NAME_COLUMN);
            int id = Integer.parseInt(csvRecord.get(ID_COLUMN));
            String state = csvRecord.get(STATE_COLUMN);
            String city = csvRecord.get(CITY_COLUMN);
            String county = csvRecord.get(COUNTY_COLUMN);
            String status = csvRecord.get(STATUS_COLUMN);
            double latitude, longitude;
            try {
                latitude = Double.parseDouble(csvRecord.get(LATITUDE_COLUMN));
                longitude = Double.parseDouble(csvRecord.get(LONGITUDE_COLUMN));
            } catch (Exception e){
                // logger.info("Lat Long Issue for "+name +"\n The line of issue is: "+cells.toString());
                latitude = 0;
                longitude = 0;
            }
            SuperfundSite site = new SuperfundSite(name, id, state, city, county, status, latitude, longitude);
            sites.add(site);
        }
        csvParser.close();
        return sites;
    }

    /**
     * Removes invalid superfund sites from the given list and returns them as their own list
     */
    public ArrayList<SuperfundSite> cleanSuperfundData(final ArrayList<SuperfundSite> sites){
        ArrayList<SuperfundSite> invalidSites = new ArrayList<>();
        for(int i = 0; i < sites.size();){
            if(sites.get(i).isValidSite()){
                i++;
            } else {
                SuperfundSite invalid = sites.remove(i);
                invalidSites.add(invalid);
            }
        }
        return invalidSites;
    }

}