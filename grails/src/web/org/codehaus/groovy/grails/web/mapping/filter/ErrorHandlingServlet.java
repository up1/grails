/* Copyright 2004-2005 Graeme Rocher
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
package org.codehaus.groovy.grails.web.mapping.filter;

import org.codehaus.groovy.grails.web.mapping.UrlMappingInfo;
import org.codehaus.groovy.grails.web.mapping.UrlMappingsHolder;
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes;
import org.codehaus.groovy.grails.web.servlet.GrailsDispatcherServlet;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequestFilter;
import org.codehaus.groovy.grails.web.util.WebUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * A servlet for handling errors
 *
 * @author mike
 * @since 1.0-RC1
 */
public class ErrorHandlingServlet extends GrailsDispatcherServlet {
    private static final String TEXT_HTML = "text/html";

    protected void doDispatch(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        int statusCode;

        if (request.getAttribute("javax.servlet.error.status_code") != null) {
            statusCode = Integer.parseInt(request.getAttribute("javax.servlet.error.status_code").toString());


        }
        else {
            statusCode = 500;
        }

        final UrlMappingsHolder urlMappingsHolder = lookupUrlMappings();
        final UrlMappingInfo urlMappingInfo = urlMappingsHolder.matchStatusCode(statusCode);

        if (urlMappingInfo != null) {

            GrailsWebRequestFilter filter = new GrailsWebRequestFilter();
            filter.setServletContext(getServletContext());
            filter.doFilter(request, response, new FilterChain() {
                public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
                    WebUtils.forwardRequestForUrlMappingInfo(request, response, urlMappingInfo);
                }
            });
        }
        else {
            renderDefaultResponse(response, statusCode);
        }
    }


    private void renderDefaultResponse(HttpServletResponse response, int statusCode) throws IOException {
        if (statusCode == 404) {
            renderDefaultResponse(response, statusCode, "Not Found", "Page not found.");
        }
        else {
            renderDefaultResponse(response, statusCode, "Internal Error", "Internal server error.");
        }
    }

    private void renderDefaultResponse(HttpServletResponse response, int statusCode, String title, String text) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType(TEXT_HTML);

        Writer writer = response.getWriter();


        writer.write("<HTML>\n<HEAD>\n<TITLE>Error " + statusCode + " - " + title);
        writer.write("</TITLE>\n<BODY>\n<H2>Error " + statusCode + " - " + title + ".</H2>\n");
        writer.write(text + "<BR/>");

        for (int i = 0; i < 20; i++)
            writer.write("\n<!-- Padding for IE                  -->");

        writer.write("\n</BODY>\n</HTML>\n");
        writer.flush();
    }

    private UrlMappingsHolder lookupUrlMappings() {
         WebApplicationContext wac =
                 WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());

         return (UrlMappingsHolder)wac.getBean(UrlMappingsHolder.BEAN_ID);
     }

}