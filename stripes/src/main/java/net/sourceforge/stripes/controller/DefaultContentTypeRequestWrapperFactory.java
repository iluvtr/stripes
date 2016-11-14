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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import net.sourceforge.stripes.config.BootstrapPropertyResolver;
import static net.sourceforge.stripes.config.BootstrapPropertyResolver.PACKAGES;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.controller.json.GsonJsonContentTypeRequestWrapper;
import net.sourceforge.stripes.controller.json.JsonContentTypeRequestWrapper;
import net.sourceforge.stripes.controller.multipart.DefaultMultipartWrapperFactory;
import net.sourceforge.stripes.controller.multipart.MultipartWrapper;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.ReflectUtil;
import net.sourceforge.stripes.util.ResolverUtil;
import net.sourceforge.stripes.util.StringUtil;

public class DefaultContentTypeRequestWrapperFactory implements ContentTypeRequestWrapperFactory {

    public static final String CONTENT_TYPE_REQUEST_WRAPPER_CLASSES = "ContentTypeRequestWrapper.Classes";

    private static final Log log = Log.getInstance(DefaultContentTypeRequestWrapperFactory.class);

    private static final String DEFAULT_CONFIG_WRAPPER_CLASSES = "application/json=" + GsonJsonContentTypeRequestWrapper.class.getName();

    private Map<String, Class<? extends ContentTypeRequestWrapper>> wrapperClasses = new HashMap<String, Class<? extends ContentTypeRequestWrapper>>();

    private Configuration configuration;

    @Override
    public void init(Configuration configuration) throws Exception {
        this.configuration = configuration;

        String configWrapperClasses = configuration.getBootstrapPropertyResolver().getProperty(CONTENT_TYPE_REQUEST_WRAPPER_CLASSES);
        if (configWrapperClasses == null || configWrapperClasses.trim().isEmpty()) {
            configWrapperClasses = DEFAULT_CONFIG_WRAPPER_CLASSES;
        }
        log.debug("Configured content type request wrapper classes " + configWrapperClasses);

        wrapperClasses = initWrapperClasses(configWrapperClasses);
    }

    private Map<String, Class<? extends ContentTypeRequestWrapper>> initWrapperClasses(String configWrapperClasses) {
        Map<String, Class<? extends ContentTypeRequestWrapper>> result = new HashMap<String, Class<? extends ContentTypeRequestWrapper>>();
        for (String configStr : StringUtil.standardSplit(configWrapperClasses)) {
            String[] config = configStr.split("=");
            if (config.length != 2) {
                throw new StripesRuntimeException("Bad mapping for content type request wrapper class: " + configStr + "!");
            }
            String contentType = config[0].trim();
            if (contentType.isEmpty()) {
                throw new StripesRuntimeException("Content type in mapping should not be empty");
            }
            String className = config[1].trim();
            Class<? extends ContentTypeRequestWrapper> clazz = null;
            try {
                clazz = ReflectUtil.findClass(className);
            } catch (ClassNotFoundException e) {
                throw new StripesRuntimeException("Could not find class " + className + " in content type request wrapper classes", e);
            }
            result.put(contentType, clazz);
        }
        return result;
    }

    @Override
    public ContentTypeRequestWrapper wrap(HttpServletRequest request) throws Exception {
        String requestedContentType = request.getContentType().toLowerCase();
        for (Map.Entry<String, Class<? extends ContentTypeRequestWrapper>> entry : wrapperClasses.entrySet()) {
            String contentType = entry.getKey();
            Class<? extends ContentTypeRequestWrapper> clazz = entry.getValue();
            if (requestedContentType.startsWith(contentType)) {
                return configuration.getObjectFactory().newInstance(clazz);
            }
        }
        log.debug("Not found ContentTypeRequestWreapper for this request, asumming normal operation, contentType=" + requestedContentType);
        return null;
    }
    
   

}
