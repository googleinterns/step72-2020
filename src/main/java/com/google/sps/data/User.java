// // Copyright 2019 Google LLC
// //
// // Licensed under the Apache License, Version 2.0 (the "License");
// // you may not use this file except in compliance with the License.
// // You may obtain a copy of the License at
// //
// //     https://www.apache.org/licenses/LICENSE-2.0
// //
// // Unless required by applicable law or agreed to in writing, software
// // distributed under the License is distributed on an "AS IS" BASIS,
// // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// // See the License for the specific language governing permissions and
// // limitations under the License.

// package com.google.sps.data;
// import java.util.Collection;
// import java.util.Collections;
// import java.util.ArrayList;

// public final class User {

//   public static final String DATA_TYPE = "User";
//   public static final String ID = "manual_id";
//   public static final String NAME = "name";
//   public static final String EMAIL = "email";
//   public static final String CREATED_EVENTS = "created_events";
//   public static final String BOOKMARKED_EVENTS = "bookmarked_events";
//   public static final String CHALLENGE_STATUSES = "challenge_statuses";
//   public static final String CURRENT_CHALLENGE = "current_challenge";

//   private final long id;
//   private final String name;
//   private final String email;
//   private final ArrayList<long> created_events;
//   private final ArrayList<long> bookmarked_events;
//   private final ArrayList<int> challenge_statuses;
//   private final long current_challenge;

//   public User(long id, String name, String email, int numChallenges) {
//     this.id = id;
//     this.name = name;
//     this.email = email;
//     this.created_events = new ArrayList<long>();
//     this.bookmarked_events = new ArrayList<long>();
//     this.challenge_statuses = new ArrayList<int>(Collections.nCopies(numChallenges, 0));
//     this.current_challenge = 0;
//   }
// }