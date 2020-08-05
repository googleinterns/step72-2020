package com.google.sps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.sps.data.ChallengeData;
import com.google.sps.servlets.ChallengesServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.JUnit4;
import org.junit.runner.RunWith;
import org.mockito.Mockito.*;



@RunWith(JUnit4.class)
public final class ChallengesServletTest {
  private ChallengesServlet servlet;
  private String NUM_CHALLNEGES = "3";
  private static final String DEFAULT_CHALLENGE_ID = "GARD_0";
  private static final HashMap<String, Integer> DEFAULT_CHALLENGE_STATUSES = getDefaultChallengeStatuses();

  private User user;
  private JSONObject user_json;

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
    
    user_json = new JSONObject(user.toJSON());  
    servlet = new ChallengesServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void ConvertJavaObjectToJsonTest(){
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(request.getParameter(NUM_CHALLNEGES)).thenReturn("");

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doGet(request, response);
      pw.flush();
      Assert.assertEquals(new Gson().toJson(ChallengeData.CHALLENGES_MAP),
                          writer.toString().trim());
    } catch (IOException e) {}
  }
}