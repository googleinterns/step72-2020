
package com.google.sps.data;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class Challenge {
  public enum Type {
    GARDENING, RECYCLE, WASTE
  }

  private Type challenge_type;
  private String id;
  private String name;

  //<Step name, Step description>
  private List <ImmutablePair<String,String>> steps;

  public Challenge(Type type, String id, String name, List steps){  
    this.challenge_type = type;
    this.id = id; 
    this.name = name;
    this.steps = new ArrayList <>(steps);
  }

  public boolean isEqual(Challenge object) {
    if (object instanceof Challenge){
      Challenge otherChallenge = (Challenge) object;
      if (challenge_type.equals(object.challenge_type) &&
          steps.equals(object.steps) && 
          name.equals(object.name) && id.equals(object.id))
        return true;
      }
    return false; 
  }

  public Type getType() {
    return challenge_type;
  }
  
  public String getName() {
    return name;
  }

  public String getId(){
    return id;
  }

  public String toString() {
      return "Challenge Type: " + challenge_type + ", Name: " + name;
  }
}