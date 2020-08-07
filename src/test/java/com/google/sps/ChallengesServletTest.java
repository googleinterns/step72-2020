package com.google.sps;
import com.google.gson.*;
import org.mockito.Mockito.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import com.google.gson.Gson;
import com.google.sps.data.Challenge;
import com.google.sps.data.ChallengeData;
import com.google.sps.data.MockIdHelper;
import com.google.sps.data.User;

import com.google.sps.servlets.ChallengesServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

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
public final class ChallengesServletTest {
  private ChallengesServlet servlet;
  private static final String NUM_CHALLNEGES = "3";

  private static final String MOCK_ID_TOKEN = "123";
  private static final String MOCK_NICKNAME = "name";
  private static final String MOCK_USER_ID = "00";

  private static final HashMap<String, Integer> DEFAULT_CHALLENGE_STATUSES = getDefaultChallengeStatuses();

  private User user;
  private JSONObject user_json;
  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Before
  public void setup(){
    helper.setUp();
    servlet = new ChallengesServlet();
    servlet.setIdHelper(new MockIdHelper());
    servlet.setDatastoreService(datastore);

    user = new User.Builder(MOCK_USER_ID)
        .setNickname(MOCK_NICKNAME)
        .setCurrentChallengeId(ChallengeData.DEF_CURRENT_CHALLENGE_ID)
        .setChallengeStatuses(DEFAULT_CHALLENGE_STATUSES)
        .build();
    
    user_json = new JSONObject(user.toJSON());  
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void convertJavaObjectToJsonTest(){
    datastore.put(user.toEntity());
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(request.getParameter(ChallengesServlet.NUM_CHALLENGES)).thenReturn(NUM_CHALLNEGES);
    when(request.getParameter(ChallengesServlet.ID_TOKEN)).thenReturn(MOCK_ID_TOKEN);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doGet(request, response);
      pw.flush();
      JSONArray response_json = new JSONArray(writer.toString().trim());
      JSONObject challenge_json = 
        new JSONObject(new Gson().toJson(ChallengeData.CHALLENGES_MAP));

      for(int i = 0; i < 3; i++){
        String id = response_json.getJSONObject(i).getString("id");
        Assert.assertEquals(challenge_json.getJSONObject(id).toString(),
                            response_json.getJSONObject(i).toString());
      }
    } catch (IOException e) {}
  }

  @Test
  public void updateChallengeStatusAndCompletedChallenges() {
    datastore.put(user.toEntity());
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    String completed_challenge_id = "RECY_0";
    String current_challenge_id = "GARD_0";
    String completed_challenge_status = "0";

    when(request.getParameter(ChallengesServlet.COMPLETED_CHALLENGE)).thenReturn(completed_challenge_id);
    when(request.getParameter(ChallengesServlet.CURRENT_CHALLENGE)).thenReturn(current_challenge_id);
    when(request.getParameter(ChallengesServlet.ID_TOKEN)).thenReturn(MOCK_ID_TOKEN);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doPut(request, response);
      pw.flush();
      JSONObject response_json = new JSONObject(writer.toString().trim());
      JSONObject challenge_statuses = response_json.getJSONObject(User.CHALLENGE_STATUSES);
      JSONArray completed_challenges = response_json.getJSONArray(User.COMPLETED_CHALLENGES);

      Assert.assertTrue((Integer) challenge_statuses.get(current_challenge_id) == 0);
      Assert.assertTrue((Integer) challenge_statuses.get(completed_challenge_id) == 0);
      Assert.assertTrue((Integer) challenge_statuses.get("WAST_0") == 0);
      Assert.assertTrue((Integer) challenge_statuses.length() == 4);
      Assert.assertEquals(completed_challenges.get(0),completed_challenge_id);
    } catch (IOException e) {}
  }

  private static HashMap<String, Integer> getDefaultChallengeStatuses() {
    HashMap<String, Integer> statuses = new HashMap<String, Integer>();
    statuses.put("GARD_0",0);
    statuses.put("RECY_0",0);
    statuses.put("WAST_0",0);
    return statuses;
  }
}