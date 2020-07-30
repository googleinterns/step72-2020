package com.google.sps.data;

import java.util.HashMap;



public final class BadgeData {
  private static final String GARD_BADG_1 = ;
  private static final String RECY_BADG_1 = ;
  private static final String WAST_BADG_1 = ;

  private static final String GARD_BADG_DESC = "";
  private static final String RECY_BADG_DESC = "";
  private static final String WAST_BADGE_DESC = ""; 
  
  public static Map<String, String> BADGES_MAP = createMap();

  private static Map<String, String> createMap() {
    Map<String, String> badge_map = new HashMap<String, String>();
    badge_map.put(new Badge());
    badge_map.put(new Badge());
    badge_map.put(new Badge());
    return badge_map;
  }

  private BadgeData(){
    //To prevent initialization
  }
}