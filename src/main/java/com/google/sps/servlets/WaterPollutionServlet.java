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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.sps.data.WaterSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Servlet that returns a list of all Superfund Sites */
@WebServlet("/water")
public class WaterPollutionServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(WaterPollutionServlet.class);
    
    public static final String EPA_API_LINK = "https://enviro.epa.gov/enviro/efservice/SDW_CONTAM_VIOL_ZIP/";
    public static final String EPA_ZIP_FORMAT = "GEOLOCATION_ZIP/";
    public static final String EPA_STATE_FORMAT = "STATE/";
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
        String area = request.getParameter(AREA_PARAMETER);
        response.setContentType("application/json");
        // if(area.equals(ZIP)){
        //     String json = new Gson().toJson(
        //         retrieveSDWViolations(request.getParameter(ZIP_PARAMETER), EPA_ZIP_FORMAT));
        //     response.getWriter().write(json);
        // } else if(area.equals(STATE)) {
        //     String json = new Gson().toJson(
        //         retrieveSDWViolations(request.getParameter(ZIP_PARAMETER), EPA_STATE_FORMAT));
        //     response.getWriter().write(json);
        // }
        String json = new Gson().toJson(
            retrieveSDWViolations(request.getParameter(TOWN_PARAMATER), request.getParameter(STATE_PARAMATER)));
        response.getWriter().write(json);
    }

    /**
    * A function that returns an arraylist of all the superfund sites in 
    * a given zip code
    * @param areaCode the zip code or state to pull data for
    * @param areaType the Zip or State format for the EPA URL
    * @return the list of superfund sites pulled from the EPA API
    */
    public ArrayList<WaterSystem> retrieveSDWViolations(String town, String state){
        ArrayList<WaterSystem> waterSystems;
        try {
            URL url = new URL("https://enviro.epa.gov/enviro/efservice/WATER_SYSTEM/"+"CITIES_SERVED/CONTAINS/"+town.toUpperCase()+"/PRIMACY_AGENCY_CODE/"+state + CSV_FORMAT);
            // URL url = new URL(EPA_API_LINK + areaType + areaCode + MIN_DATE + CSV_FORMAT);
            waterSystems = parseViolationsFromURL(url);
        } catch (IOException e){
            logger.error(e.getMessage());
            waterSystems = new ArrayList<>();
            System.out.println("IO Exception for Water Violations");
        }
        
        return waterSystems;
    }

    public ArrayList<WaterSystem> parseViolationsFromURL(URL url) throws IOException{
        ArrayList<WaterSystem> waterSystems = new ArrayList<>();
        Scanner scanner= new Scanner(url.openStream());
        scanner.nextLine();
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            //cleans data to ignore new line characters within a cell
            while(scanner.hasNextLine() && line.split(SPLITERATOR).length < TOTAL_CELL_COUNT){
                line += scanner.nextLine();
            }
            String[] cells = line.split(SPLITERATOR);
            if(cells.length < TOTAL_CELL_COUNT) continue;
            WaterSystem system = new WaterSystem(cells);
            system.addViolations();
            waterSystems.add(system);
        }
        scanner.close();
        return waterSystems;
    }

}