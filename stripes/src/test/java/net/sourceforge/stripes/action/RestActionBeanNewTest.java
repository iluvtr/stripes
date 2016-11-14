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

import com.google.gson.Gson;
import java.net.HttpURLConnection;
import java.util.Map;
import net.sourceforge.stripes.FilterEnabledTestBase;
import net.sourceforge.stripes.controller.ContentTypeRequestWrapper;
import net.sourceforge.stripes.controller.DefaultContentTypeRequestWrapperFactory;
import net.sourceforge.stripes.controller.json.GsonJsonContentTypeRequestWrapper;
import net.sourceforge.stripes.controller.json.JsonContentTypeRequestWrapper;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.util.Log;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Néstor Hernández Loli
 */
public class RestActionBeanNewTest extends FilterEnabledTestBase {
    private static final Log LOG = Log.getInstance(RestActionBeanNewTest.class);

    @Test(groups = "fast")
    public void testHttpAnnotationsWorksOnActionBeans() throws Exception{
        MockRoundtrip trip = new MockRoundtrip(getMockServletContext(), PersonActionBean.class);
        trip.execute();
        
        Person person = new Person();
        person.setAge(10);
        person.setName("Blaba");
        Gson gson = new Gson();
        
        trip.getResponse().setHeader("content-type", "application/json");
        trip.getRequest().setMethod("GET");
        trip.getRequest().setRequestBody(gson.toJson(person));
        
        trip.execute();
        
        Assert.assertEquals(trip.getResponse().getStatus(), HttpURLConnection.HTTP_OK);
        
//        Assert.assertEquals(trip.getResponse().getOutputString(), "Hello world!");
        
        
        logTripResponse(trip);
//         
    }
    
    private void logTripResponse(MockRoundtrip trip) {
        
        LOG.debug("TRIP RESPONSE: [Status=" + trip.getResponse().getStatus()
                + "] [Message=" + trip.getResponse().getOutputString() + "] [Error Message="
                + trip.getResponse().getErrorMessage() + "]");
    }
    
    @RestActionBean
    @UrlBinding("/person")
    public static class PersonActionBean implements  ActionBean{
        private ActionBeanContext context;
        private Person person;

        @POST
        public Resolution post() {
            return null;
        } 
        
        @GET
        public Resolution get() {
            return new StreamingResolution("text/plain", "Hello world!");
        } 
        
        public Person getPerson() { return person; } 
        public void setPerson(Person person) { this.person = person; } 

        public ActionBeanContext getContext() {return context; } 
        public void setContext(ActionBeanContext context) {this.context = context;}
    }
    
    public static class Person {
        private String name;
        private int age;

        public String getName() { return name; } 
        public void setName(String name) { this.name = name; }

        public int getAge() { return age; } 
        public void setAge(int age) {    this.age = age; }
         
    }    
    
    
}
