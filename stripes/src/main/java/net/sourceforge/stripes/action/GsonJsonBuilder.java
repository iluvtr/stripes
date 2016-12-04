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
package net.sourceforge.stripes.action;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Writer;
import java.util.Set;

/**
 *
 * @author Néstor Hernández Loli
 */
public class GsonJsonBuilder extends ObjectOutputBuilder<GsonJsonBuilder> implements JsonBuilder {

    private Object rootObject;

    public GsonJsonBuilder() {
        super(null);
    }

    @Override
    public void setRootObject(Object object) {
        this.rootObject = object;
    }

    @Override
    public Object getRootObject() {
        return rootObject;
    }

    @Override
    public void build(Writer writer) throws Exception {
        if (rootObject == null) {
            throw new IllegalStateException("You must call the method setRootObject before!");
        }
        Gson gson = new GsonBuilder()
                .addSerializationExclusionStrategy(new ExclusionStrategyImpl())
                .create();
        
        gson.toJson(rootObject, writer);
    }

    private class ExclusionStrategyImpl implements ExclusionStrategy {

        @Override
        public boolean shouldSkipField(FieldAttributes arg0) {
            String name = arg0.getName();
            Set<String> excludedProperties = getExcludedProperties();
            return excludedProperties.contains(name);
        }

        @Override
        public boolean shouldSkipClass(Class<?> arg0) {
            return false;
        }
    }

}
