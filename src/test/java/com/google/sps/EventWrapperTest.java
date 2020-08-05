package com.google.sps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.sps.data.MockIdHelper;
import com.google.sps.data.EventWrapper;
import com.google.sps.data.User;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Event.ExtendedProperties;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.sps.servlets.EventsServlet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito.*;

import org.json.JSONObject;

import java.util.Date;
import com.google.api.client.util.DateTime;

@RunWith(JUnit4.class)
public final class EventWrapperTest {
    private EventWrapper mockEventWrapper;
    private EventsServlet servlet;
    
    // Creates a mock user entity in order to test the getEventCreatorName() method in EventWrapper class
    private User mockUser;
    
    private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    private static final String MOCK_DATE = "2020-07-01";
    private static final String MOCK_TIME = "14:00";
    private static final String MOCK_TIMEZONE = "0";
    private static final String UTC_TIMEZONE = "UTC";
    
    private static final String MOCK_ID_TOKEN = "123";
    private static final String MOCK_SUMMARY = "Test Event";
    private static final String MOCK_DESCRIPTION = "Description";
    private static final String MOCK_LOCATION = "Mountain View, CA";
    private static final String MOCK_CATEGORY = "other";
    private static final String MOCK_USER_ID = "00";
    private static final String MOCK_NICKNAME = "Name";
    private static final Long MOCK_BOOKMARKS = 0L;

    private static final String DEFAULT_CHALLENGE_ID = "GARD_0";;
    private static final HashMap<String, Integer> DEFAULT_CHALLENGE_STATUSES = getDefaultChallengeStatuses();
     
    private static Date MOCK_START;
    private static Date MOCK_END;
    
  @Before
  public void setup(){
    helper.setUp();
    
    servlet = new EventsServlet();
    MOCK_START = servlet.getEventDateTime(MOCK_DATE, MOCK_TIME, MOCK_TIMEZONE);
    MOCK_END = servlet.getEventDateTime(MOCK_DATE, MOCK_TIME, MOCK_TIMEZONE);
    
    mockEventWrapper = new EventWrapper.Builder()
        .setSummary(MOCK_SUMMARY)
        .setDescription(MOCK_DESCRIPTION)
        .setLocation(MOCK_LOCATION)
        .setStartDateTime(MOCK_START)
        .setEndDateTime(MOCK_END)
        .setCategory(MOCK_CATEGORY)
        .setCreator(MOCK_USER_ID)
        .build();
    
    mockUser = new User.Builder(MOCK_USER_ID)
        .setNickname(MOCK_NICKNAME)
        .setCurrentChallengeId(DEFAULT_CHALLENGE_ID)
        .setChallengeStatuses(DEFAULT_CHALLENGE_STATUSES)
        .build();
        
    datastore.put(mockUser.toEntity());
  }

  @After
  public void tearDown() {
      helper.tearDown();
  }

  @Test
  public void eventEqualsTest(){
    EventWrapper other = new EventWrapper.Builder()
        .setSummary(MOCK_SUMMARY)
        .setDescription(MOCK_DESCRIPTION)
        .setLocation(MOCK_LOCATION)
        .setStartDateTime(MOCK_START)
        .setEndDateTime(MOCK_END)
        .setCategory(MOCK_CATEGORY)
        .setCreator(MOCK_USER_ID)
        .setBookmarks(MOCK_BOOKMARKS)
        .build();
    Assert.assertTrue(mockEventWrapper.equals(other));
  }

  @Test
  public void eventSummaryNotEqualsTest(){
    EventWrapper other = new EventWrapper.Builder()
        .setSummary("Other Event")
        .setDescription(MOCK_DESCRIPTION)
        .setLocation(MOCK_LOCATION)
        .setStartDateTime(MOCK_START)
        .setEndDateTime(MOCK_END)
        .setCategory(MOCK_CATEGORY)
        .setCreator(MOCK_USER_ID)
        .build();
    Assert.assertFalse(mockEventWrapper.equals(other));
  }
  
  @Test
  public void eventDateNotEqualsTest(){
    EventWrapper other = new EventWrapper.Builder()
        .setSummary(MOCK_SUMMARY)
        .setDescription(MOCK_DESCRIPTION)
        .setLocation(MOCK_LOCATION)
        .setStartDateTime(servlet.getEventDateTime(MOCK_DATE, MOCK_TIME, "480"))
        .setEndDateTime(MOCK_END)
        .setCategory(MOCK_CATEGORY)
        .setCreator(MOCK_USER_ID)
        .build();
    Assert.assertFalse(mockEventWrapper.equals(other));
  }

  @Test
  public void convertEventWrapperToEvent(){
       Event expected = getExpectedEvent();
       
       Event result = mockEventWrapper.toEvent();
       
       Assert.assertEquals(expected.getSummary(), result.getSummary());
       Assert.assertEquals(expected.getDescription(), result.getDescription());
       Assert.assertEquals(expected.getLocation(), result.getLocation());
       Assert.assertEquals(expected.getStart(), result.getStart());
       Assert.assertEquals(expected.getEnd(), result.getEnd());
       Assert.assertEquals(expected.getExtendedProperties().get(EventWrapper.CATEGORY), result.getExtendedProperties().get(EventWrapper.CATEGORY));
       Assert.assertEquals(expected.getExtendedProperties().get(EventWrapper.EVENT_CREATOR), result.getExtendedProperties().get(EventWrapper.EVENT_CREATOR));
       Assert.assertEquals(expected.getExtendedProperties().get(EventWrapper.BOOKMARKS), result.getExtendedProperties().get(EventWrapper.BOOKMARKS));
  }

  @Test
  public void convertEntityToEvent(){
    Event expected = getExpectedEvent();
    
    Entity entity = new Entity(EventWrapper.EVENT);
    entity.setProperty(EventWrapper.SUMMARY, MOCK_SUMMARY);
    entity.setProperty(EventWrapper.DESCRIPTION, MOCK_DESCRIPTION);
    entity.setProperty(EventWrapper.LOCATION, MOCK_LOCATION);
    entity.setProperty(EventWrapper.START_TIME, MOCK_START);
    entity.setProperty(EventWrapper.END_TIME, MOCK_END);
    entity.setProperty(EventWrapper.EVENT_CREATOR, MOCK_USER_ID);
    entity.setProperty(EventWrapper.CATEGORY, MOCK_CATEGORY);
    entity.setProperty(EventWrapper.BOOKMARKS, MOCK_BOOKMARKS);
    
    Event result = EventWrapper.convertEntityToEvent(entity);
    
    
    Assert.assertEquals(expected.getSummary(), result.getSummary());
    Assert.assertEquals(expected.getDescription(), result.getDescription());
    Assert.assertEquals(expected.getLocation(), result.getLocation());
    Assert.assertEquals(expected.getStart(), result.getStart());
    Assert.assertEquals(expected.getEnd(), result.getEnd());
    Assert.assertEquals(expected.getExtendedProperties().get(EventWrapper.CATEGORY), result.getExtendedProperties().get(EventWrapper.CATEGORY));
    Assert.assertEquals(expected.getExtendedProperties().get(EventWrapper.EVENT_CREATOR), result.getExtendedProperties().get(EventWrapper.EVENT_CREATOR));
    Assert.assertEquals(expected.getExtendedProperties().get(EventWrapper.BOOKMARKS), result.getExtendedProperties().get(EventWrapper.BOOKMARKS));
    
  }

  @Test
  public void convertEventWrappertoEntity(){
    Entity entity = mockEventWrapper.toEntity();
    
    Assert.assertEquals((String) entity.getProperty(EventWrapper.SUMMARY), MOCK_SUMMARY);
    Assert.assertEquals((String) entity.getProperty(EventWrapper.DESCRIPTION), MOCK_DESCRIPTION);
    Assert.assertEquals((String) entity.getProperty(EventWrapper.LOCATION), MOCK_LOCATION);
    Assert.assertEquals((Date) entity.getProperty(EventWrapper.START_TIME), MOCK_START);
    Assert.assertEquals((Date) entity.getProperty(EventWrapper.END_TIME), MOCK_END);
    Assert.assertEquals((String) entity.getProperty(EventWrapper.EVENT_CREATOR), MOCK_USER_ID);
    Assert.assertEquals((String) entity.getProperty(EventWrapper.CATEGORY), MOCK_CATEGORY);
    Assert.assertEquals((Long) entity.getProperty(EventWrapper.BOOKMARKS), MOCK_BOOKMARKS);
  }

  @Test
  public void getEventCreatorName(){
      String result = EventWrapper.getEventCreatorName(MOCK_USER_ID);
      Assert.assertEquals(MOCK_NICKNAME, result);
  }

    private static HashMap<String, Integer> getDefaultChallengeStatuses() {
      HashMap<String, Integer> statuses = new HashMap<String, Integer>();
      statuses.put("GARD_0",0);
      statuses.put("RECY_0",0);
      statuses.put("WAST_0",0);
      return statuses;
    }
    
    private static Event getExpectedEvent() {
       DateTime startDateTime = new DateTime(MOCK_START);
       EventDateTime start = new EventDateTime()
        .setDateTime(startDateTime)
        .setTimeZone(UTC_TIMEZONE);
       DateTime endDateTime = new DateTime(MOCK_END);
       EventDateTime end = new EventDateTime()
        .setDateTime(endDateTime)
        .setTimeZone(UTC_TIMEZONE);
       Event expected = new Event()
        .setSummary(MOCK_SUMMARY)
        .setLocation(MOCK_LOCATION)
        .setDescription(MOCK_DESCRIPTION)
        .setStart(start)
        .setEnd(end);

       ExtendedProperties ep = new ExtendedProperties();
       ep.set(EventWrapper.CATEGORY, MOCK_CATEGORY);
       ep.set(EventWrapper.EVENT_CREATOR, MOCK_NICKNAME);
       ep.set(EventWrapper.BOOKMARKS, 0L);
       expected.setExtendedProperties(ep);
       return expected;
    }

  
}