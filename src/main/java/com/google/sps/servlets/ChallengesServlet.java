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
import com.google.gson.*;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.JsonFactory;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;

import com.google.sps.data.Challenge;
import com.google.sps.data.ChallengeData;
import com.google.sps.data.GoogleIdHelper;
import com.google.sps.data.User;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javafx.util.Pair;

/** Servlet that manages challenges **/
@WebServlet("/challenges")
public class ChallengesServlet extends HttpServlet {
  private static final int NO_CHALLENGES = 0;
  private static final String NUM_CHALLENGES = "num-challenges";
  private static final String ID_TOKEN = "id_token";
  private ArrayList<Challenge> requested_challenge_list = new ArrayList<>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int num_challenges = getNumChallenges(request);
    Payload payload = GoogleIdHelper.verifyId(request);
    if (payload == null) {
        response.setStatus(400);
        return;
    }

    String user_id = payload.getSubject();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query = new Query(User.DATA_TYPE).setFilter(new FilterPredicate(User.ID, FilterOperator.EQUAL, user_id));
    Entity entity = datastore.prepare(query).asSingleEntity();
    User user = User.convertEntitytoUser(entity, user_id);

    HashMap<String, Integer> challenge_statuses = user.getChallengeStatuses();
    for(String key : challenge_statuses.keySet()){
      Challenge my_challenge = ChallengeData.CHALLENGES_MAP.get(key);
      requested_challenge_list.add(my_challenge);
    }

    String json = convertToJsonUsingGson(requested_challenge_list);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  /** Converts array of challenges into json format */
  private String convertToJsonUsingGson(ArrayList challenge_list){
    String json = new Gson().toJson(challenge_list);
    return json;
  }

  private int getNumChallenges(HttpServletRequest request){
    String num_of_challenges_requested = request.getParameter(NUM_CHALLENGES);

    //convert num_challenges to an int
    int num_challenges;
    try{
      num_challenges = Integer.parseInt(num_of_challenges_requested);
    } catch(NumberFormatException e){
        System.err.println("Could not convert to int " + num_of_challenges_requested);
        return NO_CHALLENGES;
    }

    if (num_challenges > ChallengeData.CHALLENGES_MAP.size()){
      return NO_CHALLENGES; 
    }
    return num_challenges;
  }
}