/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.server;

import com.google.code.facebookapi.FacebookXmlRestClient;
import com.google.code.facebookapi.IFacebookRestClient;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.w3c.dom.Document;

/**
 *
 * @author kebernet
 */
public class FacebookSessionFilter implements Filter {
    public static final String FACEBOOK_CLIENT = "FACEBOOK.CLIENT";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession();
        System.out.println("fb_sig_in_iframe "+req.getParameter("fb_sig_in_iframe"));
        System.out.println("fb_sig_user "+req.getParameter("fb_sig_user"));
        if("1".equals(req.getParameter("fb_sig_in_iframe")) && req.getParameter("fb_sig_user") != null ){

            session.setAttribute("user.id", "urn:facebook:"+req.getParameter("fb_sig_user"));
            System.out.println("Facebook user:"+session.getAttribute("user.id"));
//            IFacebookRestClient<Document> userClient = (IFacebookRestClient<Document>) session.getAttribute(FACEBOOK_CLIENT);
//            if(userClient == null && req.getParameter("fb_sig_ss") != null) {
//                userClient = new FacebookXmlRestClient(req.getParameter("fb_sig_api_key"), req.getParameter("fb_sig_ss"));
//                request.setAttribute( FACEBOOK_CLIENT, userClient);
//            }

            
        }

        chain.doFilter(request, response);

    }

    @Override
    public void destroy() {

    }

}
