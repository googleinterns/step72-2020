package com.google.sps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.sps.data.Challenges;
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

  @Before
  public void setup(){
    servlet = new ChallengesServlet();
  }

  @Test
  public void ConvertJavaObjectToJsonTest(){
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doGet(request, response);
      pw.flush();

      System.out.println(new Gson().toJson(Challenges.challenges));
      //System.out.println(writer.toString()); 
      Assert.assertEquals(new Gson().toJson(Challenges.challenges),
                          writer.toString().trim());

    } catch (IOException e) {}
  }
}