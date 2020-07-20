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

import com.google.sps.data.UserInfo;

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

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.json.gson.GsonFactory;

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
    static final String CREATOR = "creator";
    static final String EVENT_NUM = "EventNum";
    static final String EVENT_NUM_VALUE = "value";
    static final String EVENT_ID = "event_id";
    static final String ID_TOKEN_PARAM = "id_token";

    private static final String CLIENT_ID = "605480199600-e4uo1livbvl58cup3qtd1miqas7vspcu.apps.googleusercontent.com";

    static final List<String> CATEGORIES = new ArrayList<String>(
        Arrays.asList("food_beverage", "nature", "water", "waste_cleanup", "other")
    );

    static final int MAX_STRING_BYTES = 1500;

    static final HttpTransport HTTP_TRANSPORT = new UrlFetchTransport();
    static final JsonFactory JSON_FACTORY = new GsonFactory();

    static final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(HTTP_TRANSPORT, JSON_FACTORY)
        .setAudience(Collections.singletonList(CLIENT_ID))
        .build();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);
    Query query = new Query(EVENT).addSort(TIMESTAMP, SortDirection.DESCENDING);
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
        String userId = (String) entity.getProperty(CREATOR);
        long eventId = (long) entity.getProperty(EVENT_ID);

        Query userQuery = new Query(UserInfo.DATA_TYPE).setFilter(new FilterPredicate(UserInfo.ID, FilterOperator.EQUAL, userId));
        Entity creator = datastore.prepare(userQuery).asSingleEntity();
        String nickname = (String) creator.getProperty(UserInfo.NICKNAME);

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
        ep.set(CREATOR, nickname);
        ep.set(EVENT_ID, eventId);
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
      GoogleIdToken idToken = verifyId(request.getParameter(ID_TOKEN_PARAM));
      if (idToken == null) {
        System.out.println("Invalid ID token.");
        response.setStatus(400);
        return;
      }

      Payload payload = idToken.getPayload();
      String userId = payload.getSubject();

      String eventSummary = request.getParameter(SUMMARY);
      String eventDescription = request.getParameter(DESCRIPTION);
      String eventLocation = request.getParameter(LOCATION);
      String eventDateString = request.getParameter(DATE);
      String eventStartTimeString = request.getParameter(START_TIME);
      String eventEndTimeString = request.getParameter(END_TIME);
      String timezoneOffset = request.getParameter(USER_TIMEZONE);
      String category = request.getParameter(CATEGORY);

      eventSummary = sanitizeInput(eventSummary);
      eventDescription = sanitizeInput(eventDescription);
      eventLocation = sanitizeInput(eventLocation);

      if (!CATEGORIES.contains(category)) category = "other";
  
      Date eventStartDateTime = getEventDateTime(eventDateString, eventStartTimeString, timezoneOffset);
      Date eventEndDateTime = getEventDateTime(eventDateString, eventEndTimeString, timezoneOffset);
      if (eventStartDateTime == null || eventEndDateTime == null) return;
      if (eventEndDateTime.compareTo(eventStartDateTime) < 0) {
          Date temp = eventStartDateTime;
          eventStartDateTime = eventEndDateTime;
          eventEndDateTime = temp;
      }

      long timestamp = System.currentTimeMillis();

      long eventId = getEventId();
      updateUserCreatedEvents(userId, eventId);

      Entity eventEntity = new Entity(EVENT);
      eventEntity.setProperty(EVENT_ID, eventId);
      eventEntity.setProperty(SUMMARY, eventSummary);
      eventEntity.setProperty(TIMESTAMP, timestamp);
      eventEntity.setProperty(LOCATION, eventLocation);
      eventEntity.setProperty(DESCRIPTION, eventDescription);
      eventEntity.setProperty(START_TIME, eventStartDateTime);
      eventEntity.setProperty(END_TIME, eventEndDateTime);
      eventEntity.setProperty(CATEGORY, category);
      eventEntity.setProperty(CREATOR, userId);

      datastore.put(eventEntity);

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

  public String sanitizeInput(String input) throws java.io.UnsupportedEncodingException {
      input = new String( input.getBytes("UTF-8") , 0, Math.min(MAX_STRING_BYTES, input.length()), "UTF-8");
      return input;
  }

  public long getEventId() {
      Query query = new Query(EVENT_NUM);
      PreparedQuery pq = datastore.prepare(query);
      QueryResultList<Entity> eventNumResult = pq.asQueryResultList(FetchOptions.Builder.withDefaults());

      Entity eventNumEntity;
      long eventNumValue;
      if (eventNumResult.size() == 0) {
          eventNumEntity = new Entity(EVENT_NUM);
          eventNumValue = 0;
          eventNumEntity.setProperty(EVENT_NUM_VALUE, eventNumValue);
          datastore.put(eventNumEntity);      
      }
      else {
          eventNumEntity = eventNumResult.get(0);
          eventNumValue = (long)eventNumEntity.getProperty(EVENT_NUM_VALUE)+1;
          eventNumEntity.setProperty(EVENT_NUM_VALUE, eventNumValue);
          datastore.put(eventNumEntity);
      }

      return eventNumValue;
  }

  public void updateUserCreatedEvents(String userId, long eventId) {
      Query userQuery = new Query(UserInfo.DATA_TYPE).setFilter(new FilterPredicate(UserInfo.ID, FilterOperator.EQUAL, userId));
      Entity entity = datastore.prepare(userQuery).asSingleEntity();
      ArrayList<Long> createdEvents =(ArrayList<Long>) entity.getProperty(UserInfo.CREATED_EVENTS);
      if (createdEvents == null) createdEvents = new ArrayList<Long>();
      createdEvents.add(eventId);
      entity.setProperty(UserInfo.CREATED_EVENTS, createdEvents);
      datastore.put(entity);
  }

  private GoogleIdToken verifyId(String idTokenString) {
    GoogleIdToken idToken = null;
    try {
        idToken = verifier.verify(idTokenString);
    } catch (Exception e) {
        System.err.println(e.getMessage());
    }
    return idToken;
  }
}