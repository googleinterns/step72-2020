
package com.google.sps;

import java.util.List;
import com.google.sps.data.Challenge;
import org.junit.Assert;
import java.util.Arrays;
import com.google.sps.data.Challenges;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito.*;


@RunWith(JUnit4.class)
public final class ChallengeTest {

  //private Challenge challenge;
  private static final List<String> STEPS = Arrays.asList("step 1", "step 2", "step 3");
  private List<Challenge> challengeList = Arrays.asList(
    new Challenge("Gardening","Environmentally friendly fertilizer!", STEPS),
    new Challenge("Recycle","Create your own compost" ,STEPS),
    new Challenge("Waste","Old Electronics", STEPS));
  
  @Test
  public void isEqualValid(){
    Assert.assertTrue(challengeList.get(0).isEqual(challengeList.get(0)));
    Assert.assertTrue(Challenges.challenges[1].isEqual(challengeList.get(1)));
  }

  @Test
  public void isEqualInvalid(){
    Assert.assertFalse(challengeList.get(0).isEqual(challengeList.get(1)));
  }

  @Test
  public void toStringTestValid(){
    Assert.assertTrue(challengeList.get(2).toString().equals("Waste"));
    Assert.assertTrue(Challenges.challenges[1].toString().equals("Recycle"));
  }
}