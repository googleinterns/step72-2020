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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Event.ExtendedProperties;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.*; 
import java.util.TimeZone;

/** Servlet that returns events sorted by most recent timestamp */
@WebServlet("/events")
public class EventsServlet extends HttpServlet {

    static final String EVENT = "Event";
    static final String TIMESTAMP = "timestamp";
    static final String SUMMARY = "summary";
    static final String DESCRIPTION = "description";
    static final String LOCATION = "location";
    static final String DATETIME = "date_time";
    static final String CATEGORY = "category";
    static final String UTC_TIMEZONE = "UTC";
    static final String DATE = "date";
    static final String START_TIME = "start";
    static final String END_TIME = "end";
    static final String USER_TIMEZONE = "timezone";

    static final List<String> CATEGORIES = new ArrayList<String>(
        Arrays.asList("food_beverage", "nature", "water", "waste_cleanup", "other")
    );

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);
    Query query = new Query(EVENT).addSort(TIMESTAMP, SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery pq = datastore.prepare(query);

    QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);

    List<Event> events = new ArrayList<>();
    for (Entity entity : results) {
        long timestamp = (long) entity.getProperty(TIMESTAMP);
        String summary = (String) entity.getProperty(SUMMARY);
        String description = (String) entity.getProperty(DESCRIPTION);
        String location = (String) entity.getProperty(LOCATION);
        Date startTime = (Date) entity.getProperty(START_TIME);
        Date endTime = (Date) entity.getProperty(END_TIME);
        String category = (String) entity.getProperty(CATEGORY);

        DateTime startDateTime = new DateTime(startTime);
        EventDateTime start = new EventDateTime()
            .setDateTime(startDateTime)
            .setTimeZone(UTC_TIMEZONE);
        DateTime endDateTime = new DateTime(endTime);
        EventDateTime end = new EventDateTime()
            .setDateTime(endDateTime)
            .setTimeZone(UTC_TIMEZONE);
        Event event = new Event()
            .setSummary(summary)
            .setLocation(location)
            .setDescription(description)
            .setStart(start)
            .setEnd(end);

        ExtendedProperties ep = new ExtendedProperties();
        ep.set(CATEGORY, category);
        event.setExtendedProperties(ep);
    
        events.add(event);
    }

    response.setContentType("application/json; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");

    String json = convertToJson(events);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      String eventSummary = request.getParameter(SUMMARY);
      String eventDescription = request.getParameter(DESCRIPTION);
      String eventLocation = request.getParameter(LOCATION);
      String eventDateString = request.getParameter(DATE);
      String eventStartTimeString = request.getParameter(START_TIME);
      String eventEndTimeString = request.getParameter(END_TIME);
      String timezoneOffset = request.getParameter(USER_TIMEZONE);
      String category = request.getParameter(CATEGORY);

      if (!CATEGORIES.contains(category)) category = "other";
  
      Date eventStartDateTime = getEventDateTime(eventDateString, eventStartTimeString, timezoneOffset);
      Date eventEndDateTime = getEventDateTime(eventDateString, eventEndTimeString, timezoneOffset);
      if (eventStartDateTime == null || eventEndDateTime == null) return;
      long timestamp = System.currentTimeMillis();

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

      Entity eventEntity = new Entity(EVENT);
      eventEntity.setProperty(SUMMARY, eventSummary);
      eventEntity.setProperty(TIMESTAMP, timestamp);
      eventEntity.setProperty(LOCATION, eventLocation);
      eventEntity.setProperty(DESCRIPTION, eventDescription);
      eventEntity.setProperty(START_TIME, eventStartDateTime);
      eventEntity.setProperty(END_TIME, eventEndDateTime);
      eventEntity.setProperty(CATEGORY, category);

      datastore.put(eventEntity);

      response.sendRedirect("/feed.html");
  }

  private String convertToJson(List<Event> events) {
      Gson gson = new Gson();
      String json = gson.toJson(events);
      return json;
  }

  public Date getEventDateTime(String eventDate, String eventTime, String timezoneOffset) {
      Date eventDateTime;
      SimpleDateFormat eventDateTimeFormat;
      try {
          // add opposite of offset to get back to utc
          String hrs = String.format("%02d", Math.abs(Integer.parseInt(timezoneOffset) / 60));
          String min = String.format("%02d", Integer.parseInt(timezoneOffset) % 60);
          char sign = '-';
          if (timezoneOffset.charAt(0) == '-') sign = '+';
          eventDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm Z")
            .parse(eventDate + " " + eventTime + " " + sign + hrs + min);
            
          return eventDateTime;
      } catch(Exception e) {
          System.out.println(e.getMessage());
          return null;
      }
  }
}