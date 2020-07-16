// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.sps.data.Challenge;
import com.google.sps.data.ChallengeData;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that manages challenges **/
@WebServlet("/challenges")
public class ChallengesServlet extends HttpServlet {
  private static final int NO_CHALLENGES = 0;
  private static final String NUM_CHALLENGES = "";
  private List<String> requested_challenge_list;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int num_challenges = getNumChallenges(NUM_CHALLENGES);

    String json = convertToJsonUsingGson(requested_challenge_list);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

  }

  /** Converts array of challenges into json format */
  private String convertToJsonUsingGson(Challenge [] challenge_list){
    String json = new Gson().toJson(challenge_list);
    return json;
  }

  private void appendChallengesToList(int num_challenges){
    
    requested_challenge_list = new ArrayList<>();
    int count = 0; 
    for(int i = 0; i < ChallengeData.CHALLENGES.length; i++){
      requested_challenge_list.add();
    }
  }

  /**Returns the number of challenges requested; return 0 if number was invalid */

  /* Future reference for refactor: 
  doGet(...,...) {
    int num_gardening_challenges = getNumChallenges(request.getParameter(NUM_GARDENING_CHALLENGES), ChallengeData.GARDENING);
    int num_waste_challenges = ...
    int ...   
    }
  
  getNumChallenges (String num_challenges, List challenges) {}
    
  
  */
  private getNumChallenges(HttpServletRequest request){
    String num_of_challenges_requested = request.getParameter(NUM_CHALLENGES);

    //convert num_challenges to an int
    int num_challenges;
    try{
      num_challenges = Integer.parseInt(num_of_challenges_requested);
    } catch(NumberFormatException e){
        System.err.println("Could not convert to int " + num_of_challenges_requested);
        return NO_CHALLENGES;
    }

    if (num_challenges > ChallengeData.challenges.length){
      return NO_CHALLENGES; 
    }
    return num_challenges
  }
}