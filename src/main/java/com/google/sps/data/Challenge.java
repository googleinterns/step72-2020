
package com.google.sps.data;

import java.util.ArrayList;
import java.util.List;


public class Challenge {
  public enum Type {
    GARDENING, RECYCLE, WASTE
  }

  private Type challenge_type;
  private String name;
  private List <String> steps;

  public Challenge(Type type, String name, List steps){  
    this.challenge_type = type;
    this.name = name;
    this.steps = new ArrayList <>(steps);
  }

  public boolean isEqual(Challenge object) {
    if (object instanceof Challenge){
      Challenge otherChallenge = (Challenge) object;
      if (challenge_type.equals(object.challenge_type) &&
        steps.equals(object.steps) && name.equals(object.name))
        return true;
      }
    return false; 
  }

  public String toString() {
      return "Challenge Type: " + challenge_type + ", Name: " + name;
  }
}
