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

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.json.gson.GsonFactory;
import javax.servlet.http.HttpServletRequest;
import com.google.api.client.json.JsonFactory;
import java.util.Collections;

import java.util.Map;
import java.util.HashMap;


public final class MockIdHelper implements IdHelper {

    static final String ID_TOKEN_PARAM = "id_token";
    static final String NAME = "name";
    static final String USER_ID = "userId";

    private static Map<String, String> mockPayload = new HashMap<String, String>() {
        {
            put(USER_ID, "00");
            put(NAME, "Name");
        }
    };


    public static Map<String, String> verifyId(HttpServletRequest request) {
        String idToken = null;
        try {
            idToken = request.getParameter(ID_TOKEN_PARAM);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        if (idToken == null) {
            System.out.println("Invalid ID token.");
            return null;
        }
        return mockPayload;
    }

    public String getUserId(HttpServletRequest request) {
        Map<String, String> payload = verifyId(request);
        if (payload == null) {
            return null;
        }
        return payload.get(USER_ID);
    }

    public String getUserNickname(HttpServletRequest request) {
        Map<String, String> payload = verifyId(request);
        if (payload == null) {
            return null;
        }
        return payload.get(NAME);
    }
}