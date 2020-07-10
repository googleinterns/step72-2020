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

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.sps.servlets.EventsServlet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito.*;

import java.util.Date;

@RunWith(JUnit4.class)
public final class EventsTest {

    private EventsServlet servlet;
    private String date;
    private String time;
    private String timezone;

    @Before
    public void setup(){
        servlet = new EventsServlet();
        
        date = "2020-07-01";
        time = "14:00";

    }

    @Test
    public void timezoneBehindUtc(){
        Date expectedDateTime = new Date(1593640800000L);
        timezone = "480";
        Date result = servlet.getEventDateTime(date, time, timezone);
        Assert.assertEquals(expectedDateTime, result);
    }

    @Test
    public void timezoneEqualsUtc(){
        Date expectedDateTime = new Date(1593612000000L);
        timezone = "0";
        Date result = servlet.getEventDateTime(date, time, timezone);
        Assert.assertEquals(expectedDateTime, result);
    }

    @Test
    public void timezoneAheadOfUtc(){
        Date expectedDateTime = new Date(1593608400000L);
        timezone = "-60";
        Date result = servlet.getEventDateTime(date, time, timezone);
        Assert.assertEquals(expectedDateTime, result);
    }

    @Test
    public void timezoneChangesDate(){
        Date expectedDateTime = new Date(1593651600000L);
        timezone = "660";
        Date result = servlet.getEventDateTime(date, time, timezone);
        Assert.assertEquals(expectedDateTime, result);
    }

}