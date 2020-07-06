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

@WebServlet("/login-status")
public class LoginStatusServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      System.out.println(userEmail);
      String urlToRedirectToAfterUserLogsOut = "/feed.html";
      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);

      List<String> res = new ArrayList<String>();
      res.add("true");
      res.add(logoutUrl);

      String json = convertToJson(res);
      response.getWriter().println(json);
    } else {
      String urlToRedirectToAfterUserLogsIn = "/feed.html";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);

      List<String> res = new ArrayList<String>();
      res.add("false");
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
    json += "\"url\": ";
    json += "\"" + info.get(1) + "\"";
    json += "}";
    return json;
  }
}


