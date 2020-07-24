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
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.sps.data.User;
import com.google.sps.data.GoogleIdHelper;

/** Servlet that returns events sorted by most recent timestamp */
@WebServlet("/user")
public class UserServlet extends HttpServlet {

  static final String CHALLENGE_ID_PARAM = "chal";
  static final String CHALLENGE_STATUS_PARAM = "stat";
  static final String BOOKMARKED_EVENT_PARAM = "book";
  static final String ADDED_TO_CALENDAR_PARAM = "add";
  static final String ID_TOKEN_PARAM = "id_token";
  static final String NAME = "name";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Payload payload = GoogleIdHelper.verifyId(request);
    if (payload == null) {
        response.setStatus(400);
        return;
    }
    String userId = payload.getSubject();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query = new Query(User.DATA_TYPE).setFilter(new FilterPredicate(User.ID, FilterOperator.EQUAL, userId));
    Entity entity = datastore.prepare(query).asSingleEntity();

    if (entity == null) {
        response.setStatus(404);
        return;
    }

    User user = User.convertEntitytoUser(entity, userId);

    response.setContentType("application/json; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");

    response.getWriter().println(user.toJSON());
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Payload payload = GoogleIdHelper.verifyId(request);
    if (payload == null) {
        response.setStatus(400);
        return;
    }
    String userId = payload.getSubject();
    String userNickname = (String) payload.get(NAME);
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService(); 

    Long currentChallengeId = 0L;
    // @Erick Change to get length of challenges (replace the 3)
    ArrayList<Integer> challengeStatuses = new ArrayList<Integer>(Collections.nCopies(3, 0));

    User user = new User.Builder(userId)
        .setNickname(userNickname)
        .setCurrentChallengeId(currentChallengeId)
        .setChallengeStatuses(challengeStatuses)
        .build();

    datastore.put(user.toEntity());

    response.sendRedirect("/index.html");
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
      Payload payload = GoogleIdHelper.verifyId(request);
      if (payload == null) {
        response.setStatus(400);
        return;
      }
      String userId = payload.getSubject();

      String challengeIdParam = request.getParameter(CHALLENGE_ID_PARAM);
      String statusParam = request.getParameter(CHALLENGE_STATUS_PARAM);
      String bookmarkedEventParam = request.getParameter(BOOKMARKED_EVENT_PARAM);
      String addedToCalendarParam = request.getParameter(ADDED_TO_CALENDAR_PARAM);

      Long challengeId;
      Integer newStatus;
      Long eventId;

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService(); 

      Query query = new Query(User.DATA_TYPE).setFilter(new FilterPredicate(User.ID, FilterOperator.EQUAL, userId));
      Entity entity = datastore.prepare(query).asSingleEntity();
      User user = User.convertEntitytoUser(entity, userId);

      if (challengeIdParam != null) {
          try {
              challengeId = Long.parseLong(challengeIdParam);
              if (statusParam != null) {
                  newStatus = Integer.parseInt(statusParam);
                  updateChallengeStatus(user, challengeId, newStatus);
              } else {
                  updateCurrentChallenge(user, challengeId);
              }
          } catch (Exception e) {
              System.err.println(e.getMessage());
          }
      } 
      else if (bookmarkedEventParam != null) {
          try {
              eventId = Long.parseLong(bookmarkedEventParam);
              updateBookmarkedEvents(user, eventId);
          } catch (Exception e) {
              System.err.println(e.getMessage());
          }
      }
      else if (addedToCalendarParam != null) {
          try {
              eventId = Long.parseLong(addedToCalendarParam);
              updateAddedToCalendarEvents(user, eventId);
          } catch (Exception e) {
              System.err.println(e.getMessage());
          }
      }

      datastore.put(user.toEntity());

      response.setContentType("application/json; charset=UTF-8");
      response.setCharacterEncoding("UTF-8");

      response.getWriter().println(user.toJSON());
  }

  // @Erick If challenge id structure changes, update this method
  private void updateCurrentChallenge(User user, Long id) {
      user.setCurrentChallenge(id);
  }

  // @Erick If challenge status structure changes, update this method
  private void updateChallengeStatus(User user, Long id, int status) {
      ArrayList<Integer> challengeStatuses = user.getChallengeStatuses();
      challengeStatuses.set(id.intValue(), status);
      user.setChallengeStatuses(challengeStatuses);

  }

  private void updateBookmarkedEvents(User user, Long eventId) {
      ArrayList<Long> bookmarkedEvents = user.getBookmarkedEvents();
      if (bookmarkedEvents == null) bookmarkedEvents = new ArrayList<Long>();
      bookmarkedEvents.add(eventId);
      user.setBookmarkedEvents(bookmarkedEvents);
  }

  private void updateAddedToCalendarEvents(User user, Long eventId) {
      ArrayList<Long> addedEvents = user.getAddedToCalendarEvents();
      if (addedEvents == null) addedEvents = new ArrayList<Long>();
      addedEvents.add(eventId);
      user.setAddedToCalendarEvents(addedEvents);
  }

  

} 