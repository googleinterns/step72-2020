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

public final class UserInfo {

  public static final String DATA_TYPE = "UserInfo";
  public static final String ID = "userId";
  public static final String NICKNAME = "nickname";
  public static final String CREATED_EVENTS = "created_events";
  public static final String BOOKMARKED_EVENTS = "bookmarked_events";
  public static final String CURRENT_CHALLENGE = "current_challenge";
  public static final String CHALLENGE_STATUSES = "challenge_statuses";

  private final String id;
  private final String nickname;
  private final List<Long> created_events;
  private final List<Long> bookmarked_events;
  private Long current_challenge_id;
  private final List<Integer> challenge_statuses;


  public UserInfo(String id, String nickname) {
    this.id = id;
    this.nickname = nickname;
    this.created_events = new ArrayList<Long>();
    this.bookmarked_events = new ArrayList<Long>();
    this.current_challenge_id = 0L;
    this.challenge_statuses = new ArrayList<Integer>();
  }

  public UserInfo(String id, String nickname, ArrayList<Long> created_events, 
    ArrayList<Long> bookmarked_events, Long current_challenge_id, ArrayList<Integer> challenge_statuses) {
    this.id = id;
    this.nickname = nickname;
    
    if (created_events == null) this.created_events = new ArrayList<Long>();
    else this.created_events = (ArrayList) created_events.clone();

    if (bookmarked_events == null) this.bookmarked_events = new ArrayList<Long>();
    else this.bookmarked_events = (ArrayList) bookmarked_events.clone();

    this.current_challenge_id = current_challenge_id;
    this.challenge_statuses = (ArrayList) challenge_statuses.clone();
  }

  public Long getCurrentChallenge() {
      return this.current_challenge_id;
  }

  public void setCurrentChallenge(Long chal_id) {
      this.current_challenge_id = chal_id;
  }
}