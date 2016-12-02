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
package net.sourceforge.stripes.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.json.JacksonJsonContentTypeRequestWrapper;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.ReflectUtil;
import net.sourceforge.stripes.util.StringUtil;

public class DefaultContentTypeRequestWrapperFactory implements ContentTypeRequestWrapperFactory {

    public static final String CONTENT_TYPE_REQUEST_WRAPPER_CONFIGS = "ContentTypeRequestWrapper.Configs";

    private static final Log log = Log.getInstance(DefaultContentTypeRequestWrapperFactory.class);

    private static final Class<? extends ContentTypeRequestWrapper> DEFAULT_JSON_CONTENT_TYPE_REQUEST_WRAPPER = JacksonJsonContentTypeRequestWrapper.class;
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String DEFAULT_CONFIGS = CONTENT_TYPE_JSON + "=" + DEFAULT_JSON_CONTENT_TYPE_REQUEST_WRAPPER.getName();

    private Map<String, Class<? extends ContentTypeRequestWrapper>> ctrClasses;

    private Configuration configuration;

    @Override
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;
        String configs = configuration.getBootstrapPropertyResolver().getProperty(CONTENT_TYPE_REQUEST_WRAPPER_CONFIGS);
        if (configs == null || configs.trim().isEmpty()) {
            configs = DEFAULT_CONFIGS;
        }
        log.debug("Current configuration provided=" + configs);
        ctrClasses = initClasses(configs);

        fillDefaultsIfNotProvided();

        log.debug("Configured content type request wrapper classes " + ctrClasses);
    }

    private void fillDefaultsIfNotProvided() {
        if (!ctrClasses.containsKey(CONTENT_TYPE_JSON)) {
            ctrClasses.put(CONTENT_TYPE_JSON, DEFAULT_JSON_CONTENT_TYPE_REQUEST_WRAPPER);
        }
    }

    private Map<String, Class<? extends ContentTypeRequestWrapper>> initClasses(String configs) {
        Map<String, Class<? extends ContentTypeRequestWrapper>> result = new HashMap<String, Class<? extends ContentTypeRequestWrapper>>();
        for (String configStr : StringUtil.standardSplit(configs)) {
            String[] config = configStr.split("=");
            if (config.length != 2) {
                throw new StripesRuntimeException("Invalid configuration. "
                        + "Each configuration should have two parts separated by '=', eg. application/json=net.sft.MyContentType. "
                        + "Actual configuration=" + configStr);
            }
            String contentType = config[0].trim();
            if (contentType.isEmpty()) {
                throw new StripesRuntimeException("Content type in configuration should not be empty. "
                        + "Actual configuration=" + configStr);
            }
            String className = config[1].trim();
            Class<? extends ContentTypeRequestWrapper> clazz = null;
            try {
                clazz = ReflectUtil.findClass(className);
            } catch (ClassNotFoundException e) {
                throw new StripesRuntimeException("Could not find class " + className + " for content type " + contentType, e);
            }
            result.put(contentType, clazz);
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public ContentTypeRequestWrapper wrap(HttpServletRequest request) throws Exception {
        String requestedContentType = request.getContentType().toLowerCase();
        for (Map.Entry<String, Class<? extends ContentTypeRequestWrapper>> entry : ctrClasses.entrySet()) {
            String contentType = entry.getKey();
            Class<? extends ContentTypeRequestWrapper> clazz = entry.getValue();
            if (requestedContentType.startsWith(contentType)) {
                return configuration.getObjectFactory().newInstance(clazz);
            }
        }
        log.debug("Not ContentTypeRequestWrapper found for this request, asumming normal operation, contentType=" + requestedContentType);
        return null;
    }

}
