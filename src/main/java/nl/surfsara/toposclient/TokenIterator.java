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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Jeroen Schot
 */
public class TokenIterator implements Iterator<Token> {

    private TokenPool pool;
    private Token currentToken;
    private Token nextToken;

    public TokenIterator(TokenPool pool) {
        this.pool = pool;
    }

    @Override
    public boolean hasNext() {
        if (nextToken == null) {
            nextToken = pool.nextToken();
        }
        return nextToken != null;
    }

    @Override
    public Token next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        currentToken = nextToken;
        return currentToken;
    }

    @Override
    public void remove() {
        pool.deleteToken(currentToken);
    }
}