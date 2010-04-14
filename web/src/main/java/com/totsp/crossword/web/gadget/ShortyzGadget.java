/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.gadget;

import com.google.gwt.gadgets.client.DynamicHeightFeature;
import com.google.gwt.gadgets.client.Gadget;
import com.google.gwt.gadgets.client.Gadget.ModulePrefs;
import com.google.gwt.gadgets.client.IntrinsicFeature;
import com.google.gwt.gadgets.client.NeedsDynamicHeight;
import com.google.gwt.gadgets.client.NeedsIntrinsics;
import com.google.gwt.gadgets.client.UserPreferences;
import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.totsp.crossword.web.client.BasicEntryPoint;
import com.totsp.crossword.web.client.BasicEntryPoint.DisplayChangeListener;
import com.totsp.crossword.web.client.PuzzleServiceProxy;
import com.totsp.crossword.web.client.PuzzleServiceProxy.CallStrategy;

/**
 *
 * @author kebernet
 */
@ModulePrefs(

            title = "Shortyz",
            author = "Robert Cooper",
            author_quote = "If you only have two ducks, they are always in a row.",
            author_email = "kebernet@gmail.com",
            width = 750,
            height = 300,
            scrolling = true

)
public class ShortyzGadget extends Gadget<UserPreferences> implements NeedsIntrinsics, NeedsDynamicHeight{

    UserPreferences prefs;
    @Override
    protected void init(UserPreferences preferences) {
        this.prefs = preferences;
        BasicEntryPoint entry = new BasicEntryPoint();
        BasicEntryPoint.PROXY = new PuzzleServiceProxy(BasicEntryPoint.SERVICE, new CallStrategy(){

            @Override
            public Request makeRequest(RequestBuilder builder) {
                 makePostRequest(builder.getUrl(), builder.getRequestData(), builder.getCallback());
                 return new FakeRequest();
            }

        });
        BasicEntryPoint.displayChangeListener = new DisplayChangeListener(){

            @Override
            public void onDisplayChange() {
                height.adjustHeight();
            }

        };
        entry.onModuleLoad();
    }

    @Override
    public void initializeFeature(IntrinsicFeature feature) {
        
    }

    private native void makePostRequest(String url, String postdata, RequestCallback callback) /*-{
            function response(obj) { //(4)
                  @com.totsp.crossword.web.gadget.ShortyzGadget::onSuccessInternal(Lcom/totsp/crossword/web/gadget/GadgetResponse;Lcom/google/gwt/http/client/RequestCallback;)(obj, callback);
            };
            var params = {};
            params[$wnd.gadgets.io.RequestParameters.HEADERS] = {
                  "Content-Type": "text/x-gwt-rpc"
            }
            params[$wnd.gadgets.io.RequestParameters.METHOD] = $wnd.gadgets.io.MethodType.POST; //(1)
            params[$wnd.gadgets.io.RequestParameters.POST_DATA]= postdata; //(2)
            $wnd.gadgets.io.makeRequest(url, response, params); //(3)

            
      }-*/;

    static void onSuccessInternal(final GadgetResponse response, RequestCallback callback) {
            try {
                  callback.onResponseReceived(null, new FakeResponse(response));
            } catch (Exception e) {
                  callback.onError(null, e);
            }
      }

    
    private DynamicHeightFeature height;
    @Override
    public void initializeFeature(DynamicHeightFeature feature) {
        this.height = feature;
    }

    private static class FakeResponse extends Response {
        private GadgetResponse response;

        FakeResponse(GadgetResponse response){
            this.response = response;
        }

        @Override
            public String getText() {
                  return response.getText();
            }

            @Override
            public String getStatusText() {
                  return "OK";
            }

        @Override
        public String getHeader(String header) {
            return null;
        }

        @Override
        public Header[] getHeaders() {
            return new Header[0];
        }

        @Override
        public String getHeadersAsString() {
           return null;
        }

        @Override
        public int getStatusCode() {
            return 200;
        }
    }


    public static class FakeRequest extends Request {

    }

}
