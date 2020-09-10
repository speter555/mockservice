/*-
 * #%L
 * Mockservice
 * %%
 * Copyright (C) 2020 speter555
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package hu.speter555.mockservice.util;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

/**
 * File cache in Map
 *
 * @author speter555
 */
@ApplicationScoped
public class CacheFileHelper {

    private Map<String, String> cache = new HashMap<>();

    public boolean containsKey(String filePath) {
        return cache.containsKey(filePath);
    }

    public String get(String filePath) {
        return cache.get(filePath);
    }

    public void put(String filePath, String response) {
        cache.put(filePath, response);
    }
}
