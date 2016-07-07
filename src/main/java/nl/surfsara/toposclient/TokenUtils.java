/*
 * Copyright 2016 SURFsara B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.surfsara.toposclient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.net.URI;

/**
 * @author Jeroen Schot
 */
public class TokenUtils {

    public static Token fromHttpResponse(HttpResponse response, HttpContext context) {
        try {
            URI idUrl;
            RedirectLocations locations = (RedirectLocations) context.getAttribute(DefaultRedirectStrategy.REDIRECT_LOCATIONS);
            if (locations != null) {
                idUrl = locations.getAll().get(locations.getAll().size() - 1);
            } else {
                return null;
            }
            URI lockUrl = new URI(response.getFirstHeader("X-Topos-LockURL").getValue());
            HttpEntity e = response.getEntity();
            String content = EntityUtils.toString(e, "UTF-8");
            EntityUtils.consume(e);
            return new Token(content, idUrl, lockUrl);
        } catch (Exception e) {
            return null;
        }
    }
}
