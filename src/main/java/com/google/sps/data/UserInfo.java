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

import java.util.HashMap;
import java.util.Map;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.EmbeddedEntity;
import javafx.util.Pair;

public final class UserInfo {

  public static final String DATA_TYPE = "UserInfo";
  public static final String ID = "userId";
  public static final String NICKNAME = "nickname";
  public static final String CREATED_EVENTS = "created_events";
  public static final String BOOKMARKED_EVENTS = "bookmarked_events";
  public static final String CURRENT_CHALLENGE = "current_challenge";
  public static final String CHALLENGE_STATUSES = "challenge_statuses";
  public static final String ENTITY_KEY = "entity_key";

  private final String id;
  private final String nickname;
  private final ArrayList<Long> created_events;
  private final ArrayList<Long> bookmarked_events;
  private String current_challenge_id;
  //private ArrayList<Pair<String, Integer>> challenge_statuses;
  private HashMap <String, Integer> challenge_statuses;
  private Key entity_key;

  public UserInfo(String id, String nickname, Key entity_key) {
    this.id = id;
    this.nickname = nickname;
    this.entity_key = entity_key;
    this.created_events = new ArrayList<Long>();
    this.bookmarked_events = new ArrayList<Long>();
    this.current_challenge_id = "WAST_0";
    this.challenge_statuses = new HashMap<>();
  }

  // ArrayList Params may be null
  public UserInfo(String id, String nickname, ArrayList<Long> created_events, ArrayList<Long> bookmarked_events, 
    Long current_challenge_id, HashMap<String, Integer> challenge_statuses, Key entity_key) {
    this.id = id;
    this.nickname = nickname;
    this.entity_key = entity_key;

    if (created_events == null) this.created_events = new ArrayList<Long>();
    else this.created_events = (ArrayList) created_events.clone();

    if (bookmarked_events == null) this.bookmarked_events = new ArrayList<Long>();
    else this.bookmarked_events = (ArrayList) bookmarked_events.clone();

    this.current_challenge_id = current_challenge_id;

    // @Erick May need to change this initialization if structure of challenge statuses changes
    if (challenge_statuses == null) this.challenge_statuses = new HashMap<String, Integer>();
    else this.challenge_statuses = (HashMap) challenge_statuses.clone();
  }
  

  // @Erick May need to change the following methods if structure of challenge statuses or id changes
  public Long getCurrentChallenge() {
      return this.current_challenge_id;
  }

  public void setCurrentChallenge(Long chal_id) {
      this.current_challenge_id = chal_id;
  }

  public HashMap<String, Integer> getChallengeStatuses() {
      return (HashMap) this.challenge_statuses;
  }

  // challenge_statuses param should not be null
  public void setChallengeStatuses(HashMap<String, Integer> challenge_statuses) {
      this.challenge_statuses = (HashMap<String, Integer>) challenge_statuses.clone();
  }

  public static UserInfo convertEntitytoUserInfo(Entity entity, String userId) {
    Key entity_key = entity.getKey();
    String nickname = (String) entity.getProperty(NICKNAME);
    Long currentChallengeId = (Long) entity.getProperty(CURRENT_CHALLENGE);
    ArrayList<Long> createdEvents =(ArrayList<Long>) entity.getProperty(CREATED_EVENTS);
    ArrayList<Long> bookmarkedEvents = (ArrayList<Long>) entity.getProperty(BOOKMARKED_EVENTS);
    HashMap<String, Integer> challengeStatuses = getChallengeStatusFromEntity(entity);
    UserInfo user = new UserInfo(userId, nickname, createdEvents, bookmarkedEvents, currentChallengeId, challengeStatuses, entity_key);
    return user;
  }
  
  public Entity toEntity() {
      Entity userEntity;
      if (this.entity_key == null) userEntity = new Entity(DATA_TYPE);
      else userEntity = new Entity(DATA_TYPE, this.entity_key.getId());
      userEntity.setProperty(ID, this.id);
      userEntity.setProperty(NICKNAME, this.nickname);
      userEntity.setProperty(CURRENT_CHALLENGE, this.current_challenge_id);
      userEntity.setProperty(CHALLENGE_STATUSES, embedChallengeStatuses());
      return userEntity;
  }

  public String toJSON() {
      Gson gson = new Gson();
      String json = gson.toJson(this);
      return json;
  }

  /* Function embeds array of challenge_statuses pairs into an Entity so it may
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
       Long status = (Long) embedded_entity.getProperty(key);
       challenge_statuses.put(key, status.intValue());
     }
    }
    return challenge_statuses;
  }
} 