package com.google.sps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.sps.data.MockIdHelper;
import com.google.sps.data.EventWrapper;

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

@RunWith(JUnit4.class)
public final class EventsServletTest {

    private EventsServlet servlet;
    private EventWrapper mockEventWrapper;
    
    private static final String MOCK_DATE = "2020-07-01";
    private static final String MOCK_TIME = "14:00";
    private static final String MOCK_TIMEZONE = "0";
    
    private static final String MOCK_ID_TOKEN = "123";
    private static final String MOCK_SUMMARY = "Test Event";
    private static final String MOCK_DESCRIPTION = "Description";
    private static final String MOCK_LOCATION = "Mountain View, CA";
    private static final String MOCK_CATEGORY = "other";
    private static final String MOCK_USER_ID = "00";
    
    private static Date MOCK_START;
    private static Date MOCK_END;
    
    private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

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
        System.out.println("event: " + mockEventWrapper.toJSON());
    }

}