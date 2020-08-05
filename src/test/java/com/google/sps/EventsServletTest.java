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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.sps.servlets.EventsServlet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito.*;

import java.util.Date;
import org.json.JSONObject;
import org.json.JSONArray;

@RunWith(JUnit4.class)
public final class EventsServletTest {

    private EventsServlet servlet;
    private EventWrapper mockEventWrapper;
    private JSONObject mockEventWrapperJson;
    private User mockUser;
    
    private static final String MOCK_DATE = "2020-07-01";
    private static final String MOCK_TIME = "14:00";
    private static final String MOCK_TIMEZONE = "0";
    
    private static final String MOCK_ID_TOKEN = "123";
    private static final String MOCK_SUMMARY = "Test Event";
    private static final String MOCK_DESCRIPTION = "Description";
    private static final String MOCK_LOCATION = "Mountain View, CA";
    private static final String MOCK_CATEGORY = "other";
    private static final String MOCK_USER_ID = "00";
    private static final String MOCK_NICKNAME = "Name";
    private static final Long MOCK_DATETIME_MILLISECONDS = 1593612000000L;
    private static final Long MOCK_BOOKMARKS = 0L;

    
    private static Date MOCK_START;
    private static Date MOCK_END;
    
    private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    private static final String DEFAULT_CHALLENGE_ID = "GARD_0";;
    private static final HashMap<String, Integer> DEFAULT_CHALLENGE_STATUSES = getDefaultChallengeStatuses();

    @Before
    public void setup(){
        helper.setUp();
        servlet = new EventsServlet();
        
        servlet.setIdHelper(new MockIdHelper());
        servlet.setDatastoreService(datastore);
        
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
    
        mockEventWrapperJson = new JSONObject(mockEventWrapper.toJSON());
        
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
    public void timezoneBehindUtc(){
        Date expectedDateTime = new Date(1593640800000L);
        String timezone = "480";
        Date result = servlet.getEventDateTime(MOCK_DATE, MOCK_TIME, timezone);
        Assert.assertEquals(expectedDateTime, result);
    }

    @Test
    public void timezoneEqualsUtc(){
        Date expectedDateTime = new Date(1593612000000L);
        String timezone = "0";
        Date result = servlet.getEventDateTime(MOCK_DATE, MOCK_TIME, timezone);
        Assert.assertEquals(expectedDateTime, result);
    }

    @Test
    public void timezoneAheadOfUtc(){
        Date expectedDateTime = new Date(1593608400000L);
        String timezone = "-60";
        Date result = servlet.getEventDateTime(MOCK_DATE, MOCK_TIME, timezone);
        Assert.assertEquals(expectedDateTime, result);
    }

    @Test
    public void timezoneChangesDate(){
        Date expectedDateTime = new Date(1593651600000L);
        String timezone = "660";
        Date result = servlet.getEventDateTime(MOCK_DATE, MOCK_TIME, timezone);
        Assert.assertEquals(expectedDateTime, result);
    }
    
    @Test
    public void getEvents() {
        datastore.put(mockEventWrapper.toEntity());

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);

        try {
            when(response.getWriter()).thenReturn(pw);
            servlet.doGet(request, response);
            pw.flush();
            System.out.println(mockEventWrapperJson);
            System.out.println(writer.toString().trim());

            JSONArray responseJson = new JSONArray(writer.toString().trim());
            Assert.assertTrue(responseJson.length() == 1);
            JSONObject responseItem = (JSONObject) responseJson.get(0);
            System.out.println(responseItem.getJSONObject("extendedProperties"));
            
            Assert.assertEquals(MOCK_SUMMARY, responseItem.getString(EventWrapper.SUMMARY));
            Assert.assertEquals(MOCK_DESCRIPTION, responseItem.getString(EventWrapper.DESCRIPTION));
            Assert.assertEquals(MOCK_LOCATION, responseItem.getString(EventWrapper.LOCATION));
            Assert.assertTrue(MOCK_DATETIME_MILLISECONDS == responseItem.getJSONObject(EventWrapper.END_TIME).getJSONObject("dateTime").getLong("value"));
            Assert.assertTrue(MOCK_DATETIME_MILLISECONDS == responseItem.getJSONObject(EventWrapper.END_TIME).getJSONObject("dateTime").getLong("value"));
            Assert.assertEquals(MOCK_CATEGORY, responseItem.getJSONObject("extendedProperties").getString(EventWrapper.CATEGORY));
            Assert.assertEquals(MOCK_NICKNAME,responseItem.getJSONObject("extendedProperties").getString(EventWrapper.EVENT_CREATOR));
            Assert.assertTrue(MOCK_BOOKMARKS == responseItem.getJSONObject("extendedProperties").getLong(EventWrapper.BOOKMARKS));
            Assert.assertTrue((Long) responseItem.getJSONObject("extendedProperties").getLong(EventWrapper.EVENT_ID) != null);
                                
        } catch (IOException e) {}
    }
    
    private static HashMap<String, Integer> getDefaultChallengeStatuses() {
      HashMap<String, Integer> statuses = new HashMap<String, Integer>();
      statuses.put("GARD_0",0);
      statuses.put("RECY_0",0);
      statuses.put("WAST_0",0);
      return statuses;
    }

}