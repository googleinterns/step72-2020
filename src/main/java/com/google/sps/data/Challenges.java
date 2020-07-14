
package com.google.sps.data;

import java.util.Arrays;
import java.util.List;


public final class Challenges {
    
    //Challenge types
    private static final String GARDENING_TYPE = "Gardening";
    private static final String RECYCLE_TYPE = "Recycle";
    private static final String WASTE_TYPE = "Waste";

    //Challenge names
    private static final String GARDENING_CHALLENGE_0 = "Environmentally friendly fertilizer!";
    //private static final String GARDENING_CHALLENGE_1 = "Incorporate native plants!";

    private static final String RECYCLE_CHALLENGE_0 = "Old Electronics";
    //private static final String RECYCLE_CHALLENGE_1 = "Donate";

    private static final String WASTE_CHALLENGE_0 = "Create your own compost";
    //private static final String WASTE_CHALLENGE_1 = "Avoid disposable goods";

    //Challenge steps
    private static final List GARDENING_STEPS_0 = Arrays.asList("step 1", "step 2", "step 3");
    private static final List RECYCLE_STEPS_0 = Arrays.asList("step 1", "step 2", "step 3");
    private static final List WASTE_STEPS_0 = Arrays.asList("step 1", "step 2", "step 3");

    public static final Challenge[] challenges = {
        new Challenge(Challenge.Type.GARDENING, GARDENING_CHALLENGE_0, GARDENING_STEPS_0),
        new Challenge(Challenge.Type.RECYCLE, RECYCLE_CHALLENGE_0, RECYCLE_STEPS_0),
        new Challenge(Challenge.Type.WASTE, WASTE_CHALLENGE_0, WASTE_STEPS_0)
    };

    private Challenges() {
     //To prevent initialization
    }
}