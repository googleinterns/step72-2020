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
      new Challenge(Challenge.Type.GARDENING,"Environmentally friendly fertilizer!", STEPS),
      new Challenge(Challenge.Type.RECYCLE, "Old Electronics" ,STEPS),
      new Challenge(Challenge.Type.WASTE, "Create your own compost", STEPS));
  }
  
  @Test
  public void isEqualValidSameInstance(){
    Assert.assertTrue(challenge_list.get(2).isEqual(challenge_list.get(2)));
  }

  @Test
  public void isEqualValidDiffInstance(){
    Assert.assertTrue(Challenges.challenges[1].isEqual(challenge_list.get(1)));
  }

  @Test
  public void isEqualInvalid(){
    Assert.assertFalse(challenge_list.get(0).isEqual(challenge_list.get(1)));
  }

  @Test
  public void toStringTestValid(){
    System.out.println(challenge_list.get(1).toString());
    Assert.assertTrue(challenge_list.get(1).toString().equals("Challenge Type: RECYCLE, Name: Old Electronics"));
  }
}