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
import com.google.sps.servlets.SuperfundServlet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public final class SuperfundTest {

    private SuperfundServlet servlet;

    private ArrayList<SuperfundSite> actonSites;

    private static final String AREA_PARAMETER = "area";
    private static final String ZIP = "zip";
    private static final String STATE = "state";

    private static final String ZIP_PARAMETER = "zip_code";
    
    @Before
    public void setup(){
        servlet = new SuperfundServlet();
        
        actonSites = new ArrayList<>();
        actonSites.add(new SuperfundSite("W.R. GRACE & CO., INC. (ACTON PLANT)", SuperfundServlet.DEFAULT_SCORE, "MA", "ACTON", "MIDDLESEX", "Currently on the Final NPL", 42.45055, -71.427781));
        actonSites.add(new SuperfundSite("W R GRACE DARAMIC PLANT", SuperfundServlet.DEFAULT_SCORE, "MA", "ACTON", "MIDDLESEX", "Not on the NPL", 0, 0));
        actonSites.add(new SuperfundSite("AGWAY/KRESS PROPERTY", SuperfundServlet.DEFAULT_SCORE, "MA", "ACTON", "MIDDLESEX", "Not on the NPL", 0, 0));

    }

    @Test
    public void superfundInvalid(){
        Assert.assertFalse(actonSites.get(2).isValidSite());
    }

    @Test
    public void superfundValid(){
        Assert.assertTrue(actonSites.get(0).isValidSite());
    }

    @Test
    public void hometownTest(){
        ArrayList<SuperfundSite> actonSites = new ArrayList<>();
        actonSites.add(new SuperfundSite("W.R. GRACE & CO., INC. (ACTON PLANT)", 0, "MA", "ACTON", "MIDDLESEX", "Currently on the Final NPL", 42.45055, -71.427781));
        actonSites.add(new SuperfundSite("W R GRACE DARAMIC PLANT", 0, "MA", "ACTON", "MIDDLESEX", "Not on the NPL", 0, 0));
        actonSites.add(new SuperfundSite("AGWAY/KRESS PROPERTY", 0, "MA", "ACTON", "MIDDLESEX", "Not on the NPL", 0, 0));
        
        ArrayList<SuperfundSite> servletSites = new ArrayList<>();
        try {
            URL url = new URL(SuperfundServlet.EPA_API_LINK + SuperfundServlet.EPA_ZIP_FORMAT + "01720" + SuperfundServlet.CSV_FORMAT);
            servletSites = servlet.parseSuperfundsFromURL(url);
        } catch (IOException e){}
        System.out.println(servletSites);
        Assert.assertArrayEquals(actonSites.toArray(), servletSites.toArray());
    }

    @Test
    public void massachusettsNumSitesTest(){
        ArrayList<SuperfundSite> servletSites = servlet.retrieveSuperfundData("MA", SuperfundServlet.EPA_STATE_FORMAT);
        Assert.assertEquals(servletSites.size(), 40);
    }

    @Test
    public void invalidZip(){
        ArrayList<SuperfundSite> servletSites = servlet.retrieveSuperfundData("666666", SuperfundServlet.EPA_ZIP_FORMAT);
        Assert.assertEquals(servletSites.size(), 0);
    }

    @Test
    public void cleanHometownData(){
        SuperfundSite[] realSites = {actonSites.get(0)};
        SuperfundSite[] otherSites = {actonSites.get(1), actonSites.get(2)};
        ArrayList<SuperfundSite> testData = new ArrayList<>(actonSites);
        ArrayList<SuperfundSite> otherArrayList = servlet.cleanSuperfundData(testData);
        
        Assert.assertArrayEquals(testData.toArray(), realSites);
        Assert.assertArrayEquals(otherArrayList.toArray(), otherSites);
    }

    @Test
    public void hometownServletTest(){
        HttpServletRequest request = mock(HttpServletRequest.class);       
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getParameter(AREA_PARAMETER)).thenReturn(ZIP);
        when(request.getParameter(ZIP_PARAMETER)).thenReturn("01720");
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);

        try {
            when(response.getWriter()).thenReturn(pw);
            servlet.doGet(request, response);
            pw.flush();
        
            ArrayList<SuperfundSite> cleanedList = new ArrayList<>();
            cleanedList.add(actonSites.get(0));
            
            Assert.assertEquals(new Gson().toJson(cleanedList), writer.toString());
        } catch (IOException e){}
    }
}