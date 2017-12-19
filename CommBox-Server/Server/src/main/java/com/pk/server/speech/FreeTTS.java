package com.pk.server.speech;

//import com.sun.speech.freetts.Voice;
//import com.sun.speech.freetts.VoiceManager;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class FreeTTS {

    private static final String VOICENAME_kevin = "kevin";
    private String text; // string to speech

    public FreeTTS(String text) {
        this.text = text;
    }

    public void speak() {
        Voice voice;
        VoiceManager voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice(VOICENAME_kevin);
        voice.allocate();
        voice.speak(text);
    }
}
