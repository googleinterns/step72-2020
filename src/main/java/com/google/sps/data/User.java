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

import com.google.gson.*;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public final class User {

  public static final String DATA_TYPE = "User";
  public static final String ID = "userId";
  public static final String NICKNAME = "nickname";
  public static final String CREATED_EVENTS = "created_events";
  public static final String BOOKMARKED_EVENTS = "bookmarked_events";
  public static final String ADDED_TO_CALENDAR_EVENTS = "added_to_calendar_events";
  public static final String CURRENT_CHALLENGE = "current_challenge";
  public static final String CHALLENGE_STATUSES = "challenge_statuses";

  private String id;
  private String nickname;
  private ArrayList<Long> created_events;
  private ArrayList<Long> bookmarked_events;
  private ArrayList<Long> added_to_calendar_events;
  private Long current_challenge_id;
  private ArrayList<Integer> challenge_statuses;
  /* Stored so that calls to datastore.put(entity) will overwrite the user with the
   same userId if such a user already exists -- prevents multiple instances of 
   same user being stored */
  private Key entity_key;

  public static class Builder {
        private String id; 
        private String nickname;
        private ArrayList<Long> created_events;
        private ArrayList<Long> bookmarked_events;
        private ArrayList<Long> added_to_calendar_events;
        private Long current_challenge_id;
        private ArrayList<Integer> challenge_statuses;
        private Key entity_key;

        public Builder(String id) {
            this.id = id;
        }
        public Builder setNickname(String nickname){
            this.nickname = nickname;
            return this; 
        }
        public Builder setCreatedEvents(ArrayList<Long> created_events){
            if (created_events == null) this.created_events = new ArrayList<Long>();
            else this.created_events = (ArrayList) created_events.clone();
            return this;
        }
        public Builder setBookmarkedEvents(ArrayList<Long> bookmarked_events){
            if (bookmarked_events == null) this.bookmarked_events = new ArrayList<Long>();
            else this.bookmarked_events = (ArrayList) bookmarked_events.clone();
            return this;
        }
        public Builder setAddedToCalendarEvents(ArrayList<Long> added_to_calendar_events){
            if (added_to_calendar_events == null) this.added_to_calendar_events = new ArrayList<Long>();
            else this.added_to_calendar_events = (ArrayList) added_to_calendar_events.clone();
            return this;
        }
        public Builder setCurrentChallengeId(Long current_challenge_id){
            this.current_challenge_id = current_challenge_id;
            return this;
        }
        public Builder setChallengeStatuses(ArrayList<Integer> challenge_statuses){
            // @Erick May need to change this initialization if structure of challenge statuses changes
            if (challenge_statuses == null) this.challenge_statuses = new ArrayList<Integer>();
            else this.challenge_statuses = (ArrayList) challenge_statuses.clone();
            return this;
        }
        public Builder setEntityKey(Key entity_key) {
            this.entity_key = entity_key;
            return this;
        }

        public User build(){
            User user = new User();  
            user.id = this.id;
            user.nickname = this.nickname;
            user.entity_key = this.entity_key;
            user.created_events = this.created_events;
            user.bookmarked_events = this.bookmarked_events;
            user.added_to_calendar_events = this.added_to_calendar_events;
            user.current_challenge_id = this.current_challenge_id;
            user.challenge_statuses = this.challenge_statuses;
            return user;
        }
   }

   private User() {
      this.created_events = new ArrayList<Long>();
      this.bookmarked_events = new ArrayList<Long>();
      this.added_to_calendar_events = new ArrayList<Long>();
      this.current_challenge_id = 0L;
      this.challenge_statuses = new ArrayList<Integer>();
      this.entity_key = null;
   }



  // @Erick May need to change the following methods if structure of challenge statuses or id changes
  public Long getCurrentChallenge() {
      return this.current_challenge_id;
  }

  public void setCurrentChallenge(Long chal_id) {
      this.current_challenge_id = chal_id;
  }

  public ArrayList<Integer> getChallengeStatuses() {
      return (ArrayList) this.challenge_statuses;
  }

  // challenge_statuses param should not be null
  public void setChallengeStatuses(ArrayList<Integer> challenge_statuses) {
      this.challenge_statuses = (ArrayList) challenge_statuses.clone();
  }

  public ArrayList<Long> getCreatedEvents() {
      return (ArrayList) this.created_events;
  }

  // created_events param should not be null
  public void setCreatedEvents(ArrayList<Long> created_events) {
      this.created_events = (ArrayList) created_events.clone();
  }

  public ArrayList<Long> getBookmarkedEvents() {
      return (ArrayList) this.bookmarked_events;
  }

  // bookmarked_events param should not be null
  public void setBookmarkedEvents(ArrayList<Long> bookmarked_events) {
      this.bookmarked_events = (ArrayList) bookmarked_events.clone();
  }

  public ArrayList<Long> getAddedToCalendarEvents() {
      return (ArrayList) this.added_to_calendar_events;
  }

  // added_to_calendar_events param should not be null
  public void setAddedToCalendarEvents(ArrayList<Long> added_to_calendar_events) {
      this.added_to_calendar_events = (ArrayList) added_to_calendar_events.clone();
  }
 

  public static User convertEntitytoUser(Entity entity, String userId) {
    Key entityKey = entity.getKey();
    String nickname = (String) entity.getProperty(NICKNAME);
    Long currentChallengeId = (Long) entity.getProperty(CURRENT_CHALLENGE);
    ArrayList<Long> createdEvents =(ArrayList<Long>) entity.getProperty(CREATED_EVENTS);
    ArrayList<Long> bookmarkedEvents = (ArrayList<Long>) entity.getProperty(BOOKMARKED_EVENTS);
    ArrayList<Long> addedEvents = (ArrayList<Long>) entity.getProperty(ADDED_TO_CALENDAR_EVENTS);
    ArrayList<Integer> challengeStatuses = (ArrayList<Integer>) entity.getProperty(CHALLENGE_STATUSES);

    User user = new User.Builder(userId)
        .setNickname(nickname)
        .setEntityKey(entityKey)
        .setCreatedEvents(createdEvents)
        .setBookmarkedEvents(bookmarkedEvents)
        .setAddedToCalendarEvents(addedEvents)
        .setCurrentChallengeId(currentChallengeId)
        .setChallengeStatuses(challengeStatuses)
        .build();

    return user;
  }
  
  public Entity toEntity() {
      Entity userEntity;
      if (this.entity_key == null) userEntity = new Entity(DATA_TYPE);
      else userEntity = new Entity(DATA_TYPE, this.entity_key.getId());
      this.entity_key = userEntity.getKey();
      userEntity.setProperty(ID, this.id);
      userEntity.setProperty(NICKNAME, this.nickname);
      userEntity.setProperty(CREATED_EVENTS, this.created_events);
      userEntity.setProperty(BOOKMARKED_EVENTS, this.bookmarked_events);
      userEntity.setProperty(ADDED_TO_CALENDAR_EVENTS, this.added_to_calendar_events);
      userEntity.setProperty(CURRENT_CHALLENGE, this.current_challenge_id);
      userEntity.setProperty(CHALLENGE_STATUSES, this.challenge_statuses);
      return userEntity;
  }

  public String toJSON() {
      Gson gson = new Gson();
      String json = gson.toJson(this);
      return json;
  }
} 