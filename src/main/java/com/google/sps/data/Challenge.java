
package com.google.sps.data;

import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;

public class Challenge {
  public enum Type {
    GARDENING, RECYCLE, WASTE
  }

  private Type challenge_type;
  private String name;

  //<Step name, Step description>
  private List <Pair<String,String>> steps_desc_pair;

  public Challenge(Type type, String name, List steps){  
    this.challenge_type = type;
    this.name = name;
    this.steps_desc_pair = new ArrayList <>(steps);
  }

  public boolean isEqual(Challenge object) {
    if (object instanceof Challenge){
      Challenge otherChallenge = (Challenge) object;
      if (challenge_type.equals(object.challenge_type) &&
          steps_desc_pair.equals(object.steps_desc_pair) && 
          name.equals(object.name))
        return true;
      }
    return false; 
  }

  public String toString() {
      return "Challenge Type: " + challenge_type + ", Name: " + name;
  }
}
