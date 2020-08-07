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
import com.google.sps.data.IdHelper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/** Servlet that manages challenges **/
@WebServlet("/challenges")
public class ChallengesServlet extends HttpServlet {
  public static final int NO_CHALLENGES = 0;
  public static final String NUM_CHALLENGES = "num-challenges";
  public static final String ID_TOKEN = "id_token";
  public static final String COMPLETED_CHALLENGE = "completed-chal";
  public static final String CURRENT_CHALLENGE = "current-chal";

  private IdHelper id_helper = new GoogleIdHelper();
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  
  public void setIdHelper(IdHelper id_helper) {
    this.id_helper = id_helper;
  }

  public void setDatastoreService(DatastoreService service) {
    this.datastore = service;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int num_challenges = getNumChallenges(request);
    String user_id = id_helper.getUserId(request);
    if (user_id == null) {
      response.setStatus(400);
      return;
    }

    Query query = new Query(User.DATA_TYPE).setFilter(new FilterPredicate(User.ID, FilterOperator.EQUAL, user_id));
    Entity entity = datastore.prepare(query).asSingleEntity();
    if(entity == null){
      response.setStatus(404);
      return;
    }
    User user = User.convertEntityToUser(entity, user_id);


    HashMap<String, Integer> challenge_statuses = user.getChallengeStatuses();
    ArrayList<Challenge> requested_challenge_list = new ArrayList<>();
    for(String key : challenge_statuses.keySet()){
      Challenge my_challenge = ChallengeData.CHALLENGES_MAP.get(key);
      requested_challenge_list.add(my_challenge);
    }

    String json = convertToJsonUsingGson(requested_challenge_list);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  /* add new challenge id to user status map, update current challenge id
     and add challenge id to user completed challenge map */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String completed_challenge_id = request.getParameter(COMPLETED_CHALLENGE);
    String current_challenge_id = request.getParameter(CURRENT_CHALLENGE);
    String user_id = id_helper.getUserId(request);
    if (user_id == null) {
      response.setStatus(400);
      return;
    }

    Query query = new Query(User.DATA_TYPE).setFilter(new FilterPredicate(User.ID, FilterOperator.EQUAL, user_id));
    Entity entity = datastore.prepare(query).asSingleEntity();
    User user = User.convertEntityToUser(entity, user_id);

    if(completed_challenge_id != null){
      try{
        updateUserChallenges(user, completed_challenge_id, current_challenge_id);
        } catch(Exception e) {
          System.err.println(e.getMessage());
      }
    }
    datastore.put(user.toEntity());

    response.setContentType("application/json; charset=UTF-8");
    response.getWriter().println(user.toJSON());
  }

  /** Converts array of challenges into json format */
  private String convertToJsonUsingGson(ArrayList challenge_list) {
    String json = new Gson().toJson(challenge_list);
    return json;
  }

  //This function Update's User's Current, and Completed challenges
  // along with statuses
  private void updateUserChallenges(User user, String compl_id, String cur_id) {
    HashMap<String, Integer> challenge_statuses = user.getChallengeStatuses();
    HashSet<String> completed_challenges = user.getCompletedChallenges();
    user.setCurrentChallenge(cur_id);
    user.appendToCompletedChallenges(compl_id);
    
    //add new challenge to challenge status.
    for(String challenge_id : ChallengeData.CHALLENGES_MAP.keySet()){
      if(!completed_challenges.contains(challenge_id) &&
         !challenge_statuses.containsKey(challenge_id)){

          challenge_statuses.put(challenge_id, 0);
          user.setChallengeStatuses(challenge_statuses);
          break;
      }   
    }
  } 

  /** Convert request parameter NUM_CHALLENGES into an Integeer */
  private int getNumChallenges(HttpServletRequest request){
    String num_of_challenges_requested = request.getParameter(NUM_CHALLENGES);
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