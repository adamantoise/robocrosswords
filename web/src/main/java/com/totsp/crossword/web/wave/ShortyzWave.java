/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.totsp.crossword.web.wave;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.gadgets.client.DynamicHeightFeature;
import com.google.gwt.gadgets.client.Gadget.ModulePrefs;
import com.google.gwt.gadgets.client.IntrinsicFeature;
import com.google.gwt.gadgets.client.NeedsDynamicHeight;
import com.google.gwt.gadgets.client.NeedsIntrinsics;
import com.google.gwt.gadgets.client.UserPreferences;
import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.web.client.GadgetResponse;

import com.totsp.crossword.web.client.Game;
import com.totsp.crossword.web.client.Game.DisplayChangeListener;
import com.totsp.crossword.web.client.Game.PlayStateListener;
import com.totsp.crossword.web.client.PuzzleCodec;
import com.totsp.gwittir.serial.client.SerializationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.cobogw.gwt.waveapi.gadget.client.Participant;
import org.cobogw.gwt.waveapi.gadget.client.ParticipantUpdateEvent;
import org.cobogw.gwt.waveapi.gadget.client.ParticipantUpdateEventHandler;
import org.cobogw.gwt.waveapi.gadget.client.State;
import org.cobogw.gwt.waveapi.gadget.client.StateUpdateEvent;
import org.cobogw.gwt.waveapi.gadget.client.StateUpdateEventHandler;
import org.cobogw.gwt.waveapi.gadget.client.WaveGadget;


/**
 *
 * @author kebernet
 */
@ModulePrefs(title = "Shortyz", author = "Robert Cooper", author_quote = "If you only have two ducks, they are always in a row.", author_email = "kebernet@gmail.com", width = 850, height = 450, scrolling = true)
public class ShortyzWave extends WaveGadget<UserPreferences>
    implements NeedsIntrinsics, NeedsDynamicHeight {
    private static final String INTIAL_PUZZLE_KEY = "intial";
    private static final String[] COLORS = new String[] { "blue", "green", "gray", "violet", "lime" };
    private static final PuzzleCodec CODEC = GWT.create(PuzzleCodec.class);
    private static final PlayCodec PLAY_CODEC = GWT.create(PlayCodec.class);
    private FlexTable userList = new FlexTable();
    private Set<String> stateKeysSeen = new HashSet<String>();
    private PlayStateListener deltaStateListener = new PlayStateListener(){

        @Override
        public void onLetterPlayed(String responder, int across, int down, char response) {
            Play play = new Play();
            play.setTime(getWave().getTime());
            play.setAcross(across);
            play.setDown(down);
            play.setResponse(response);
            play.setResponder(responder);
            HashMap<String, String> delta = new HashMap<String, String>();
            try {
                delta.put("play-" + play.getTime(), PLAY_CODEC.serialize(play));
                getWave().getState().submitDelta(delta);
            } catch (SerializationException ex) {
                Window.alert("Failed to submit play"+ex.toString());
            }

        }

        @Override
        public void onPuzzleLoaded(Puzzle puz) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    };
    private HandlerRegistration startupStateHanlder;

    public ShortyzWave(){
        super();
    }


    UserPreferences prefs;
    private DynamicHeightFeature height;

    @Override
    public void initializeFeature(IntrinsicFeature feature) {

    }

    public static native void makePostRequest(String url, String postdata,
        RequestCallback callback) /*-{
    var response = function(obj) { 
        @com.totsp.crossword.web.wave.ShortyzWave::onSuccessInternal(Lcom/totsp/crossword/web/client/GadgetResponse;Lcom/google/gwt/http/client/RequestCallback;)(obj, callback);
    };
    var params = {};
    params[$wnd.gadgets.io.RequestParameters.HEADERS] = {
    "Content-Type": "text/x-gwt-rpc"
    };
    params[$wnd.gadgets.io.RequestParameters.METHOD] = $wnd.gadgets.io.MethodType.POST; 
    params[$wnd.gadgets.io.RequestParameters.POST_DATA]= postdata; 
    $wnd.gadgets.io.makeRequest(url, response, params);


    }-*/;

    @Override
    public void initializeFeature(DynamicHeightFeature feature) {
        this.height = feature;
    }

    @Override
    protected void init(UserPreferences preferences) {
        this.prefs = preferences;
        final Game g = Injector.INSTANCE.game();
        g.setDisplayChangeListener(new DisplayChangeListener() {
                @Override
                public void onDisplayChange() {
                    height.adjustHeight();
                }
            });
        this.userList.setWidth("100px");
        g.getDisplay().add(userList);

        
        this.getWave().addParticipantUpdateEventHandler(new ParticipantUpdateEventHandler(){

            @Override
            public void onUpdate(ParticipantUpdateEvent event) {
                Injector.INSTANCE.renderer().setColorMap(provisionColors(event.getParticipants()));
                g.render();
            }

        });

        this.startupStateHanlder = this.getWave().addStateUpdateEventHandler(new StateUpdateEventHandler(){

            @Override
            public void onUpdate(StateUpdateEvent event) {
                final State state = event.getState();
                if(state.get(INTIAL_PUZZLE_KEY) == null){
                    g.loadList();
                    g.setPlayStateListener(new PlayStateListener(){

                        @Override
                        public void onLetterPlayed(String responder, int across, int down, char response) {
                            //
                        }

                        @Override
                        public void onPuzzleLoaded(Puzzle puz) {
                            try {
                                startupStateHanlder.removeHandler();
                                HashMap<String, String> delta = new HashMap<String, String>();
                                delta.put(INTIAL_PUZZLE_KEY, CODEC.serialize(puz));
                                state.submitDelta(delta);
                                g.setPlayStateListener(deltaStateListener);
                            } catch (SerializationException ex) {
                                Window.alert("Critical error "+ex.toString());
                            }

                        }

                    });
                } else {
                    try {
                        startupStateHanlder.removeHandler();
                        g.startPuzzle(0L, CODEC.deserialize(state.get(INTIAL_PUZZLE_KEY)));
                        g.setPlayStateListener(deltaStateListener);
                    } catch (SerializationException ex) {
                        Window.alert("Critical error: "+ex.toString());
                    }
                }

            }

        });
        
        

    }

    private Map<String, String> provisionColors(JsArray<Participant> participants){
        Participant user = this.getWave().getViewer();
        Injector.INSTANCE.game().setResponder(user != null ? user.getId() : null);
        HashMap<String, String> colors = new HashMap<String, String>();
        colors.put( user.getId(), "black" );
        userList.removeAllRows();

        int count = 0;
        int colorIndex = 0;
        Label l = new Label(user.getDisplayName());
        l.getElement().getStyle().setColor("black");
        userList.setWidget(0, 0, l);
        for(int i=0; i < participants.length(); i++){

            Participant p = participants.get(i);
            if(p.getId().equals(user.getId())){
                continue;
            }
            
            colors.put(p.getId(), COLORS[colorIndex]);
            l = new Label(p.getDisplayName());
            l.getElement().getStyle().setColor(COLORS[colorIndex]);
            count++;
            userList.setWidget(count, 0, l);
            colorIndex++;
            if(colorIndex == COLORS.length){
                colorIndex=0;
            }
        }

        return colors;
    }

    static void onSuccessInternal(final GadgetResponse response,
        RequestCallback callback) {
        try {
            callback.onResponseReceived(null, new FakeResponse(response));
        } catch (Exception e) {
            callback.onError(null, e);
        }
    }

    public static class FakeRequest extends Request {
    }

    private static class FakeResponse extends Response {
        private GadgetResponse response;

        FakeResponse(GadgetResponse response) {
            this.response = response;
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

        @Override
        public String getStatusText() {
            return "OK";
        }

        @Override
        public String getText() {
            return response.getText();
        }
    }


}
