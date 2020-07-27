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


public final class GoogleIdHelper implements IdHelper {
    
    // OAuth credentials client ID, used to verify user ID token
    private static final String CLIENT_ID = "605480199600-e4uo1livbvl58cup3qtd1miqas7vspcu.apps.googleusercontent.com";

    static final String ID_TOKEN_PARAM = "id_token";
    static final String NAME = "name";

    static final HttpTransport HTTP_TRANSPORT = new UrlFetchTransport();
    static final JsonFactory JSON_FACTORY = new GsonFactory();
    

    static final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(HTTP_TRANSPORT, JSON_FACTORY)
        .setAudience(Collections.singletonList(CLIENT_ID))
        .build();

    public static Payload verifyId(HttpServletRequest request) {
        GoogleIdToken idToken = null;
        try {
            idToken = verifier.verify(request.getParameter(ID_TOKEN_PARAM));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        if (idToken == null) {
            System.out.println("Invalid ID token.");
            return null;
        }

        Payload payload = idToken.getPayload();
        return payload;
    }

    public String getUserId(HttpServletRequest request) {
        Payload payload = verifyId(request);
        if (payload == null) {
            return null;
        }
        return payload.getSubject();
    }

    public String getUserNickname(HttpServletRequest request) {
        Payload payload = verifyId(request);
        if (payload == null) {
            return null;
        }
        return (String) payload.get(NAME);
    }
}