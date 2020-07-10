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

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;

import com.google.sps.data.UserInfo;

@WebServlet("/login-status")
public class LoginStatusServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    UserService userService = UserServiceFactory.getUserService();
    List<String> res = new ArrayList<String>();

    if (userService.isUserLoggedIn()) {
      res.add("true");
      String userId = userService.getCurrentUser().getUserId();

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

      Query query = new Query(UserInfo.DATA_TYPE).setFilter(new FilterPredicate("userId", FilterOperator.EQUAL, userId));

      Entity result = datastore.prepare(query).asSingleEntity();
      

      // sends whether user is a returning user so can get info to create user if necessary
      if (result == null) {
          System.out.println("Not returning user");
          res.add("false");
      }
      else {
          System.out.println("Returning user");
          res.add("true");
      }

      String urlToRedirectToAfterUserLogsOut = "/feed.html";
      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
      
      res.add(logoutUrl);

      String json = convertToJson(res);
      response.getWriter().println(json);
    } else {
      String urlToRedirectToAfterUserLogsIn = "/feed.html";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);

      res.add("false");
      res.add("n/a");
      res.add(loginUrl);

      String json = convertToJson(res);
      response.getWriter().println(json);
    }
  }
  private String convertToJson(List<String> info) {
    String json = "{";
    json += "\"loggedIn\": ";
    json += "\"" + info.get(0) + "\"";
    json += ", ";
    json += "\"returningUser\": ";
    json += "\"" + info.get(1) + "\"";
    json += ", ";
    json += "\"url\": ";
    json += "\"" + info.get(2) + "\"";
    json += "}";
    return json;
  }


}


