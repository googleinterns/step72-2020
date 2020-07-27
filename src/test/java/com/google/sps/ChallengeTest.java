package com.google.sps;

import com.google.sps.data.Challenge;
import com.google.sps.data.ChallengeData;
import java.util.Arrays;
import java.util.List;
import javafx.util.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Before;
import org.junit.runners.JUnit4;
import org.junit.runner.RunWith;

@RunWith(JUnit4.class)
public final class ChallengeTest {
  private List<Pair<String,String>> step_desc_pairs;
  private List<Challenge> challenge_list;

  @Before
  public void setup(){
    step_desc_pairs = Arrays.asList(
      new Pair<String, String>("step 1", "Use a reusable bag for Gro"),
      new Pair<String, String>("step 2", "Reduce..."),
      new Pair<String, String>("step 3", "Recylce"));

    challenge_list = Arrays.asList(
      new Challenge(Challenge.Type.GARDENING,"Environmentally friendly fertilizer!", step_desc_pairs),
      new Challenge(Challenge.Type.RECYCLE,"Old Electronics",step_desc_pairs),
      new Challenge(Challenge.Type.WASTE,"Create your own compost", step_desc_pairs));
  }
  
  @Test
  public void isEqualValidSameInstance(){
    Assert.assertTrue(challenge_list.get(2).isEqual(challenge_list.get(2)));
  }

  @Test
  public void isEqualValidDiffInstance(){
    Assert.assertTrue(ChallengeData.CHALLENGES[1].isEqual(challenge_list.get(1)));
  }

  @Test
  public void isEqualInvalid(){
    Assert.assertFalse(challenge_list.get(0).isEqual(challenge_list.get(1)));
  }

  @Test
  public void toStringTestValid(){
    //System.out.println(challenge_list.get(1).toString());
    Assert.assertTrue(challenge_list.get(1).toString().equals("Challenge Type: RECYCLE, Name: Old Electronics"));
  }

  @Test
  public void getTypeTestValid(){
    Assert.assertEquals(challenge_list.get(0).getType(), Challenge.Type.GARDENING);
  }

  @Test
  public void getNameTestValid(){
    Assert.assertEquals(challenge_list.get(0).getName(), "Environmentally friendly fertilizer!");
  }
}