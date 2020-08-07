package com.google.sps.data;

import java.util.HashMap;

public final class BadgeData {
  public static final String GARD_BADG_1 = "GARD_BADG_1";
  public static final String RECY_BADG_1 = "RECY_BADG_1";
  public static final String WAST_BADG_1 = "WAST_BADG_1";

  public static final String GARD_BADG_1_URL = "resources/gard_medal_1.png";
  public static final String RECY_BADG_1_URL = "resources/recy_medal_1.png";
  public static final String WAST_BADG_1_URL = "resources/wast_medal_1.png";

  public static final String GARD_BADG_3_URL = "resources/gard_medal_3.png";
  public static final String RECY_BADG_3_URL = "resources/gard_medal_3.png";

  public static final String GARD_BADG_3 = "GARD_BADG_3";
  public static final String RECY_BADG_3 = "RECY_BADG_3";
  public static final String WAST_BADG_3 = "WAST_BADG_3";

  private static final String YOU_COMPLETE_ONE = "You've Completed 1 ";
  private static final String YOU_COMPLETE_THREE =  "You've Completed 3 ";

  private static final String GARDENING_CHALLENGE = "Gardening Challenge";
  private static final String RECYCLE_CHALLENGE = "Recycle Challenge";
  private static final String WASTE_CHALLENGE = "Waste Challenge";

  public static HashMap<String, Badge> BADGE_MAP = createMap();

  private static HashMap<String, Badge> createMap() {
    HashMap<String, Badge> badge_map = new HashMap<String, Badge>();
    badge_map.put(GARD_BADG_1,new Badge(Challenge.Type.GARDENING,GARD_BADG_1,GARD_BADG_1_URL,YOU_COMPLETE_ONE + GARDENING_CHALLENGE));
    badge_map.put(RECY_BADG_1,new Badge(Challenge.Type.RECYCLE,RECY_BADG_1,RECY_BADG_1_URL,YOU_COMPLETE_ONE + RECYCLE_CHALLENGE));
    badge_map.put(WAST_BADG_1,new Badge(Challenge.Type.WASTE,WAST_BADG_1,WAST_BADG_1_URL,YOU_COMPLETE_ONE + WASTE_CHALLENGE));
    return badge_map;
  } 

  private BadgeData(){
    //To prevent initialization
  }
}