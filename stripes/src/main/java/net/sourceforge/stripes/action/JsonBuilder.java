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

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

/**
 * This class converts an object to JSON.
 *
 * @author Néstor Hernández Loli
 */
public interface JsonBuilder {

    JsonBuilder addPropertyExclusion(String... property);

    void setRootObject(Object object);

    void build(Writer writer) throws Exception;

}
