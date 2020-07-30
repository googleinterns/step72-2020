package com.google.sps;

import java.io.IOException;
import java.util.ArrayList;

import javax.jdo.annotations.Order;

import com.google.sps.data.WaterSystem;
import com.google.sps.servlets.WaterPollutionServlet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WaterPollutionTest {

    WaterPollutionServlet servlet;
    ArrayList<WaterSystem> servletSystems;
    
    @Before
    public void setup(){
        servlet = new WaterPollutionServlet();
    }
    
    @Test
    public void hometownTest(){
        servletSystems = new ArrayList<>();
        try {
            servletSystems = servlet.retrieveSDWViolations("Acton", "MA");
        } catch (IOException e){}
        System.out.println(servletSystems);
        WaterSystem acton = new WaterSystem("MA2002000");
        Assert.assertTrue(servletSystems.contains(acton));
    }

    @Test
    public void contaminantsTest(){
        WaterSystem planetGymnastics = new WaterSystem("MA2002002");
        planetGymnastics.addViolations();
        Assert.assertEquals(planetGymnastics.getContaminants().size(), 23);
    }

}