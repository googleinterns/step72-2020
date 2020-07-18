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
import com.google.sps.data.SuperfundSite;
import com.google.sps.data.WaterContaminant;
import com.google.sps.data.WaterSystem;
import com.google.sps.servlets.SuperfundServlet;
import com.google.sps.servlets.WaterPollutionServlet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito.*;

public class WaterPollutionTest {

    WaterPollutionServlet servlet;
    
    @Before
    public void setup(){
        servlet = new WaterPollutionServlet();
    }
    
    @Test
    public void hometownTest(){
        ArrayList<WaterSystem> servletSystems = new ArrayList<>();
        // try {
            servletSystems = servlet.retrieveSDWViolations("Acton", "MA");
        // } catch (IOException e){}
        System.out.println(servletSystems);
        WaterSystem acton = new WaterSystem("MA2002000");
        Assert.assertTrue(servletSystems.contains(acton));
    }

}