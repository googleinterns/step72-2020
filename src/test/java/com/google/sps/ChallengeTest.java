package com.google.sps;

import com.google.sps.data.Challenge;
import com.google.sps.data.Challenges;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.JUnit4;
import org.junit.runner.RunWith;
import org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public final class ChallengeTest {
  private static final List<String> STEPS = Arrays.asList("step 1", "step 2", "step 3");
  private List<Challenge> challenge_list;

  @Before
  public void setup(){
    challenge_list = Arrays.asList(
      new Challenge("Gardening","Environmentally friendly fertilizer!", STEPS),
      new Challenge("Recycle","Create your own compost" ,STEPS),
      new Challenge("Waste","Old Electronics", STEPS));
  }
  
  @Test
  public void isEqualValid(){
    Assert.assertTrue(challenge_list.get(0).isEqual(challenge_list.get(0)));
    Assert.assertTrue(Challenges.challenges[1].isEqual(challenge_list.get(1)));
  }

  @Test
  public void isEqualInvalid(){
    Assert.assertFalse(challenge_list.get(0).isEqual(challenge_list.get(1)));
  }

  @Test
  public void toStringTestValid(){
    Assert.assertTrue(challenge_list.get(2).toString().equals("Waste"));
    Assert.assertTrue(Challenges.challenges[1].toString().equals("Recycle"));
  }
}