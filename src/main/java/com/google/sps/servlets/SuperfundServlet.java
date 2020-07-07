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
import java.util.Scanner;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.sps.data.SuperfundSite;

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
        Scanner scanner= new Scanner(url.openStream());
        scanner.nextLine();
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            //cleans data to ignore new line characters within a cell
            while(scanner.hasNextLine() && line.split(SPLITERATOR).length < TOTAL_CELL_COUNT){
                line += scanner.nextLine();
            }
            String cells[] = line.split(SPLITERATOR);
            if(cells.length < TOTAL_CELL_COUNT) continue;
            String name = cells[3];
            double score = DEFAULT_HAZARD_SCORE;
            String state = cells[7];
            String city = cells[6];
            String county = cells[10];
            String status = cells[15];
            double lattitude, longitude;
            try {
                lattitude = Double.parseDouble(cells[12]);
                longitude = Double.parseDouble(cells[13]);
            } catch (Exception e){
                // logger.info("Lat Long Issue for "+name +"\n The line of issue is: "+cells.toString());
                lattitude = 0;
                longitude = 0;
            }
            SuperfundSite site = new SuperfundSite(name, score, state, city, county, status, lattitude, longitude);
            sites.add(site);
        }
        scanner.close();
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