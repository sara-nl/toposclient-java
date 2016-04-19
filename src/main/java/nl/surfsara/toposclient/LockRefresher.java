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

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jeroen Schot
 */
class LockRefresher extends TimerTask {

    private int lockTime;
    private Map<URI, URI> locks;
    private CloseableHttpClient httpClient;

    public LockRefresher(int lockTime) {
        this.lockTime = lockTime;
        this.locks = new ConcurrentHashMap<>();
        this.httpClient = HttpClients.createDefault();
    }

    public void addLock(Token token) {
        locks.put(token.getId(), token.getLock());
    }

    public void removeLock(Token token) {
        locks.remove(token.getId());
    }

    public void refreshLock(URI lockUrl) throws Exception {
        URI uri = new URIBuilder(lockUrl)
                .setParameter("timeout", String.valueOf(lockTime))
                .build();
        System.out.println(uri);
        HttpGet request = new HttpGet(uri);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            EntityUtils.consume(response.getEntity());
        }
    }

    @Override
    public void run() {
        for (URI lock : locks.values()) {
            try {
                refreshLock(lock);
            } catch (Exception e) {
                System.err.println("Exception while refreshing lock");
            }
        }
    }
}
