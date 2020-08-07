package com.google.sps.data;

import com.google.sps.data.Challenge;
import java.util.HashSet;

public class Badge {
  private Challenge.Type challenge_type;
  private String id;
  private String url;
  private String description;

  public Badge(Challenge.Type challenge_type, String id, String url, String description){
    this.id = id;
    this.url = url;
    this.challenge_type = challenge_type;
    this.description = description;
  }
}