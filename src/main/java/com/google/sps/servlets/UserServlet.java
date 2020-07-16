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

import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.User;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.sps.data.UserInfo;

/** Servlet that returns events sorted by most recent timestamp */
@WebServlet("/user")
public class UserServlet extends HttpServlet {

  static final String CHALLENGE_ID_PARAM = "chal";
  static final String CHALLENGE_STATUS_PARAM = "stat";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    
    String userId = userService.getCurrentUser().getUserId();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query = new Query(UserInfo.DATA_TYPE).setFilter(new FilterPredicate("userId", FilterOperator.EQUAL, userId));
    Entity entity = datastore.prepare(query).asSingleEntity();

    if (entity == null) {
        doPost(request, response);
        entity = datastore.prepare(query).asSingleEntity();
    }

    UserInfo user = convertEntitytoUserInfo(entity, userId);

    response.setContentType("application/json; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");

    String json = convertToJson(user);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      UserService userService = UserServiceFactory.getUserService();
      String userId = userService.getCurrentUser().getUserId();
      String userNickname = request.getParameter(UserInfo.NICKNAME);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService(); 

      Long currentChallengeId = 0L;
      ArrayList<Integer> challengeStatuses = new ArrayList<Integer>(Collections.nCopies(3, 0));

      Entity userEntity = new Entity(UserInfo.DATA_TYPE);
      userEntity.setProperty(UserInfo.ID, userId);
      userEntity.setProperty(UserInfo.NICKNAME, userNickname);
      userEntity.setProperty(UserInfo.CREATED_EVENTS, Collections.emptyList());
      userEntity.setProperty(UserInfo.BOOKMARKED_EVENTS, Collections.emptyList());
      userEntity.setProperty(UserInfo.CURRENT_CHALLENGE, currentChallengeId);
      userEntity.setProperty(UserInfo.CHALLENGE_STATUSES, challengeStatuses);

      datastore.put(userEntity);

      response.sendRedirect("/feed.html");
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
      UserService userService = UserServiceFactory.getUserService();
      String userId = userService.getCurrentUser().getUserId();

      String challengeIdParam = request.getParameter(CHALLENGE_ID_PARAM);
      String statusParam = request.getParameter(CHALLENGE_STATUS_PARAM);

      Long challengeId = -1L;
      Integer newStatus = -1;

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService(); 

      Query query = new Query(UserInfo.DATA_TYPE).setFilter(new FilterPredicate("userId", FilterOperator.EQUAL, userId));
      Entity entity = datastore.prepare(query).asSingleEntity();

      if (challengeIdParam != null) {
          try {
              challengeId = Long.parseLong(challengeIdParam);
              if (statusParam != null) {
                  newStatus = Integer.parseInt(statusParam);
                  updateChallengeStatus(entity, challengeId, newStatus);
              } else {
                  updateCurrentChallenge(entity, challengeId);
              }
          } catch (Exception e) {
              System.err.println(e.getMessage());
          }
      }

      datastore.put(entity);

      UserInfo user = convertEntitytoUserInfo(entity, userId);

      response.setContentType("application/json; charset=UTF-8");
      response.setCharacterEncoding("UTF-8");

      String json = convertToJson(user);
      response.getWriter().println(json);
  }

  // @Erick If challenge id structure is changed, update this method
  private void updateCurrentChallenge(Entity entity, Long id) {
      entity.setProperty(UserInfo.CURRENT_CHALLENGE, id);
  }
  
  // @Erick If challenge id structure is changed, update this method
  private void updateChallengeStatus(Entity entity, Long id, int status) {
      ArrayList<Integer> challengeStatuses = (ArrayList<Integer>) entity.getProperty(UserInfo.CHALLENGE_STATUSES);
      challengeStatuses.set(id.intValue(), status);
      entity.setProperty(UserInfo.CHALLENGE_STATUSES, challengeStatuses);
  }

  private String convertToJson(UserInfo user) {
      Gson gson = new Gson();
      String json = gson.toJson(user);
      return json;
  }

  private UserInfo convertEntitytoUserInfo(Entity entity, String userId) {
    String nickname = (String) entity.getProperty(UserInfo.NICKNAME);
    Long currentChallengeId = (Long) entity.getProperty(UserInfo.CURRENT_CHALLENGE);
    ArrayList<Long> createdEvents =(ArrayList<Long>) entity.getProperty(UserInfo.CREATED_EVENTS);
    ArrayList<Long> bookmarkedEvents = (ArrayList<Long>) entity.getProperty(UserInfo.BOOKMARKED_EVENTS);
    ArrayList<Integer> challengeStatuses = (ArrayList<Integer>) entity.getProperty(UserInfo.CHALLENGE_STATUSES);

    UserInfo user = new UserInfo(userId, nickname, createdEvents, bookmarkedEvents, currentChallengeId, challengeStatuses);
    return user;
  }
} 