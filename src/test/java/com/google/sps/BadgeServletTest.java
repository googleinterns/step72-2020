package com.google.sps;
import com.google.gson.*;
import org.mockito.Mockito.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import com.google.gson.Gson;
import com.google.sps.data.Badge;
import com.google.sps.data.BadgeData;
import com.google.sps.data.Challenge;
import com.google.sps.data.ChallengeData;

import com.google.sps.data.GoogleIdHelper;
import com.google.sps.data.MockIdHelper;
import com.google.sps.data.User;

import com.google.sps.servlets.ChallengesServlet;
import com.google.sps.servlets.BadgeServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.JUnit4;
import org.junit.runner.RunWith;
import org.json.JSONObject;
import org.json.JSONArray;

@RunWith(JUnit4.class)
public final class BadgeServletTest {
  private BadgeServlet servlet;
  private static final String NUM_CHALLNEGES = "3";
  private static final String ID_TOKEN_PARAM = "id_token";

  private static final String MOCK_ID_TOKEN = "123";
  private static final String MOCK_NICKNAME = "name";
  private static final String MOCK_USER_ID = "00";
  private static final String MOCK_CMPL_CHAL_TYPE = "GARDENING";


  private static final HashMap<String, Integer> DEFAULT_CHALLENGE_STATUSES = getDefaultChallengeStatuses();
  private static final HashSet<String> DEFAULT_BADGES = getDefaultBadges();
  private static final HashSet<String> DEFAULT_COMPLETED_CHALLENGES = getCompletedChallenges();

  private User user;
  private JSONObject user_json;
  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Before
  public void setup(){
    helper.setUp();
    servlet = new BadgeServlet();
    servlet.setIdHelper(new MockIdHelper());
    servlet.setDatastoreService(datastore);

    user = new User.Builder(MOCK_USER_ID)
        .setNickname(MOCK_NICKNAME)
        .setCurrentChallengeId(ChallengeData.DEF_CURRENT_CHALLENGE_ID)
        .setChallengeStatuses(DEFAULT_CHALLENGE_STATUSES)
        .setCompletedChallenges(DEFAULT_COMPLETED_CHALLENGES)
        .setEarnedBadges(DEFAULT_BADGES)
        .build();  
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void getBadgesTest() {
    datastore.put(user.toEntity());
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(request.getParameter(ID_TOKEN_PARAM)).thenReturn(MOCK_ID_TOKEN);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doGet(request, response);
      pw.flush();
      JSONArray response_json = new JSONArray(writer.toString().trim()); 
      JSONObject badge_json = 
        new JSONObject(new Gson().toJson(BadgeData.BADGE_MAP));

      for(int i = 0; i < 2; i++){
        String id = response_json.getJSONObject(i).getString("id");
        Assert.assertEquals(badge_json.getJSONObject(id).toString(),
                            response_json.getJSONObject(i).toString());
      }
    } catch (IOException e) {}
  }

  @Test
  public void newBadgeIndicatorTest() {
    datastore.put(user.toEntity());
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(request.getParameter(ID_TOKEN_PARAM)).thenReturn(MOCK_ID_TOKEN);
    when(request.getParameter(BadgeServlet.COMPLETED_CHALLENGE_TYPE)).thenReturn(MOCK_CMPL_CHAL_TYPE);
    
    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

      
    try{
      when(response.getWriter()).thenReturn(pw);
      servlet.doPut(request, response);
      pw.flush();
      boolean new_badge_indicator = Boolean.parseBoolean(writer.toString().trim());
      System.out.println("badge indicator: " + new_badge_indicator);
      
      Assert.assertTrue(new_badge_indicator);

    } catch (IOException e) {} 
  }

  @Test
  public void updateUserBadgeTest() {
    datastore.put(user.toEntity());
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(request.getParameter(ID_TOKEN_PARAM)).thenReturn(MOCK_ID_TOKEN);
    when(request.getParameter(BadgeServlet.COMPLETED_CHALLENGE_TYPE)).thenReturn(MOCK_CMPL_CHAL_TYPE);
    
    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try{
      when(response.getWriter()).thenReturn(pw);
      servlet.doPut(request, response);
      pw.flush();

      Query query = new Query(User.DATA_TYPE).setFilter(new FilterPredicate(User.ID, FilterOperator.EQUAL, MOCK_USER_ID));
      Entity entity = datastore.prepare(query).asSingleEntity();
      User user = User.convertEntityToUser(entity, MOCK_USER_ID);
      
      HashSet<String> earned_badges = user.getEarnedBadges();
      HashSet<String> correct_badges = new HashSet<>(DEFAULT_BADGES);
      correct_badges.add(BadgeData.GARD_BADG_1);

      Assert.assertTrue(earned_badges.equals(correct_badges));

    } catch (IOException e) {} 
  }

  private static HashMap<String, Integer> getDefaultChallengeStatuses() {
    HashMap<String, Integer> statuses = new HashMap<String, Integer>();
    statuses.put("GARD_0",3);
    statuses.put("FOOD_0",1);
    statuses.put("WAST_0",1);
    return statuses;
  }

  private static HashSet<String> getCompletedChallenges() {
    HashSet<String> completed_challenges = new HashSet<String>();
    completed_challenges.add("GARD_0");
    completed_challenges.add("GARD_1");
    completed_challenges.add("FOOD_0");
    return completed_challenges;
  }

  private static HashSet<String> getDefaultBadges() {
    HashSet<String> badges = new HashSet<String>();
    badges.add(BadgeData.RECY_BADG_1);
    badges.add(BadgeData.WAST_BADG_1);
    return badges;
  }
}