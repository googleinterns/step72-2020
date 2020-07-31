package com.google.sps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.sps.data.User;
import com.google.sps.servlets.UserServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

import com.google.sps.data.MockIdHelper;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.*;

@RunWith(JUnit4.class)
public final class UserServletTest {
  private UserServlet servlet;
  private static final String MOCK_ID_TOKEN = "123";;
  private static final String MOCK_USER_ID = "00";
  private static final String MOCK_NICKNAME = "Name";


  private static final HashMap<String, Integer> DEFAULT_CHALLENGE_STATUSES = new HashMap<String, Integer>();
  static {
    DEFAULT_CHALLENGE_STATUSES.put("GARD_0",0);
    DEFAULT_CHALLENGE_STATUSES.put("RECY_0",0);
    DEFAULT_CHALLENGE_STATUSES.put("WAST_0",0);
  }

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  private User user;
  private JSONObject userJson;

  @Before
  public void setup(){
    helper.setUp();
    servlet = new UserServlet();
    servlet.setIdHelper(new MockIdHelper());
    servlet.setDatastoreService(datastore);

    user = new User.Builder(MOCK_USER_ID)
        .setNickname(MOCK_NICKNAME)
        .setCurrentChallengeId("GARD_0")
        .setChallengeStatuses(DEFAULT_CHALLENGE_STATUSES)
        .build();
    
    userJson = new JSONObject(user.toJSON());
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void getUser(){
    datastore.put(user.toEntity());

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameter(UserServlet.ID_TOKEN_PARAM)).thenReturn(MOCK_ID_TOKEN);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doGet(request, response);
      pw.flush();

      JSONObject responseJson = new JSONObject(writer.toString().trim());
      Assert.assertEquals(userJson.getString(User.ID),
                          responseJson.getString(User.ID));
      Assert.assertEquals(userJson.getString(User.NICKNAME),
                          responseJson.getString(User.NICKNAME));
    } catch (IOException e) {}
  }

  @Test
  public void getNonexistentUser(){
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameter(UserServlet.ID_TOKEN_PARAM)).thenReturn(MOCK_ID_TOKEN);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doGet(request, response);
      pw.flush();

      Assert.assertEquals(writer.toString().trim(), "");

    } catch (IOException e) {}
  }

  @Test
  public void noIdToken(){
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameter(UserServlet.ID_TOKEN_PARAM)).thenReturn(null);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doGet(request, response);
      pw.flush();
      
      Assert.assertEquals(writer.toString().trim(), "");

    } catch (IOException e) {}
  }

  @Test
  public void createUser(){
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameter(UserServlet.ID_TOKEN_PARAM)).thenReturn(MOCK_ID_TOKEN);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      // Confirm matching user does not already exist
      Query query = new Query(User.DATA_TYPE).setFilter(new FilterPredicate(User.ID, FilterOperator.EQUAL, MOCK_USER_ID));
      Entity entity = datastore.prepare(query).asSingleEntity();
      Assert.assertEquals(entity, null);

      servlet.doPost(request, response);
      pw.flush();
      
      query = new Query(User.DATA_TYPE).setFilter(new FilterPredicate(User.ID, FilterOperator.EQUAL, MOCK_USER_ID));
      entity = datastore.prepare(query).asSingleEntity();
      User createdUser = User.convertEntityToUser(entity, MOCK_USER_ID);
      JSONObject createdUserJson = new JSONObject(createdUser.toJSON());
      Assert.assertEquals(userJson.getString(User.ID),
                          createdUserJson.getString(User.ID));
      Assert.assertEquals(userJson.getString(User.NICKNAME),
                          createdUserJson.getString(User.NICKNAME));

    } catch (IOException e) {}
  }

  @Test
  public void updateCurrentChallenge(){
    datastore.put(user.toEntity());
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    String newChallengeId = "RECY_0";

    when(request.getParameter(UserServlet.CHALLENGE_ID_PARAM)).thenReturn(newChallengeId);
    when(request.getParameter(UserServlet.ID_TOKEN_PARAM)).thenReturn(MOCK_ID_TOKEN);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doPut(request, response);
      pw.flush();
      JSONObject responseJson = new JSONObject(writer.toString().trim());
      Assert.assertTrue(responseJson.getString("current_challenge_id").equals(newChallengeId));
    } catch (IOException e) {}
  }

  @Test
  public void updateChallengeStatuses(){
    datastore.put(user.toEntity());
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    String challengeId = "RECY_0";
    String newStatus = "1";

    when(request.getParameter(UserServlet.CHALLENGE_ID_PARAM)).thenReturn(challengeId);
    when(request.getParameter(UserServlet.CHALLENGE_STATUS_PARAM)).thenReturn(newStatus);
    when(request.getParameter(UserServlet.ID_TOKEN_PARAM)).thenReturn(MOCK_ID_TOKEN);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doPut(request, response);
      pw.flush();
      JSONObject responseJson = new JSONObject(writer.toString().trim());
      JSONObject challengeStatuses = responseJson.getJSONObject(User.CHALLENGE_STATUSES);
      Assert.assertTrue((Integer) challengeStatuses.get(challengeId) == Integer.parseInt(newStatus));
      Assert.assertTrue((Integer) challengeStatuses.get("GARD_0") == 0);
      Assert.assertTrue((Integer) challengeStatuses.get("WAST_0") == 0);
    } catch (IOException e) {}
  }


  @Test
  public void addBookmarkedEvent(){
    datastore.put(user.toEntity());
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    String eventId = "5";

    when(request.getParameter(UserServlet.BOOKMARKED_EVENT_PARAM)).thenReturn(eventId);
    when(request.getParameter(UserServlet.ADD_BOOKMARK_PARAM)).thenReturn("true");
    when(request.getParameter(UserServlet.ID_TOKEN_PARAM)).thenReturn(MOCK_ID_TOKEN);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doPut(request, response);
      pw.flush();
      JSONObject responseJson = new JSONObject(writer.toString().trim());
      Assert.assertTrue(responseJson.getJSONArray(User.BOOKMARKED_EVENTS).toString().contains(eventId));
    } catch (IOException e) {}
  }

  @Test
  public void removeBookmarkedEvent(){
    user.setBookmarkedEvents(new ArrayList<Long>(Arrays.asList(5L)));
    datastore.put(user.toEntity());
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    String eventId = "5";

    when(request.getParameter(UserServlet.BOOKMARKED_EVENT_PARAM)).thenReturn(eventId);
    when(request.getParameter(UserServlet.ADD_BOOKMARK_PARAM)).thenReturn("false");
    when(request.getParameter(UserServlet.ID_TOKEN_PARAM)).thenReturn(MOCK_ID_TOKEN);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doPut(request, response);
      pw.flush();
      JSONObject responseJson = new JSONObject(writer.toString().trim());
      Assert.assertTrue(!responseJson.getJSONArray(User.BOOKMARKED_EVENTS).toString().contains(eventId));
    } catch (IOException e) {}
  }

  @Test
  public void updateAddedToCalendarEvents() {
    user.setAddedToCalendarEvents(new ArrayList<Long>(Arrays.asList(5L)));
    datastore.put(user.toEntity());

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    String eventId = "5";

    when(request.getParameter(UserServlet.ADDED_TO_CALENDAR_PARAM)).thenReturn(eventId);
    when(request.getParameter(UserServlet.ID_TOKEN_PARAM)).thenReturn(MOCK_ID_TOKEN);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doPut(request, response);
      pw.flush();
      JSONObject responseJson = new JSONObject(writer.toString().trim());
      Assert.assertTrue(responseJson.getJSONArray(User.ADDED_TO_CALENDAR_EVENTS).toString().contains(eventId));
    } catch (IOException e) {}

  }
}