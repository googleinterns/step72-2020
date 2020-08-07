package com.google.sps.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;

public final class ChallengeData {

    //Map keys
    private static final String FOOD_0 = "FOOD_0";
    private static final String FOOD_1 = "FOOD_1";
    private static final String GARD_0 = "GARD_0";
    private static final String GARD_1 = "GARD_1";
    private static final String RECY_0 = "RECY_0";
    private static final String RECY_1 = "RECY_1";
    private static final String WAST_0 = "WAST_0";
    private static final String WAST_1 = "WAST_1";

    //Default key
    public static final String DEF_CURRENT_CHALLENGE_ID = RECY_0;

    //Challenge names
    private static final String FOOD_CHALLENGE_0 = "Don't waste leftovers";
    private static final String FOOD_CHALLENGE_1 = "Reduce meat consumption";

    private static final String GARDENING_CHALLENGE_0 = "Environmentally friendly fertilizer!";
    private static final String GARDENING_CHALLENGE_1 = "Incorporate native plants!";

    private static final String RECYCLE_CHALLENGE_0 = "Old Electronics";
    private static final String RECYCLE_CHALLENGE_1 = "Donate";

    private static final String WASTE_CHALLENGE_0 = "Create your own compost";
    private static final String WASTE_CHALLENGE_1 = "Avoid disposable goods";

    /* Note: step 1, step 2, step 3 are place holders */
    private static final List FOOD_STEPS_0 = 
      Arrays.asList(new ImmutablePair<String, String>("Step 1", "Keep a list of meals and their ingredients that your household enjorys"),
      new ImmutablePair<String, String>("Step 2", "Make shopping list based on number of meals you will eat at home"),
      new ImmutablePair<String, String>("Step 3", "Freeze and preserve fruits and vegetables."));

    private static final List FOOD_STEPS_1 = 
      Arrays.asList(new ImmutablePair<String, String>("Step 1", "Meat and dairy products are extremely environmentally expensive. Try out Meatless Mondays :D"),
      new ImmutablePair<String, String>("Step 2", "Awesome!! Try going meatless for two days this Week! Try to Reduce milk consumption too"),
      new ImmutablePair<String, String>("Step 3", "Doing Great!! Try not consume any animal products this entire week."));

    private static final List GARDENING_STEPS_0 =
      Arrays.asList(new ImmutablePair<String, String>("Step 1", "fertilize 1"),
      new ImmutablePair<String, String>("Step 2", "fertilize 2"),
      new ImmutablePair<String, String>("Step 3", "fertilize 3"));

    private static final List GARDENING_STEPS_1 =
      Arrays.asList(new ImmutablePair<String, String>("Step 1", "plant the seed"),
      new ImmutablePair<String, String>("Step 2", "Maintain and water plant"),
      new ImmutablePair<String, String>("Step 3", "Watch it grow :D"));
    
    private static final List RECYCLE_STEPS_0 =
      Arrays.asList(new ImmutablePair<String, String>("Step 1", "Use a reusable bag for Gro"),
      new ImmutablePair<String, String>("Step 2", "Reduce..."),
      new ImmutablePair<String, String>("Step 3", "Recylce"));

    private static final List RECYCLE_STEPS_1 =
      Arrays.asList(new ImmutablePair<String, String>("Step 1", "Clothing Donations Help Reduce landfills"),
      new ImmutablePair<String, String>("Step 2", "Clothing Donations Help Reduce Carbon Footprint"),
      new ImmutablePair<String, String>("Step 3", "Clothing  Donations Save Water"));

    private static final List WASTE_STEPS_0 =
      Arrays.asList(new ImmutablePair<String, String>("Step 1", "Find a nice dry, shady place near water for your compost"),
      new ImmutablePair<String, String>("Step 2", "Add chopped/shredded brown and green materials"),
      new ImmutablePair<String, String>("Step 3", "Mix Grass clippings and green waste into the pile. Also bury fruit and vegetable waste under 10 inches"));

    private static final List WASTE_STEPS_1 =
      Arrays.asList(new ImmutablePair<String, String>("Step 1", "Replace bottled water with a reusable water bottle"),
      new ImmutablePair<String, String>("Step 2", "Invest in a cloth bag to use for groceries instead of accumulating plastic grocery bags"),
      new ImmutablePair<String, String>("Step 3", "Use containers to store leftovers and lunches instead of wasteful plastic wraps."));

    //Each challenge will be linked to a unique id
    public static final HashMap<String,Challenge> CHALLENGES_MAP = createMap();
    public static final HashMap<String, Integer> DEF_CHALLENGES_AND_STATUSES = createDefaultStatuses();

    //Challenge map used for Servlet Test
    public static final HashMap<String, Challenge> CHALLENGE_MAP_TEST = createChallengeMapTest();

    /*To avoid unwanted behavior, a function is used for initialization. */
    private static HashMap<String, Challenge> createMap(){
      HashMap<String,Challenge> challenge_map = new HashMap<String, Challenge>();
      challenge_map.put(GARD_0,new Challenge(Challenge.Type.GARDENING,GARD_0,GARDENING_CHALLENGE_0, GARDENING_STEPS_0));
      challenge_map.put(GARD_1,new Challenge(Challenge.Type.GARDENING,GARD_1,GARDENING_CHALLENGE_1, GARDENING_STEPS_1));
      challenge_map.put(RECY_0,new Challenge(Challenge.Type.RECYCLE,RECY_0,RECYCLE_CHALLENGE_0, RECYCLE_STEPS_0));
      challenge_map.put(RECY_1,new Challenge(Challenge.Type.RECYCLE,RECY_1,RECYCLE_CHALLENGE_1, RECYCLE_STEPS_1));        
      challenge_map.put(WAST_0,new Challenge(Challenge.Type.WASTE,WAST_0,WASTE_CHALLENGE_0, WASTE_STEPS_0));
      challenge_map.put(WAST_1,new Challenge(Challenge.Type.WASTE,WAST_1,WASTE_CHALLENGE_1, WASTE_STEPS_1));
      challenge_map.put(FOOD_0,new Challenge(Challenge.Type.FOOD,FOOD_0,FOOD_CHALLENGE_0,FOOD_STEPS_0));
      challenge_map.put(FOOD_1,new Challenge(Challenge.Type.FOOD,FOOD_1,FOOD_CHALLENGE_1,FOOD_STEPS_1));
      return challenge_map;
    }

    private static HashMap<String, Integer> createDefaultStatuses(){
      HashMap<String,Integer> statuses = new HashMap<String, Integer>();
      statuses.put(RECY_0,0);
      statuses.put(GARD_0,0);
      statuses.put(WAST_0,0);
      return statuses;
    }

   private static HashMap<String, Challenge> createChallengeMapTest() {
    HashMap<String, Challenge> def_chal_map = new HashMap<String, Challenge>();
    def_chal_map.put(GARD_0,new Challenge(Challenge.Type.GARDENING,GARD_0,GARDENING_CHALLENGE_0, GARDENING_STEPS_0));
    def_chal_map.put(RECY_0,new Challenge(Challenge.Type.RECYCLE,RECY_0,RECYCLE_CHALLENGE_0, RECYCLE_STEPS_0));
    def_chal_map.put(WAST_0,new Challenge(Challenge.Type.WASTE,WAST_0,WASTE_CHALLENGE_0, WASTE_STEPS_0));
    return def_chal_map;
  }
    private ChallengeData() {
     //To prevent initialization
    }
}