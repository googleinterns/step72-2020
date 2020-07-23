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
import org.junit.Test;
import org.junit.runners.JUnit4;
import org.junit.runner.RunWith;
import org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;

@RunWith(JUnit4.class)
public final class UserServletTest {
  private UserServlet servlet;

  @Before
  public void setup(){
    servlet = new UserServlet();

    ArrayList<Integer> challengeStatuses = new ArrayList<Integer>(Collections.nCopies(3, 0));

    User user = new User.Builder("00")
        .setNickname("Name")
        .setCurrentChallengeId(0L)
        .setChallengeStatuses(challengeStatuses)
        .build();
  }

  @Test
  public void getUser(){
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameter("id_token")).thenReturn(idToken);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doGet(request, response);
      pw.flush();
    //   Assert.assertEquals(new Gson().toJson(ChallengeData.challenges),
    //                       writer.toString().trim());
    } catch (IOException e) {}
  }

  @Test
  public void updateBookmarkedEvents(){
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameter("book")).thenReturn("5");
    when(request.getParameter("id_token")).thenReturn(idToken);

    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);

    try {
      when(response.getWriter()).thenReturn(pw);
      servlet.doPut(request, response);
      pw.flush();
    //   Assert.assertEquals(new Gson().toJson(ChallengeData.challenges),
    //                       writer.toString().trim());
    } catch (IOException e) {}
  }
}