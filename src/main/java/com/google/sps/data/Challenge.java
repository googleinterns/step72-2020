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

public final class Challenge {

  public static final String DATA_TYPE = "Challenge";
  public static final String ID = "manual_id";
  public static final String TIMESTAMP = "timestamp";
  public static final String TITLE = "title";
  public static final String ICON = "icon";
  public static final String STEPS = "steps";

  private final long id;
  private final long timestamp;
  private final String title;
  private final String icon;
  private final Map<String, String>[] steps;

  public Event(long id, String title, long timestamp, String icon, Map<String, String>[] steps) {
    this.id = id;
    this.title = title;
    this.timestamp = timestamp;
    this.author = author;
    this.steps = steps.clone();
  }
}