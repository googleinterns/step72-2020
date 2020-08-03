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
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.EntityNotFoundException;

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
import java.util.HashMap;

import javafx.util.Pair;

import com.google.sps.data.User;
import com.google.sps.data.GoogleIdHelper;

/** Servlet that returns events sorted by most recent timestamp */
@WebServlet("/user")
public class UserServlet extends HttpServlet {
  static final String CHALLENGE_ID_PARAM = "chal";
  static final String CHALLENGE_STATUS_PARAM = "stat";
  static final String BOOKMARKED_EVENT_PARAM = "book";
  static final String ADDED_TO_CALENDAR_PARAM = "add";
  static final String ADD_BOOKMARK_PARAM = "add";
  static final String EVENT = "Event";
  static final String EVENT_ID = "event_id";
  static final String BOOKMARKS = "bookmarks";
  static final String ID_TOKEN_PARAM = "id_token";
  static final String NAME = "name";
  static final String DEF_CURRENT_CHALLENGE_ID = "RECY_0";

  private static final HashMap DEF_CHALLENGES_AND_STATUSES = createMap();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Payload payload = GoogleIdHelper.verifyId(request);
    if (payload == null) {
        response.setStatus(400);
        return;
    }
    String userId = payload.getSubject();

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


 /** Creates User */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Payload payload = GoogleIdHelper.verifyId(request);
    if (payload == null) {
        response.setStatus(400);
        return;
    }
    String userId = payload.getSubject(); 
    String userNickname = (String) payload.get(NAME);

    User user = new User.Builder(userId)
        .setNickname(userNickname)
        .setCurrentChallengeId(DEF_CURRENT_CHALLENGE_ID)
        .setChallengeStatuses(DEF_CHALLENGES_AND_STATUSES)
        .build();

    datastore.put(user.toEntity());

    response.sendRedirect("/index.html");
  }

  /** Updates userinfo */
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
      String addBookmarkParam = request.getParameter(ADD_BOOKMARK_PARAM);

      String challengeId;
      Integer newStatus;
      Long eventId;
      
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Query query = new Query(User.DATA_TYPE).setFilter(new FilterPredicate(User.ID, FilterOperator.EQUAL, userId));
      Entity entity = datastore.prepare(query).asSingleEntity();
      User user = User.convertEntitytoUser(entity, userId);

      if (challengeIdParam != null) {
          try {
              if (statusParam != null) {
                  newStatus = Integer.parseInt(statusParam);
                  updateChallengeStatus(user, challengeIdParam, newStatus);
              } else {
                  updateCurrentChallenge(user, challengeIdParam);
              }
          } catch (Exception e) {
              System.err.println(e.getMessage());
          }
      } 
      else if (bookmarkedEventParam != null) {
          try {
              eventId = Long.parseLong(bookmarkedEventParam);
              if (addBookmarkParam.equals("true")) addBookmark(user, eventId);
              else if (addBookmarkParam.equals("false")) removeBookmark(user, eventId);
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

  private void updateCurrentChallenge(User user, String id) {
      user.setCurrentChallenge(id);
  }


  private void updateChallengeStatus(User user, String id, int status) {
    HashMap<String, Integer> challengeStatuses = user.getChallengeStatuses();
    challengeStatuses.put(id, status);
    user.setChallengeStatuses(challengeStatuses);
  }

  private void addBookmark(User user, Long eventId) {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      ArrayList<Long> bookmarkedEvents = user.getBookmarkedEvents();
      if (bookmarkedEvents == null) bookmarkedEvents = new ArrayList<Long>();
      bookmarkedEvents.add(eventId);
      user.setBookmarkedEvents(bookmarkedEvents);
      Entity entity;
      try {
          entity = datastore.get(KeyFactory.createKey(EVENT, eventId));
      } catch (EntityNotFoundException e) {
          System.err.println(e.getMessage());
          return;
      }
      entity.setProperty(BOOKMARKS, (long) entity.getProperty(BOOKMARKS)+1);
      datastore.put(entity);
  }

  private void removeBookmark(User user, Long eventId) {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      ArrayList<Long> bookmarkedEvents = user.getBookmarkedEvents();
      bookmarkedEvents.remove(eventId);
      user.setBookmarkedEvents(bookmarkedEvents);

      Entity entity;
      try {
          entity = datastore.get(KeyFactory.createKey(EVENT, eventId));
      } catch (EntityNotFoundException e) {
          System.err.println(e.getMessage());
          return;
      }
      entity.setProperty(BOOKMARKS, (long) entity.getProperty(BOOKMARKS)-1);
      datastore.put(entity);
  }

  private void updateAddedToCalendarEvents(User user, Long eventId) {
      ArrayList<Long> addedEvents = user.getAddedToCalendarEvents();
      if (addedEvents == null) addedEvents = new ArrayList<Long>();
      addedEvents.add(eventId);
      user.setAddedToCalendarEvents(addedEvents);
  }

  private static HashMap<String, Integer> createMap(){
    HashMap<String,Integer> def_chal_map = new HashMap<String, Integer>();
    def_chal_map.put("GARD_0",0);
    def_chal_map.put("RECY_0",0);
    def_chal_map.put("WAST_0",0);
    return def_chal_map;
 }
} 