/*
 * Copyright 2016 Stripes Framework.
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
package net.sourceforge.stripes.controller.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import net.sourceforge.stripes.controller.ContentTypeRequestWrapper;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;

/**
 *
 * @author Néstor Hernández Loli
 */
public class GsonJsonContentTypeRequestWrapper implements ContentTypeRequestWrapper {

    private static final Log log = Log.getInstance(GsonJsonContentTypeRequestWrapper.class);

    private Map< String, Set<String>> parameters = new HashMap< String, Set<String>>();

    @Override
    public void build(HttpServletRequest request) throws IOException {
        log.debug("build() called.");
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(request.getReader());
        if (jsonElement.isJsonArray()) {
            throw new StripesRuntimeException("The JSON requests bodies must start with an object brace and not an array.");
        }
        processElement(jsonElement, null);
    }

    private void processElement(JsonElement jsonElement, String parent) {
        log.debug("Processing node (", jsonElement, ")");

        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); ++i) {
                String currentPath = parent + "[" + i + "]";
                JsonElement childElement = jsonArray.get(i);
                processElement(childElement, currentPath);
            }

        } else if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String childFieldName = entry.getKey();
                JsonElement childElement = entry.getValue();
                String currentPath = (parent != null ? parent + "." + childFieldName : childFieldName);
                processElement(childElement, currentPath);
            }

        } else {
            String name = parent;
            Set<String> parameterValues = parameters.get(name);
            if (parameterValues == null) {
                parameterValues = new HashSet<String>();
            }
            String text = jsonElement.getAsString();
            parameterValues.add(text);

            log.debug("Adding parameter (name=", name, ",value=", text, ")");

            parameters.put(name, parameterValues);
        }
    }

    @Override
    public Enumeration<String> getParameterNames() {
        log.debug("Returning parameter names to a caller.");
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        Set<String> values = parameters.get(name);
        String[] returnValues = null;
        if (values != null) {
            returnValues = values.toArray(new String[values.size()]);
        }
        log.debug("Returning parameter value (", returnValues, ") for name (", name, ") to a caller.");
        return returnValues;
    }

}
