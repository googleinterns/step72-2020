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
import org.json.JSONObject;

import com.google.appengine.api.datastore.Entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public final class UserTest {
  private String userId = "00";

  private User user;
  private JSONObject userJson;

  @Before
  public void setup(){
    ArrayList<Integer> challengeStatuses = new ArrayList<Integer>(Collections.nCopies(3, 0));

    user = new User.Builder(userId)
        .setNickname("Name")
        .setCurrentChallengeId(0L)
        .setChallengeStatuses(challengeStatuses)
        .build();
    
    userJson = new JSONObject(user.toJSON());
  }

  @Test
  public void userEqualsTest(){
    ArrayList<Integer> challengeStatuses = new ArrayList<Integer>(Collections.nCopies(3, 0));
    User other = new User.Builder(userId)
        .setNickname("Name")
        .setCurrentChallengeId(0L)
        .setCreatedEvents(new ArrayList<Long>())
        .setChallengeStatuses(challengeStatuses)
        .build();

        Assert.assertTrue(user.equals(other));
  }

  @Test
  public void userNotEqualsTest(){
    ArrayList<Integer> challengeStatuses = new ArrayList<Integer>(Collections.nCopies(3, 0));
    User other = new User.Builder(userId)
        .setNickname("Name")
        .setCurrentChallengeId(0L)
        .setCreatedEvents(new ArrayList<Long>(Arrays.asList(1L)))
        .setChallengeStatuses(challengeStatuses)
        .build();

        Assert.assertTrue(!user.equals(other));
  }

  @Test
  public void userCurrentChallengeTest(){
    user.setCurrentChallenge(1L);
    Assert.assertTrue(1L == user.getCurrentChallenge());
  }

  @Test
  public void userCreatedEventsTest(){
    user.setCreatedEvents(new ArrayList<Long>(Arrays.asList(1L, 2L, 3L)));
    Assert.assertEquals(new ArrayList<Long>(Arrays.asList(1L, 2L, 3L)), user.getCreatedEvents());
  }

//   @Test
//   public void convertEntityToUserTest(){
//     Entity entity;
//     entity.setProperty(User.NICKNAME, "Name");
//     entity.setProperty(User.CURRENT_CHALLENGE, 0L);
//     entity.setProperty(User.CHALLENGE_STATUSES, new ArrayList<Integer>(Collections.nCopies(3, 0)));
//     convertEntityToUser(entity, userId);

//   }
}