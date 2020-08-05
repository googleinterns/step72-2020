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

import com.google.sps.data.User;
import com.google.sps.data.EventWrapper;

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
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Event.ExtendedProperties;

import com.google.sps.data.IdHelper;
import com.google.sps.data.GoogleIdHelper;

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
    public static final String EVENT = "Event";
    public static final String TIMESTAMP = "timestamp";
    public static final String USER_TIMEZONE = "timezone";
    public static final String DATE = "date";
    
    private IdHelper idHelper = new GoogleIdHelper();
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    public void setIdHelper(IdHelper idHelper) {
        this.idHelper = idHelper;
    }

    public void setDatastoreService(DatastoreService service) {
        this.datastore = service;
    }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);
    Query query = new Query(EVENT).addSort(TIMESTAMP, SortDirection.DESCENDING);
    PreparedQuery pq = datastore.prepare(query);

    QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);

    List<Event> events = new ArrayList<>();
    for (Entity entity : results) {
        events.add(EventWrapper.convertEntityToEvent(entity));
    }

    response.setContentType("application/json; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");

    String json = convertToJson(events);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      String userId = idHelper.getUserId(request);
      if (userId == null) {
          response.setStatus(400);
          return;
      }

      String eventSummary = request.getParameter(EventWrapper.SUMMARY);
      String eventDescription = request.getParameter(EventWrapper.DESCRIPTION);
      String eventLocation = request.getParameter(EventWrapper.LOCATION);
      String eventDateString = request.getParameter(DATE);
      String eventStartTimeString = request.getParameter(EventWrapper.START_TIME);
      String eventEndTimeString = request.getParameter(EventWrapper.END_TIME);
      String category = request.getParameter(EventWrapper.CATEGORY);

      String timezoneOffset = request.getParameter(USER_TIMEZONE);
      if (timezoneOffset == null) {
          response.setStatus(400);
          return;
      }
  
      Date eventStartDateTime = getEventDateTime(eventDateString, eventStartTimeString, timezoneOffset);
      Date eventEndDateTime = getEventDateTime(eventDateString, eventEndTimeString, timezoneOffset);
      if (eventStartDateTime == null || eventEndDateTime == null) return;
      if (eventEndDateTime.compareTo(eventStartDateTime) < 0) {
          Date temp = eventStartDateTime;
          eventStartDateTime = eventEndDateTime;
          eventEndDateTime = temp;
      }

      EventWrapper eventWrapper = new EventWrapper.Builder()
        .setSummary(eventSummary)
        .setDescription(eventDescription)
        .setLocation(eventLocation)
        .setStartDateTime(eventStartDateTime)
        .setEndDateTime(eventEndDateTime)
        .setCategory(category)
        .setCreator(userId)
        .build();

      Entity eventEntity = eventWrapper.toEntity();
        
      datastore.put(eventEntity);
      updateUserCreatedEvents(userId, eventEntity.getKey().getId());
    
      response.sendRedirect("/index.html");
  }

  private String convertToJson(List<Event> events) {
      Gson gson = new Gson();
      String json = gson.toJson(events);
      return json;
  }

  // Handles differences in client timexone by converting to UTC timezone. Takes in the event
  // date and time as well as client timezone offset (in minutes) to convert to UTC. Combines
  // event date and time to yyyy-MM-dd HH:mm Z format.
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
          System.err.println(e.getMessage());
          return null;
      }
  }

  public void updateUserCreatedEvents(String userId, long eventId) {
      Query userQuery = new Query(User.DATA_TYPE).setFilter(new FilterPredicate(User.ID, FilterOperator.EQUAL, userId));
      Entity entity = datastore.prepare(userQuery).asSingleEntity();
      ArrayList<Long> createdEvents =(ArrayList<Long>) entity.getProperty(User.CREATED_EVENTS);
      if (createdEvents == null) createdEvents = new ArrayList<Long>();
      createdEvents.add(eventId);
      entity.setProperty(User.CREATED_EVENTS, createdEvents);
      datastore.put(entity);
  }
}
