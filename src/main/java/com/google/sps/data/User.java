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

import com.google.gson.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

import org.apache.commons.lang3.StringUtils;
import java.lang.ClassCastException;

public final class User {

  public static final String DATA_TYPE = "User";
  public static final String ID = "user_id";
  public static final String NICKNAME = "nickname";
  public static final String CREATED_EVENTS = "created_events";
  public static final String BOOKMARKED_EVENTS = "bookmarked_events";
  public static final String ADDED_TO_CALENDAR_EVENTS = "added_to_calendar_events";
  public static final String CURRENT_CHALLENGE = "current_challenge";
  public static final String CHALLENGE_STATUSES = "challenge_statuses";
  public static final String COMPLETED_CHALLENGES = "completed_challenges";

  private String user_id;
  private String nickname;
  private ArrayList<Long> created_events;
  private ArrayList<Long> bookmarked_events;
  private ArrayList<Long> added_to_calendar_events;
  private String current_challenge_id;
  private HashMap<String, Integer> challenge_statuses;
  private HashSet<String> completed_challenges = new HashSet<>();

  /* Stored so that calls to datastore.put(entity) will overwrite the user with the
   same userId if such a user already exists -- prevents multiple instances of 
   same user being stored */
  private Key entity_key;

  public static class Builder {
        private String user_id; 
        private String nickname;
        private ArrayList<Long> created_events;
        private ArrayList<Long> bookmarked_events;
        private ArrayList<Long> added_to_calendar_events;
        private String current_challenge_id;
        private HashMap<String, Integer> challenge_statuses;
        private HashSet<String> completed_challenges = new HashSet<>();
        private Key entity_key;

        public Builder(String user_id) {
            this.user_id = user_id;
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
        public Builder setCurrentChallengeId(String current_challenge_id){
            this.current_challenge_id = current_challenge_id;
            return this;
        }
        public Builder setChallengeStatuses(HashMap<String, Integer> challenge_statuses){
            if (challenge_statuses == null) this.challenge_statuses = new HashMap<String, Integer>();
            else this.challenge_statuses = (HashMap) challenge_statuses.clone();
            return this;
        }
        public Builder setEntityKey(Key entity_key) {
            this.entity_key = entity_key;
            return this;
        }

        public User build(){
            User user = new User();  
            user.user_id = this.user_id;
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

   private User() {}

   public String getId() {
       return this.user_id;
   }

   public String getNickname() {
       return this.nickname;
   }


  public String getCurrentChallenge() {
      return this.current_challenge_id;
  }

  public void setCurrentChallenge(String chal_id) {
      this.current_challenge_id = chal_id;
  }

  public HashMap<String, Integer> getChallengeStatuses() {
      return (HashMap) this.challenge_statuses;
  }

  public HashSet<String> getCompletedChallenges(){
      return (HashSet) this.completed_challenges;
  }

  // challenge_statuses param should not be null
  public void setChallengeStatuses(HashMap<String, Integer> challenge_statuses) {
      this.challenge_statuses = (HashMap) challenge_statuses.clone();
  }

  public void setCompletedChallenges(HashSet<String> completed_challenges) {
      this.completed_challenges = (HashSet) completed_challenges.clone();
  }

  public void appendToCompletedChallenges(String id) {
      this.completed_challenges.add(id);
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
 

  public static User convertEntityToUser(Entity entity, String userId) {
    Key entityKey = entity.getKey();
    String nickname = (String) entity.getProperty(NICKNAME);
    String currentChallengeId = (String) entity.getProperty(CURRENT_CHALLENGE);
    ArrayList<Long> createdEvents =(ArrayList<Long>) entity.getProperty(CREATED_EVENTS);
    ArrayList<Long> bookmarkedEvents = (ArrayList<Long>) entity.getProperty(BOOKMARKED_EVENTS);
    ArrayList<Long> addedEvents = (ArrayList<Long>) entity.getProperty(ADDED_TO_CALENDAR_EVENTS);
    HashMap<String, Integer> challengeStatuses = getChallengeStatusFromEntity(entity);
    HashSet<String> completedChallenges = getCompletedChallengesFromEntity(entity);

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
  /* Calling datastore.put([user object].toEntity()) multiple times on a [user object]
   that wasn't instantiated from a User.convertEntityToUser call will result in 
   multiple users being created in the datastore
  */
  public Entity toEntity() {
      Entity userEntity;
      if (this.entity_key == null) userEntity = new Entity(DATA_TYPE);
      else userEntity = new Entity(DATA_TYPE, this.entity_key.getId());
      this.entity_key = userEntity.getKey();
      userEntity.setProperty(ID, this.user_id);
      userEntity.setProperty(NICKNAME, this.nickname);
      userEntity.setProperty(CREATED_EVENTS, this.created_events);
      userEntity.setProperty(BOOKMARKED_EVENTS, this.bookmarked_events);
      userEntity.setProperty(ADDED_TO_CALENDAR_EVENTS, this.added_to_calendar_events);
      userEntity.setProperty(CURRENT_CHALLENGE, this.current_challenge_id);
      userEntity.setProperty(CHALLENGE_STATUSES, embedChallengeStatuses());
      userEntity.setProperty(COMPLETED_CHALLENGES,  getCompletedChallengesAsArray());
      return userEntity;
  }

  public String toJSON() {
      Gson gson = new Gson();
      String json = gson.toJson(this);
      return json;
  }

  // For array list, treats empty list and null values as equal
  public boolean equals(User user) {
      boolean idsEqual = StringUtils.equals(this.user_id, user.user_id);
      boolean nicknamesEqual = StringUtils.equals(this.nickname, user.nickname);
      boolean currentChallengeEqual = this.current_challenge_id == user.current_challenge_id;
      boolean entityKeyEqual = (this.entity_key == null && user.entity_key == null) 
        || (!(this.entity_key == null || user.entity_key == null) && this.entity_key.getId() == user.entity_key.getId());
      boolean createdEventsEqual = checkIfArrayListsEqual(this.created_events, user.created_events);
      boolean bookmarkedEventsEqual = checkIfArrayListsEqual(this.bookmarked_events, user.bookmarked_events);
      boolean addedEventsEqual = checkIfArrayListsEqual(this.added_to_calendar_events, user.added_to_calendar_events);
      boolean challengeStatusesEqual = (this.challenge_statuses == null && user.challenge_statuses == null) 
        || (this.challenge_statuses == null && user.challenge_statuses != null && user.challenge_statuses.isEmpty())
        || (this.challenge_statuses != null && this.challenge_statuses.isEmpty() && user.challenge_statuses == null)
        || (!(this.challenge_statuses == null || user.challenge_statuses == null) && this.challenge_statuses.equals(user.challenge_statuses));
      return idsEqual && nicknamesEqual && currentChallengeEqual && entityKeyEqual && createdEventsEqual && bookmarkedEventsEqual && addedEventsEqual && challengeStatusesEqual;
  }

  private boolean checkIfArrayListsEqual(ArrayList<Long> arr1, ArrayList<Long> arr2) {
      return (arr1 == null && arr2 == null) 
        || (arr1 == null && arr2 != null && arr2.isEmpty())
        || (arr1 != null && arr1.isEmpty() && arr2 == null)
        || (!(arr1 == null || arr2 == null) && arr1.equals(arr2));
  }

  /* Function embeds Map of challenge_statuses into an Entity so it may
  be stored in Datastore */
  private EmbeddedEntity embedChallengeStatuses(){
    EmbeddedEntity embedded_entity = new EmbeddedEntity();
    for (String key: challenge_statuses.keySet()){
      embedded_entity.setProperty(key, challenge_statuses.get(key));
    }
    return embedded_entity;
  }

  private static HashMap<String, Integer> getChallengeStatusFromEntity(Entity entity) {
    EmbeddedEntity embedded_entity = (EmbeddedEntity)entity.getProperty(CHALLENGE_STATUSES);
    HashMap<String, Integer> challenge_statuses = new HashMap<>();
    if (embedded_entity != null){
     for(String key : embedded_entity.getProperties().keySet()){
       try {
           Long status = (Long) embedded_entity.getProperty(key);
           challenge_statuses.put(key, status.intValue());
       } catch (ClassCastException e) {
           Integer status = (Integer) embedded_entity.getProperty(key);
           challenge_statuses.put(key, status.intValue());
       }
       
     }
    }
    return challenge_statuses;
  }

  private ArrayList<String> getCompletedChallengesAsArray(){
    ArrayList<String> compl_challenges = new ArrayList<String>(completed_challenges);
    return compl_challenges;
  }

  private static HashSet<String> getCompletedChallengesFromEntity(Entity entity) {
    ArrayList<String> temp = (ArrayList<String>) entity.getProperty(COMPLETED_CHALLENGES);
    HashSet<String> compl_challenges;
    if (temp != null){
      compl_challenges = new HashSet<String>(temp);
    } else {
      compl_challenges = new HashSet<String>();
    }
    return compl_challenges;
  }
} 