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

import java.net.URI;

/**
 * @author Jeroen Schot
 */
public class Token {
    private String contents;
    private URI id;
    private URI lock;

    public Token(String contents) {
        this(contents, null);
    }

    public Token(String contents, URI id) {
        this(contents, id, null);
    }

    public Token(String contents, URI id, URI lock)
    {
        this.contents = contents;
        this.id = id;
        this.lock = lock;
    }

    protected void setId(URI id) {
        this.id = id;
    }

    protected void setLock(URI lock) {
        this.lock = lock;
    }

    public URI getId() {
        return id;
    }

    public String getContents() {
        return contents;
    }

    public URI getLock() {
        return lock;
    }

}
