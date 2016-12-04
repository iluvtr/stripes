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

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 *
 * @author Rick Grashel
 */
public class JacksonJsonBuilder extends ObjectOutputBuilder<JacksonJsonBuilder> implements JsonBuilder {

    private Object rootObject;

    public JacksonJsonBuilder() {
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
    public void build(Writer writer) throws IOException {
        if (rootObject == null) {
            throw new IllegalStateException("You must call the method setRootObject before!");
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.addMixInAnnotations(Object.class, DynamicPropertyFilterMixin.class);
        FilterProvider filterProvider = new SimpleFilterProvider()
                .addFilter("dynamicPropertyFilter",
                        SimpleBeanPropertyFilter.serializeAllExcept(getExcludedProperties()));

        mapper.writer(filterProvider).writeValue(writer, getRootObject());
    }

    /**
     * This is an empty class which is used to do dynamic exclusion of property
     * names from serialization.
     */
    @JsonFilter("dynamicPropertyFilter")
    class DynamicPropertyFilterMixin {
    }

}
