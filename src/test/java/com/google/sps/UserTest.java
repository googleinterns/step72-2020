package com.google.sps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.sps.data.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runners.JUnit4;
import org.junit.runner.RunWith;
import org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import org.json.JSONObject;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public final class UserTest {
  private static final String USER_ID = "00";
  private static final String NICKNAME = "Name";
  private static final String DEFAULT_CHALLENGE_ID = "GARD_0";;
  private static final HashMap<String, Integer> DEFAULT_CHALLENGE_STATUSES = new HashMap<String, Integer>();
  static {
    DEFAULT_CHALLENGE_STATUSES.put("GARD_0",0);
    DEFAULT_CHALLENGE_STATUSES.put("RECY_0",0);
    DEFAULT_CHALLENGE_STATUSES.put("WAST_0",0);
  }

  private User user;
  private JSONObject userJson;

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Before
  public void setup(){
    helper.setUp();

    user = new User.Builder(USER_ID)
        .setNickname(NICKNAME)
        .setCurrentChallengeId(DEFAULT_CHALLENGE_ID)
        .setChallengeStatuses(DEFAULT_CHALLENGE_STATUSES)
        .build();
    
    userJson = new JSONObject(user.toJSON());
  }

  @After
  public void tearDown() {
      helper.tearDown();
  }

  @Test
  public void userEqualsTest(){
    User other = new User.Builder(USER_ID)
        .setNickname(NICKNAME)
        .setCurrentChallengeId(DEFAULT_CHALLENGE_ID)
        .setCreatedEvents(new ArrayList<Long>())
        .setChallengeStatuses(DEFAULT_CHALLENGE_STATUSES)
        .build();

        Assert.assertTrue(user.equals(other));
  }

  @Test
  public void userNotEqualsTest(){
    User other = new User.Builder(USER_ID)
        .setNickname(NICKNAME)
        .setCurrentChallengeId(DEFAULT_CHALLENGE_ID)
        .setCreatedEvents(new ArrayList<Long>(Arrays.asList(1L)))
        .setChallengeStatuses(DEFAULT_CHALLENGE_STATUSES)
        .build();

        Assert.assertTrue(!user.equals(other));
  }

  @Test
  public void userCurrentChallengeTest(){
    String newChallenge = "RECY_0";
    user.setCurrentChallenge(newChallenge);
    Assert.assertTrue(user.getCurrentChallenge().equals(newChallenge));
  }

  @Test
  public void userCreatedEventsTest(){
    user.setCreatedEvents(new ArrayList<Long>(Arrays.asList(1L, 2L, 3L)));
    Assert.assertEquals(new ArrayList<Long>(Arrays.asList(1L, 2L, 3L)), user.getCreatedEvents());
  }

  @Test
  public void convertEntityToUserTest(){
    Entity entity = new Entity(User.DATA_TYPE);
    entity.setProperty(User.NICKNAME, NICKNAME);
    entity.setProperty(User.CURRENT_CHALLENGE, DEFAULT_CHALLENGE_ID);
    entity.setProperty(User.CHALLENGE_STATUSES, embedChallengeStatuses(DEFAULT_CHALLENGE_STATUSES));

    User expected = new User.Builder(USER_ID)
        .setNickname(NICKNAME)
        .setEntityKey(entity.getKey())
        .setChallengeStatuses(DEFAULT_CHALLENGE_STATUSES)
        .setCurrentChallengeId(DEFAULT_CHALLENGE_ID)
        .build();

    User result = User.convertEntityToUser(entity, USER_ID);
    Assert.assertTrue(result.equals(expected));
  }

  @Test
  public void userToEntityTestBasic(){
    Entity entity = user.toEntity();
    Assert.assertEquals((String) entity.getProperty(User.ID), USER_ID);
    Assert.assertEquals((String) entity.getProperty(User.NICKNAME), NICKNAME);
    Assert.assertEquals((String) entity.getProperty(User.CURRENT_CHALLENGE), DEFAULT_CHALLENGE_ID);
    Assert.assertEquals((ArrayList<Long>) entity.getProperty(User.CREATED_EVENTS), null);
    Assert.assertEquals((ArrayList<Long>) entity.getProperty(User.BOOKMARKED_EVENTS), null);
    Assert.assertEquals((ArrayList<Long>) entity.getProperty(User.ADDED_TO_CALENDAR_EVENTS), null);
    Assert.assertEquals(getChallengeStatusFromEntity(entity), DEFAULT_CHALLENGE_STATUSES);
  }

  @Test
  public void userToEntityTestWithArrayListValues(){
    user.setCreatedEvents(new ArrayList<Long>(Arrays.asList(1L, 2L, 3L)));
    user.setBookmarkedEvents(new ArrayList<Long>(Arrays.asList(1L)));
    Entity entity = user.toEntity();
    Assert.assertEquals((String) entity.getProperty(User.ID), USER_ID);
    Assert.assertEquals((String) entity.getProperty(User.NICKNAME), NICKNAME);
    Assert.assertEquals((String) entity.getProperty(User.CURRENT_CHALLENGE), DEFAULT_CHALLENGE_ID);
    Assert.assertEquals((ArrayList<Long>) entity.getProperty(User.CREATED_EVENTS), new ArrayList<Long>(Arrays.asList(1L, 2L, 3L)));
    Assert.assertEquals((ArrayList<Long>) entity.getProperty(User.BOOKMARKED_EVENTS), new ArrayList<Long>(Arrays.asList(1L)));
    Assert.assertEquals((ArrayList<Long>) entity.getProperty(User.ADDED_TO_CALENDAR_EVENTS), null);
    Assert.assertEquals(getChallengeStatusFromEntity(entity), DEFAULT_CHALLENGE_STATUSES);
  }

  /* Function embeds Map of challenge_statuses into an Entity so it may
  be stored in Datastore -- taken from User Class and made static */
  private static EmbeddedEntity embedChallengeStatuses(HashMap<String, Integer> challengeStatuses){
    EmbeddedEntity embedded_entity = new EmbeddedEntity();
    for (String key: challengeStatuses.keySet()){
      embedded_entity.setProperty(key, challengeStatuses.get(key));
    }
    return embedded_entity;
  }

  private static HashMap<String, Integer> getChallengeStatusFromEntity(Entity entity) {
    EmbeddedEntity embedded_entity = (EmbeddedEntity)entity.getProperty(User.CHALLENGE_STATUSES);
    HashMap<String, Integer> challenge_statuses = new HashMap<>();
    if (embedded_entity != null){
     for(String key : embedded_entity.getProperties().keySet()){
       challenge_statuses.put(key, (Integer) embedded_entity.getProperty(key));
     }
    }
    return challenge_statuses;
  }
}