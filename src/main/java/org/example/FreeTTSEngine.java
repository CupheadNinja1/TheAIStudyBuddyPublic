package org.example;


import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class FreeTTSEngine implements Buddy_TTS{
    public Voice voice;


    public FreeTTSEngine() {
        System.setProperty("freetts.voices",
                "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        VoiceManager vm = VoiceManager.getInstance();
        voice = vm.getVoice("kevin16");
        if (voice != null) {
            voice.allocate();
        } else {
            throw new IllegalStateException("Voice not found!");
        }
    }


    @Override
    public void speak(String text) {
        if (voice != null) {
            voice.speak(text);
        }
    }
}

