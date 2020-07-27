package com.google.sps.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javafx.util.Pair;

public final class ChallengeData {

    //Map keys
    private static final String GARD_0 = "GARD_0";
    private static final String GARD_1 = "GARD_1";
    private static final String RECY_0 = "RECY_0";
    private static final String RECY_1 = "RECY_1";
    private static final String WAST_0 = "WAST_0";
    private static final String WAST_1 = "WAST_1";

    //Challenge names
    private static final String GARDENING_CHALLENGE_0 = "Environmentally friendly fertilizer!";
    private static final String GARDENING_CHALLENGE_1 = "Incorporate native plants!";

    private static final String RECYCLE_CHALLENGE_0 = "Old Electronics";
    private static final String RECYCLE_CHALLENGE_1 = "Donate";

    private static final String WASTE_CHALLENGE_0 = "Create your own compost";
    private static final String WASTE_CHALLENGE_1 = "Avoid disposable goods";

    /* Note: step 1, step 2, step 3 are place holders */
    private static final List GARDENING_STEPS_0 =
      Arrays.asList(new Pair<String, String>("step 1", "fertilize 1"),
      new Pair<String, String>("step 2", "fertilize 2"),
      new Pair<String, String>("step 3", "fertilize 3"));

    private static final List GARDENING_STEPS_1 =
      Arrays.asList(new Pair<String, String>("step 1", "plant the seed"),
      new Pair<String, String>("step 2", "Maintain and water plant"),
      new Pair<String, String>("step 3", "Watch it grow :D"));
    
    private static final List RECYCLE_STEPS_0 =
      Arrays.asList(new Pair<String, String>("step 1", "Use a reusable bag for Gro"),
      new Pair<String, String>("step 2", "Reduce..."),
      new Pair<String, String>("step 3", "Recylce"));

    private static final List RECYCLE_STEPS_1 =
      Arrays.asList(new Pair<String, String>("step 1", "Clothing Donations Help Reduce landfills"),
      new Pair<String, String>("step 2", "Clothing Donations Help Reduce Carbon Footprint"),
      new Pair<String, String>("step 3", "Clothing  Donations Save Water"));

    private static final List WASTE_STEPS_0 =
      Arrays.asList(new Pair<String, String>("step 1", "Find a nice dry, shady place near water for your compost"),
      new Pair<String, String>("step 2", "Add chopped/shredded brown and green materials"),
      new Pair<String, String>("step 3", "Mix Grass clippings and green waste into the pile. Also bury fruit and vegetable waste under 10 inches"));

    private static final List WASTE_STEPS_1 =
      Arrays.asList(new Pair<String, String>("step 1", "Replace bottled water with a reusable water bottle"),
      new Pair<String, String>("step 2", "Invest in a cloth bag to use for groceries instead of accumulating plastic grocery bags"),
      new Pair<String, String>("step 3", "Use containers to store leftovers and lunches instead of wasteful plastic wraps."));

    
    public static final Map<String,Challenge> CHALLENGES_MAP = createMap();

    public static final Challenge[] CHALLENGES = {
        new Challenge(Challenge.Type.GARDENING, GARDENING_CHALLENGE_0, GARDENING_STEPS_0),
        new Challenge(Challenge.Type.RECYCLE, RECYCLE_CHALLENGE_0, RECYCLE_STEPS_0),
        new Challenge(Challenge.Type.WASTE, WASTE_CHALLENGE_0, WASTE_STEPS_0),
        new Challenge(Challenge.Type.GARDENING, GARDENING_CHALLENGE_1, GARDENING_STEPS_1),
        new Challenge(Challenge.Type.RECYCLE, RECYCLE_CHALLENGE_1, RECYCLE_STEPS_1),
        new Challenge(Challenge.Type.WASTE, WASTE_CHALLENGE_1, WASTE_STEPS_1)
    };


    /*To avoid unwanted behavior, a function is used for initialization. */
    private static Map<String, Challenge> createMap(){
      Map<String,Challenge> my_map = new HashMap<String, Challenge>();
      my_map.put(GARD_0,new Challenge(Challenge.Type.GARDENING, GARDENING_CHALLENGE_0, GARDENING_STEPS_0));
      my_map.put(GARD_1,new Challenge(Challenge.Type.GARDENING, GARDENING_CHALLENGE_1, GARDENING_STEPS_1));
      my_map.put(RECY_0,new Challenge(Challenge.Type.RECYCLE, RECYCLE_CHALLENGE_0, RECYCLE_STEPS_0));
      my_map.put(RECY_1,new Challenge(Challenge.Type.RECYCLE, RECYCLE_CHALLENGE_0, RECYCLE_STEPS_0));        
      my_map.put(WAST_0,new Challenge(Challenge.Type.WASTE, WASTE_CHALLENGE_0, WASTE_STEPS_0));
      my_map.put(WAST_1,new Challenge(Challenge.Type.WASTE, WASTE_CHALLENGE_1, WASTE_STEPS_1));
      return my_map;
    }

    private ChallengeData() {
     //To prevent initialization
    }
}