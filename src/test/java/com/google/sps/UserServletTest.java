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

@RunWith(JUnit4.class)
public final class UserServletTest {
  private UserServlet servlet;
  private String idToken = "123";;
  private String userId = "00";

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

    ArrayList<Integer> challengeStatuses = new ArrayList<Integer>(Collections.nCopies(3, 0));

    user = new User.Builder(userId)
        .setNickname("Name")
        .setCurrentChallengeId(0L)
        .setChallengeStatuses(challengeStatuses)
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

    when(request.getParameter("id_token")).thenReturn(idToken);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doGet(request, response);
      pw.flush();

      JSONObject responseJson = new JSONObject(writer.toString().trim());

      Assert.assertEquals(userJson.getString("id"),
                          responseJson.getString("id"));
      Assert.assertEquals(userJson.getString("nickname"),
                          responseJson.getString("nickname"));
    } catch (IOException e) {}
  }

  @Test
  public void createUser(){
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameter("id_token")).thenReturn(idToken);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      // Confirm matching user does not already exist
      Query query = new Query(User.DATA_TYPE).setFilter(new FilterPredicate(User.ID, FilterOperator.EQUAL, userId));
      Entity entity = datastore.prepare(query).asSingleEntity();
      Assert.assertEquals(entity, null);

      servlet.doPost(request, response);
      pw.flush();
      
      query = new Query(User.DATA_TYPE).setFilter(new FilterPredicate(User.ID, FilterOperator.EQUAL, userId));
      entity = datastore.prepare(query).asSingleEntity();
      System.out.println(entity);
      User createdUser = User.convertEntityToUser(entity, userId);
      JSONObject createdUserJson = new JSONObject(createdUser.toJSON());
      Assert.assertEquals(userJson.getString("id"),
                          createdUserJson.getString("id"));
      Assert.assertEquals(userJson.getString("nickname"),
                          createdUserJson.getString("nickname"));

    } catch (IOException e) {}
  }

  @Test
  public void updateCurrentChallenge(){
    datastore.put(user.toEntity());
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    String newChallengeId = "5";

    when(request.getParameter("chal")).thenReturn(newChallengeId);
    when(request.getParameter("id_token")).thenReturn(idToken);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doPut(request, response);
      pw.flush();
      JSONObject responseJson = new JSONObject(writer.toString().trim());
      System.out.println(responseJson);
      Assert.assertTrue(responseJson.getJSONArray("current_challenge_id").toString() == newChallengeId);
    } catch (IOException e) {}
  }


  @Test
  public void addBookmarkedEvent(){
    datastore.put(user.toEntity());
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    String eventId = "5";

    when(request.getParameter("book")).thenReturn(eventId);
    when(request.getParameter("add")).thenReturn("true");
    when(request.getParameter("id_token")).thenReturn(idToken);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doPut(request, response);
      pw.flush();
      JSONObject responseJson = new JSONObject(writer.toString().trim());
      System.out.println(responseJson);
      Assert.assertTrue(responseJson.getJSONArray("bookmarked_events").toString().contains(eventId));
    } catch (IOException e) {}
  }

  @Test
  public void removeBookmarkedEvent(){
    user.setBookmarkedEvents(new ArrayList<Long>(Arrays.asList(5L)));
    datastore.put(user.toEntity());
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    String eventId = "5";

    when(request.getParameter("book")).thenReturn(eventId);
    when(request.getParameter("add")).thenReturn("false");
    when(request.getParameter("id_token")).thenReturn(idToken);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doPut(request, response);
      pw.flush();
      JSONObject responseJson = new JSONObject(writer.toString().trim());
      System.out.println(responseJson);
      Assert.assertTrue(!responseJson.getJSONArray("bookmarked_events").toString().contains(eventId));
    } catch (IOException e) {}
  }
}