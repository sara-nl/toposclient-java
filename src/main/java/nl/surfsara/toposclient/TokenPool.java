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
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Timer;
import java.util.UUID;

/**
 * @author Jeroen Schot
 */
public class TokenPool implements Iterable<Token> {

    private static String DEFAULT_SERVER_URL = "http://topos.grid.sara.nl/4.1";
    private static int DEFAULT_LOCK_TIME = 300;
    private String poolName;
    private String serverUrl;
    private CloseableHttpClient httpClient;
    private int lockTime;
    private LockRefresher lockRefresher;
    private Timer timer;

    public TokenPool() {
        this(UUID.randomUUID().toString());
    }

    public TokenPool(String poolName) {
        this(poolName, DEFAULT_SERVER_URL);
    }

    public TokenPool(String poolName, String serverUrl) {
        this(poolName, serverUrl, DEFAULT_LOCK_TIME);
    }

    public TokenPool(String poolName, String serverUrl, int lockTime) {
        this.poolName = poolName;
        this.serverUrl = serverUrl;
        this.lockTime = lockTime;
        this.httpClient = HttpClients.createDefault();
        initTokenPool();
    }

    public String getPoolName() {
        return poolName;
    }

    private void initTokenPool() {
        if (lockTime > 0) {
            lockRefresher = new LockRefresher(lockTime);
            timer = new Timer();
            long interval = 1000 * Math.max(lockTime / 2 - 10, 10);
            timer.schedule(lockRefresher, 0, interval);
        }
    }

    public void close() {
        timer.cancel();
    }

    public Token nextToken() {
        URI uri;
        try {
            uri = new URIBuilder(serverUrl + "/pools/" + poolName + "/nextToken")
                    .setParameter("timeout", String.valueOf(lockTime))
                    .build();
        } catch (URISyntaxException e) {
            return null;
        }
        HttpContext context = new BasicHttpContext();
        HttpGet request = new HttpGet(uri);
        try (CloseableHttpResponse response = httpClient.execute(request, context)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Token token = TokenUtils.fromHttpResponse(response, context);
                if (lockRefresher != null) {
                    lockRefresher.addLock(token);
                }
                return token;
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    public Token uploadToken(Token token) {
        String url = serverUrl + "/pools/" + poolName + "/nextToken";
        HttpPut request = new HttpPut(url);
        HttpEntity entity = new StringEntity(token.getContents(), "UTF-8");
        request.setEntity(entity);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            URI idUrl = new URI(response.getFirstHeader("Location").getValue());
            EntityUtils.consume(response.getEntity());
            token.setId(idUrl);
        } catch (Exception e) {
            return token;
        }
        return token;
    }

    public void deleteToken(Token token) {
        HttpDelete request = new HttpDelete(token.getId());
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            EntityUtils.consume(response.getEntity());
        } catch (IOException e) {
            System.err.println("Exception while deleting token");
        }
    }

    public void unlockToken(Token token) {
        if (lockRefresher != null) {
            lockRefresher.removeLock(token);
        }
        token.setLock(null);
        HttpDelete request = new HttpDelete(token.getLock());
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            EntityUtils.consume(response.getEntity());
        } catch (IOException e) {
            System.err.println("Exception while unlocking token");
        }
    }

    public Iterator<Token> iterator() {
        return new TokenIterator(this);
    }

}