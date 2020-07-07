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

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    String userId = userService.getCurrentUser().getUserId();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query = new Query(UserInfo.DATA_TYPE).setFilter(new FilterPredicate("userId", FilterOperator.EQUAL, userId));
    Entity entity = datastore.prepare(query).asSingleEntity();

    String nickname = (String) entity.getProperty(UserInfo.NICKNAME);
    int currentChallengeId = (int) entity.getProperty(UserInfo.CURRENT_CHALLENGE);
    ArrayList<Long> createdEvents =(ArrayList<Long>) entity.getProperty(UserInfo.CREATED_EVENTS);
    ArrayList<Long> bookmarkedEvents = (ArrayList<Long>) entity.getProperty(UserInfo.BOOKMARKED_EVENTS);
    ArrayList<Integer> challengeStatuses = (ArrayList<Integer>) entity.getProperty(UserInfo.CHALLENGE_STATUSES);

    System.out.println(challengeStatuses); 

    UserInfo user = new UserInfo(userId, nickname, createdEvents, bookmarkedEvents, currentChallengeId, challengeStatuses);
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

      ArrayList<Integer> chal = new ArrayList<Integer>();
      chal.add(1); chal.add(2); chal.add(3);

      Entity userEntity = new Entity(UserInfo.DATA_TYPE);
      userEntity.setProperty(UserInfo.ID, userId);
      userEntity.setProperty(UserInfo.NICKNAME, userNickname);
      userEntity.setProperty(UserInfo.CREATED_EVENTS, new ArrayList<Long>());
      userEntity.setProperty(UserInfo.BOOKMARKED_EVENTS, new ArrayList<Long>());
      userEntity.setProperty(UserInfo.CURRENT_CHALLENGE, 0);
      userEntity.setProperty(UserInfo.CHALLENGE_STATUSES, chal);

      datastore.put(userEntity);

      response.sendRedirect("/feed.html");
  }

  private String convertToJson(UserInfo user) {
      Gson gson = new Gson();
      String json = gson.toJson(user);
      return json;
  }
}