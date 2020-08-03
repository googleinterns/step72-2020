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

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.JsonFactory;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;

import com.google.sps.data.Badge;
import com.google.sps.data.BadgeData;
import com.google.sps.data.Challenge;
import com.google.sps.data.ChallengeData;
import com.google.sps.data.GoogleIdHelper;
import com.google.sps.data.IdHelper;
import com.google.sps.data.User;

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

/** Servlet that manages badges **/
@WebServlet("/badges")
public class BadgeServlet extends HttpServlet {
  private static final int NO_CHALLENGES = 0;
  private static final String COMPLETED_CHALLENGE_TYPE ="challenge-type";
  
  private IdHelper id_helper = new GoogleIdHelper();
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  
  /** grabs users badge data */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String user_id = id_helper.getUserId(request);
    if (user_id == null) {
        response.setStatus(400);
        return;
    }

    Query query = new Query(User.DATA_TYPE).setFilter(new FilterPredicate(User.ID, FilterOperator.EQUAL, user_id));
    Entity entity = datastore.prepare(query).asSingleEntity();
    User user = User.convertEntityToUser(entity, user_id);


    HashSet<String> earned_badges = user.getEarnedBadges();
    ArrayList<Badge> requested_badges = new ArrayList<>();
    for(String badge_id : earned_badges){
      Badge badge = BadgeData.BADGE_MAP.get(badge_id);
      requested_badges.add(badge);
    }
    
    String json = convertToJsonUsingGson(requested_badges);
    //System.out.println(json);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  /* Update user's badge data and returns indication if a new badge was added*/
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Challenge.Type completed_challenge_type = Challenge.Type.valueOf(request.getParameter(COMPLETED_CHALLENGE_TYPE));
    String user_id = id_helper.getUserId(request);
    if (user_id == null) {
        response.setStatus(400);
        return;
    }

    Query query = new Query(User.DATA_TYPE).setFilter(new FilterPredicate(User.ID, FilterOperator.EQUAL, user_id));
    Entity entity = datastore.prepare(query).asSingleEntity();
    User user = User.convertEntityToUser(entity, user_id);

    HashSet<String> earned_badges = user.getEarnedBadges();
    HashSet<String> completed_challenges = user.getCompletedChallenges();
    boolean new_badge_earned;

    int num_cmpl_chal_type = 0;
    for(String chal_id : completed_challenges){
      if(ChallengeData.CHALLENGES_MAP.get(chal_id).getType() == completed_challenge_type){
        num_cmpl_chal_type++;
      }
    }
    
    new_badge_earned = addNewBadgesToSet(user,completed_challenge_type ,num_cmpl_chal_type);
    datastore.put(user.toEntity());

    response.setContentType("application/json; charset=UTF-8");
    response.getWriter().println(new Gson().toJson(new_badge_earned));
  }

  /**Add new badges to HashSet*/
  private boolean addNewBadgesToSet(User user, Challenge.Type challenge_type, int num_cmpl_chal) {
    HashSet<String> earned_badges = user.getEarnedBadges();
    boolean new_badge_earned = false;
    if(num_cmpl_chal >= 1) {
      switch(challenge_type){
        case GARDENING:
          new_badge_earned = !earned_badges.contains(BadgeData.GARD_BADG_1);
          earned_badges.add(BadgeData.GARD_BADG_1);
          break;
        case RECYCLE:
          new_badge_earned = !earned_badges.contains(BadgeData.RECY_BADG_1);
          earned_badges.add(BadgeData.RECY_BADG_1);
          break;
        case WASTE:
          new_badge_earned = !earned_badges.contains(BadgeData.WAST_BADG_1);
          earned_badges.add(BadgeData.WAST_BADG_1);
          break;
        default:
          break;
      }

    } else if (num_cmpl_chal >= 3) {
        switch(challenge_type){
          case GARDENING:
            new_badge_earned = !earned_badges.contains(BadgeData.GARD_BADG_3);
            earned_badges.add(BadgeData.GARD_BADG_3);
            break;
          case RECYCLE:
            new_badge_earned = !earned_badges.contains(BadgeData.RECY_BADG_3);
            earned_badges.add(BadgeData.RECY_BADG_3);
            break;
          case WASTE:
            new_badge_earned = !earned_badges.contains(BadgeData.WAST_BADG_3);
            earned_badges.add(BadgeData.WAST_BADG_3);
            break;
          default:
            break;
        }  
    }    
    user.setEarnedBadges(earned_badges);
    return new_badge_earned;
  }

  /** Converts array of challenges into json format */
  private String convertToJsonUsingGson(ArrayList badge_list) {
    String json = new Gson().toJson(badge_list);
    return json;
  }
}