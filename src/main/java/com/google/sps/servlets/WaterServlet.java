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


import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.ThreadManager;
import java.io.PrintWriter;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Scanner;
import java.lang.Thread;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.stream.*;
import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.*;

// import com.google.sps.data.WaterSystem;

/** Servlet that returns a list of all Superfund Sites */
@WebServlet("/water")
public class WaterServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(WaterServlet.class);

    private HashMap<String, Integer> totalPopExposed = new HashMap<>();
    
    private static final String CSV_FORMAT = "/Excel/";
    private static final String SPLITERATOR = "\",\"";

    private static final String AREA_PARAMETER = "area";
    private static final String ZIP = "zip";
    private static final String STATE = "state";

    private static final String ZIP_PARAMETER = "zip_code";


    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String area = request.getParameter(AREA_PARAMETER);
        response.setContentType("application/json");
        if(area.equals(ZIP)){
            String json = retrieveZipData(request.getParameter(ZIP_PARAMETER));
            response.getWriter().println(json);
        } else if(area.equals(STATE)) {
        }
    }

    private String retrieveZipData(String zipCodeAsString){
        // int zipCode;

        // try {
        //     zipCode = Integer.parseInt(zipCodeAsString);
        // } catch (Exception e){
        //     logger.severe("Zip code "+zipCodeAsString+" is not an integer");
        //     return "{}";
        // }

        // try {
        //     URL url = new URL(EPA_API_LINK + zipCodeAsString + CSV_FORMAT);
        //     Scanner scanner = new Scanner(url.openStream());
        //     scanner.nextLine();
        //     while(scanner.hasNextLine()){
        //         String cells[] = scanner.nextLine().split(SPLITERATOR);
        //     }
        // } catch (Exception e){
        //     logger.error(e.getMessage());
        // }
        return "{}";
    }

}