package com.ivascucristian.alexa.skill.ilab;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

public class IlabSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {

    private static final Set<String> supportedApplicationIds = new HashSet<>();

    static {
        supportedApplicationIds.add("amzn1.ask.skill.your-skill-id-here");
    }

    public IlabSpeechletRequestStreamHandler() {
        super(new IlabSpeechlet(), supportedApplicationIds);
    }
}
