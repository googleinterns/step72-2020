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

package com.google.sps.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Date;

import com.google.gson.*;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

import com.google.api.client.util.DateTime;

import java.io.UnsupportedEncodingException;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Event.ExtendedProperties;

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

public final class EventWrapper {

  public static final String EVENT = "Event";
  public static final String TIMESTAMP = "timestamp";
  public static final String SUMMARY = "summary";
  public static final String DESCRIPTION = "description";
  public static final String LOCATION = "location";
  public static final String CATEGORY = "category";
  public static final String UTC_TIMEZONE = "UTC";
  public static final String START_TIME = "start";
  public static final String END_TIME = "end";
  public static final String EVENT_CREATOR = "creator";
  public static final String EVENT_ID = "event_id";

  public static final List<String> CATEGORIES = new ArrayList<String>(
        Arrays.asList("food_beverage", "nature", "water", "waste_cleanup", "other")
  );

  static final int MAX_STRING_BYTES = 1500;

  private String summary;
  private String description;
  private String location;
  private Date start;
  private Date end;
  private String category;
  private String creator_id;
  /* Stored in order to retrieve consistent ID for the event */
  private Key entity_key;

  public static class Builder {
        private Key entity_key;
        private String summary;
        private String description;
        private String location;
        private Date start;
        private Date end;
        private String category;
        private String creator_id;

        public Builder() {
        }

        public Builder setStartDateTime(Date start){
            this.start = start;
            return this; 
        }
        public Builder setEndDateTime(Date end){
            this.end = end;
            return this; 
        }

        public Builder setSummary(String summary) {
            this.summary = null;
            try {
                this.summary = sanitizeInput(summary);
            } catch (UnsupportedEncodingException e) {
                System.err.println(e.getMessage());
            }
            return this;
        }

        public Builder setDescription(String description) {
            this.description = null;
            try {
                this.description = sanitizeInput(description);
            } catch (UnsupportedEncodingException e) {
                System.err.println(e.getMessage());
            }
            return this;
        }

        public Builder setLocation(String location) {
            this.location = null;
            try {
                this.location = sanitizeInput(location);
            } catch (UnsupportedEncodingException e) {
                System.err.println(e.getMessage());
            }
            return this;
        }

        public Builder setEntityKey(Key entity_key) {
            this.entity_key = entity_key;
            return this;
        }

        public Builder setCategory(String category) {
            if (!CATEGORIES.contains(category)) category = "other";
            this.category = category;
            return this;
        }

        public Builder setCreator(String user_id) {
            this.creator_id = user_id;
            return this;
        }

        public EventWrapper build(){
            EventWrapper eventWrapper = new EventWrapper();
            eventWrapper.summary = this.summary;
            eventWrapper.description = this.description;
            eventWrapper.location = this.location;
            eventWrapper.start = this.start;
            eventWrapper.end = this.end;
            eventWrapper.entity_key = this.entity_key;
            eventWrapper.category = this.category;
            eventWrapper.creator_id = this.creator_id;
            return eventWrapper;
        }
   }

   private EventWrapper() {
      this.entity_key = null;
   }

   public Event toEvent() {
       DateTime startDateTime = new DateTime(this.start);
       EventDateTime start = new EventDateTime()
        .setDateTime(startDateTime)
        .setTimeZone(UTC_TIMEZONE);
       DateTime endDateTime = new DateTime(this.end);
       EventDateTime end = new EventDateTime()
        .setDateTime(endDateTime)
        .setTimeZone(UTC_TIMEZONE);
       Event event = new Event()
        .setSummary(this.summary)
        .setLocation(this.location)
        .setDescription(this.description)
        .setStart(start)
        .setEnd(end);

       ExtendedProperties ep = new ExtendedProperties();
       ep.set(CATEGORY, this.category);
       ep.set(EVENT_CREATOR, getEventCreatorName(this.creator_id));
       ep.set(EVENT_ID, this.entity_key.getId());
       event.setExtendedProperties(ep);
       return event;
   }


  public static Event convertEntityToEvent(Entity entity) {
    Key entityKey = entity.getKey();
    String summary = (String) entity.getProperty(SUMMARY);
    String description = (String) entity.getProperty(DESCRIPTION);
    String location = (String) entity.getProperty(LOCATION);
    Date startTime = (Date) entity.getProperty(START_TIME);
    Date endTime = (Date) entity.getProperty(END_TIME);
    String category = (String) entity.getProperty(CATEGORY);
    String userId = (String) entity.getProperty(EVENT_CREATOR);

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
    ep.set(EVENT_CREATOR, getEventCreatorName(userId));
    ep.set(EVENT_ID, entityKey.getId());
    event.setExtendedProperties(ep);

    return event;
  }

  public Entity toEntity() {
      Entity eventEntity;
      if (this.entity_key == null) eventEntity = new Entity(EVENT);
      else eventEntity = new Entity(EVENT, this.entity_key.getId());
      this.entity_key = eventEntity.getKey();
      long timestamp = System.currentTimeMillis();
      eventEntity.setProperty(SUMMARY, this.summary);
      eventEntity.setProperty(TIMESTAMP, timestamp);
      eventEntity.setProperty(LOCATION, this.location);
      eventEntity.setProperty(DESCRIPTION, this.description);
      eventEntity.setProperty(START_TIME, this.start);
      eventEntity.setProperty(END_TIME, this.end);
      eventEntity.setProperty(CATEGORY, this.category);
      eventEntity.setProperty(EVENT_CREATOR, this.creator_id);
      return eventEntity;
  }

  public String toJSON() {
      Gson gson = new Gson();
      String json = gson.toJson(this);
      return json;
  }

  private static String sanitizeInput(String input) throws UnsupportedEncodingException {
      input = new String( input.getBytes("UTF-8") , 0, Math.min(MAX_STRING_BYTES, input.length()), "UTF-8");
      return input;
  }

  private static String getEventCreatorName(String userId) {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Query userQuery = new Query(User.DATA_TYPE).setFilter(new FilterPredicate(User.ID, FilterOperator.EQUAL, userId));
      Entity eventCreator = datastore.prepare(userQuery).asSingleEntity();
      String nickname = (String) eventCreator.getProperty(User.NICKNAME);
      return nickname;
  }
} 