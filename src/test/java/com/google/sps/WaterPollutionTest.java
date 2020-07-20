package com.google.sps;

import java.util.ArrayList;

import com.google.sps.data.WaterSystem;
import com.google.sps.servlets.WaterPollutionServlet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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