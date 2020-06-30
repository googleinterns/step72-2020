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

public final class Event {

  public static final String DATA_TYPE = "Event";
  public static final String ID = "manual_id";
  public static final String TIMESTAMP = "timestamp";
  public static final String TITLE = "title";
  public static final String AUTHOR = "author";
  public static final String DATE = "date";
  public static final String LOCATION = "location";
  public static final String DESCRIPTION = "description";
  public static final String CATEGORY = "category";

  private final long id;
  private final long timestamp;
  private final String title;
  private final long author;
  private final String date;
  private final String location;
  private final String description;
  private final String category;

  public Event(long id, String title, long timestamp, String author, String date, String location, String description, String category) {
    this.id = id;
    this.title = title;
    this.timestamp = timestamp;
    this.author = author;
    this.date = date;
    this.location = location;
    this.description = description;
    this.category = category;
  }
}