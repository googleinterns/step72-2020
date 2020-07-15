
package com.google.sps.data;

import java.util.Arrays;
import java.util.List;
import javafx.util.Pair;


public final class ChallengeData {

    //Challenge names
    private static final String GARDENING_CHALLENGE_0 = "Environmentally friendly fertilizer!";
    //private static final String GARDENING_CHALLENGE_1 = "Incorporate native plants!";

    private static final String RECYCLE_CHALLENGE_0 = "Old Electronics";
    //private static final String RECYCLE_CHALLENGE_1 = "Donate";

    private static final String WASTE_CHALLENGE_0 = "Create your own compost";
    //private static final String WASTE_CHALLENGE_1 = "Avoid disposable goods";

    /* Note: step 1, step 2, step 3 are place holders */
    private static final List GARDENING_STEPS_PAIR_0 =
      Arrays.asList(new Pair<String, String>("step 1", "plant the seed"),
      new Pair<String, String>("step 2", "Maintain and water plant"),
      new Pair<String, String>("step 3", "Watch it grow :D"));

    private static final List RECYCLE_STEPS_PAIR_0 =
      Arrays.asList(new Pair<String, String>("step 1", "Use a reusable bag for Gro"),
      new Pair<String, String>("step 2", "Reduce..."),
      new Pair<String, String>("step 3", "Recylce"));

    private static final List WASTE_STEPS_PAIR_0 =
      Arrays.asList(new Pair<String, String>("step 1", "Find a nice dry, shady place near water for your compost"),
      new Pair<String, String>("step 2", "Add chopped/shredded brown and green materials"),
      new Pair<String, String>("step 3", "Mix Grass clippings and green waste into the pile. Also bury fruit and vegetable waste under 10 inches"));

    //might want to consider using map/hashset organize data by type
    //then in the servlet pass one from each set

    public static final Challenge[] challenges = {
        new Challenge(Challenge.Type.GARDENING, GARDENING_CHALLENGE_0, GARDENING_STEPS_PAIR_0),
        new Challenge(Challenge.Type.RECYCLE, RECYCLE_CHALLENGE_0, RECYCLE_STEPS_PAIR_0),
        new Challenge(Challenge.Type.WASTE, WASTE_CHALLENGE_0, WASTE_STEPS_PAIR_0)
    };

    private ChallengeData() {
     //To prevent initialization
    }
}