package com.google.sps;

import static org.mockito.Mockito.*;

import org.junit.*;
import com.google.api.client.http.*;
import com.google.api.client.testing.http.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.sps.data.SuperfundSite;
import com.google.sps.servlets.SuperfundServlet;

import java.util.ArrayList;
import java.io.*;
import java.net.*;

@RunWith(JUnit4.class)
public final class SuperfundTest {

    private SuperfundServlet servlet;

    private static final String EPA_API_LINK = "https://enviro.epa.gov/enviro/efservice/SEMS_ACTIVE_SITES/";
    private static final String EPA_ZIP_FORMAT = "SITE_ZIP_CODE/";
    private static final String EPA_STATE_FORMAT = "SITE_STATE/";
    private static final String CSV_FORMAT = "/Excel/";
    private static final String AREA_PARAMETER = "area";
    private static final String ZIP = "zip";
    private static final String STATE = "state";

    private static final String ZIP_PARAMETER = "zip_code";


    @Before
    public void setup(){
        servlet = new SuperfundServlet();
    }

    @Test
    public void hometownTest(){
        // HttpServletRequest request = mock(HttpServletRequest.class);       
        // HttpServletResponse response = mock(HttpServletResponse.class);
        // when(request.getParameter(AREA_PARAMETER)).thenReturn(ZIP);
        // when(request.getParameter(ZIP_PARAMETER)).thenReturn("01720");

        ArrayList<SuperfundSite> actonSites = new ArrayList<>();
        actonSites.add(new SuperfundSite("W.R. GRACE & CO., INC. (ACTON PLANT)", 0, "MA", "ACTON", "MIDDLESEX", "Currently on the Final NPL", 42.45055, -71.427781));
        actonSites.add(new SuperfundSite("W R GRACE DARAMIC PLANT", 0, "MA", "ACTON", "MIDDLESEX", "Not on the NPL", 0, 0));
        actonSites.add(new SuperfundSite("AGWAY/KRESS PROPERTY", 0, "MA", "ACTON", "MIDDLESEX", "Not on the NPL", 0, 0));

        ArrayList<SuperfundSite> servletSites = servlet.retrieveSuperfundData("01720", EPA_ZIP_FORMAT);
        System.out.println(servletSites);
        Assert.assertArrayEquals(actonSites.toArray(), servletSites.toArray());
    }

    @Test
    public void massachusettsNumSitesTest(){
        ArrayList<SuperfundSite> servletSites = servlet.retrieveSuperfundData("MA", EPA_STATE_FORMAT);
        Assert.assertEquals(servletSites.size(), 386);
    }


}