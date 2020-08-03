package com.google.sps.data;

import com.google.sps.data.Challenge;

import java.util.HashSet;

public class Badge {
  private String id;
  private Challenge.Type challenge_type;
  //private int num_to_complete;
  private String description;

  public Badge(Challenge.Type challenge_type, String id, String description){
    this.challenge_type = challenge_type;
    this.id = id;
    this.description = description;
  }
}