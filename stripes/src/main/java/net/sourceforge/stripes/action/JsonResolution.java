/*
 * Copyright 2014 Rick Grashel.
 *
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
 */
package net.sourceforge.stripes.action;

import net.sourceforge.stripes.controller.AsyncResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sourceforge.stripes.controller.ContentTypeRequestWrapperFactory;
import net.sourceforge.stripes.controller.StripesFilter;

/**
 * This resolution is intended to be used with Stripes REST action beans. This
 * type of resolution will take a Java object and serialize it to JSON
 * automatically.
 */
public class JsonResolution extends HttpResolution {

    private Object objectToSerialize;
    private String[] excludedProperties;

    /**
     * This constructor should be used if the caller wants to return an object
     * and have it automatically serialized into JSON.
     *
     * @param objectToSerialize - Object to serialize into JSON
     * @param excludedProperties - Properties to exclude from marshaling
     */
    public JsonResolution(Object objectToSerialize, String... excludedProperties) {
        super(HttpServletResponse.SC_OK);
        this.objectToSerialize = objectToSerialize;
        this.excludedProperties = excludedProperties;
    }

    /**
     * Converts the object passed in to JSON and streams it back to the client.
     *
     * @throws java.lang.Exception
     */
    public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        super.execute(request, response);

        response.setContentType("application/json");
        JsonBuilderFactory jsonBuilderFactory = StripesFilter.getConfiguration().getJsonBuilderFactory();

        JsonBuilder jsonBuilder = jsonBuilderFactory.create();
        jsonBuilder.setRootObject(objectToSerialize);
        jsonBuilder.addPropertyExclusion(excludedProperties);
        jsonBuilder.build(response.getWriter());

        response.flushBuffer();
        AsyncResponse asyncResponse = AsyncResponse.get(request);
        if (asyncResponse != null) {
            // async started, complete
            asyncResponse.complete();
        }
    }

}
