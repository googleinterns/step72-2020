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

package com.google.sps.data;

import java.util.ArrayList;
import java.util.List;

public final class User {

  public static String DATA_TYPE = "User";
  public static String EMAIL = "email";
  public static String NICKNAME = "nickname";
  public static String CREATED_EVENTS = "created_events";
  public static String BOOKMARKED_EVENTS = "bookmarked_events";
  public static String CURRENT_CHALLENGE = "current_challenge";
  public static String CHALLENGE_STATUSES = "challenge_statuses";

  private final String email;
  private final String nickname;
  private final ArrayList<Long> created_events;
  private final ArrayList<Long> bookmarked_events;
  private int current_challenge_id;
  private final ArrayList<Integer> challenge_statuses;


  public User(String email, String nickname) {
    this.email = email;
    this.nickname = nickname;
    this.created_events = new ArrayList<Long>();
    this.bookmarked_events = new ArrayList<Long>();
    this.current_challenge_id = 0;
    this.challenge_statuses = new ArrayList<Integer>();
  }

  public int getCurrentChallenge() {
      return this.current_challenge_id;
  }

  public void setCurrentChallenge(int chal_id) {
      this.current_challenge_id = chal_id;
  }
}