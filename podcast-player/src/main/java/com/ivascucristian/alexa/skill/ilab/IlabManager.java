package com.ivascucristian.alexa.skill.ilab;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Directive;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.interfaces.audioplayer.AudioItem;
import com.amazon.speech.speechlet.interfaces.audioplayer.ClearBehavior;
import com.amazon.speech.speechlet.interfaces.audioplayer.PlayBehavior;
import com.amazon.speech.speechlet.interfaces.audioplayer.Stream;
import com.amazon.speech.speechlet.interfaces.audioplayer.directive.ClearQueueDirective;
import com.amazon.speech.speechlet.interfaces.audioplayer.directive.PlayDirective;
import com.amazon.speech.speechlet.interfaces.audioplayer.directive.StopDirective;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.ivascucristian.alexa.skill.ilab.persistence.PodcastProgressStore;
import com.ivascucristian.alexa.skill.ilab.persistence.model.Episode;
import com.ivascucristian.alexa.skill.ilab.persistence.model.PodcastProgressState;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class IlabManager {

  private PodcastProgressStore persistenceStore;
  private com.ivascucristian.alexa.skill.ilab.persistence.model.Podcast podcast;

  public IlabManager(com.ivascucristian.alexa.skill.ilab.persistence.model.Podcast podcast,
      PodcastProgressStore persistenceStore) {
    this.persistenceStore = persistenceStore;
    this.podcast = podcast;
  }


  public SpeechletResponse handleSkillLaunch() {
    SsmlOutputSpeech plainTextOutputSpeech = new SsmlOutputSpeech();
    plainTextOutputSpeech
        .setSsml("<speak>Welcome to the " + podcast.getName() + " podcast unofficial skill. " +
            "You can ask to play the first, last  <break time=\"1ms\"/>or a specific episode number, between 1 and "
            + podcast.getEpisodeCount()
            + " <break time=\"1ms\"/> or 'resume', to pick up where you left off. <break time=\"1ms\"/> What episode would you like to listen to?</speak>");
    SsmlOutputSpeech welcomeRepromptOS = new SsmlOutputSpeech();
    welcomeRepromptOS.setSsml(
        "<speak>You can ask to play the first, last <break time=\"1ms\"/> or a specific episode number, between 1 and "
            + podcast.getEpisodeCount()
            + " <break time=\"1ms\"/>. What episode would you like to listen to?</speak>");
    Reprompt welcomeReprompt = new Reprompt();
    welcomeReprompt.setOutputSpeech(welcomeRepromptOS);
    return SpeechletResponse.newAskResponse(plainTextOutputSpeech, welcomeReprompt);
  }

  public SpeechletResponse handlePlaybackStopped() {
    return buildClearQueueResponse(ClearBehavior.CLEAR_ALL);
  }

  public SpeechletResponse buildClearQueueResponse(ClearBehavior clearBehavior) {
    ClearQueueDirective cqd = new ClearQueueDirective();
    cqd.setClearBehavior(clearBehavior);

    List<Directive> directives = new ArrayList<>();
    directives.add(cqd);
    SpeechletResponse sr = new SpeechletResponse();
    sr.setDirectives(directives);
    return sr;
  }

  public SpeechletResponse handleAudioStop() {
    StopDirective stopDirective = new StopDirective();
    SpeechletResponse speechletResponse = new SpeechletResponse();
    List<Directive> directiveList = new ArrayList<>();
    directiveList.add(stopDirective);
    speechletResponse.setDirectives(directiveList);
    return speechletResponse;
  }

  /* Handlers for Intents */
  public SpeechletResponse handlePlayFirstEpisode(Intent intent) {
    return buildPlayEpisodeResponse(podcast.getEpisodes().get(0));
  }

  public SpeechletResponse handlePlayLastEpisode(Intent intent) {
    return buildPlayEpisodeResponse(podcast.getEpisodes().get(podcast.getEpisodes().size() - 1));
  }

  public SpeechletResponse handlePlayExactEpisode(Intent intent) {
    // let's figure out what episode was requested
    Object episodeFromIntent = extractEpisodeNumberFromIntent(intent);
    if (episodeFromIntent instanceof SpeechletResponse) {
      return (SpeechletResponse) episodeFromIntent;
    }

    int episodeNumber = (int) episodeFromIntent;

    //episodeNumber is populated now
    return buildPlayEpisodeResponse(podcast.getEpisodes().get(episodeNumber - 1));
  }

  public SpeechletResponse handleLastEpisodeDescription(Intent intent) {
    Episode episode = podcast.getEpisodes().get(podcast.getEpisodes().size() - 1);
    return buildTellResponseWithCard(episode.getTitle(), buildEpisodeCard(episode));
  }

  public SpeechletResponse handleEpisodeDescription(Intent intent) {

    Object episodeFromIntent = extractEpisodeNumberFromIntent(intent);
    if (episodeFromIntent instanceof SpeechletResponse) {
      return (SpeechletResponse) episodeFromIntent;
    }

    int episodeNumber = (int) episodeFromIntent;

    Episode episode = podcast.getEpisodes().get(episodeNumber - 1);
    return buildTellResponseWithCard(episode.getTitle(), buildEpisodeCard(episode));
  }

  /* Helper methods */
  private SpeechletResponse buildAskResponseWithText(String text, String repromptText) {
    PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
    outputSpeech.setText(text);

    Reprompt reprompt = new Reprompt();
    if (StringUtils.isBlank(repromptText)) {
      reprompt.setOutputSpeech(outputSpeech);
    } else {
      PlainTextOutputSpeech repromptOutputSpeech = new PlainTextOutputSpeech();
      repromptOutputSpeech.setText(repromptText);
      reprompt.setOutputSpeech(repromptOutputSpeech);
    }

    return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
  }

  private SpeechletResponse buildTellResponseWithCard(String text, SimpleCard card) {
    PlainTextOutputSpeech episodeDescription = new PlainTextOutputSpeech();
    episodeDescription.setText(text);
    if (card == null) {
      return SpeechletResponse.newTellResponse(episodeDescription);
    }
    return SpeechletResponse.newTellResponse(episodeDescription, card);
  }

  private SpeechletResponse buildPlayEpisodeResponse(Episode episode) {
    return buildPlayEpisodeResponse(episode, 0, false);
  }

  private SpeechletResponse buildPlayEpisodeResponse(Episode episode, boolean autoAdvance) {
    return buildPlayEpisodeResponse(episode, 0, autoAdvance);
  }

  private SpeechletResponse buildPlayEpisodeResponse(Episode episode, long offsetInMillis,
      boolean autoAdvance) {
    // build the audio directive
    PlayDirective playEpisodeDirective = new PlayDirective();
    playEpisodeDirective.setPlayBehavior(PlayBehavior.REPLACE_ALL);
    playEpisodeDirective.setAudioItem(buildAudioItem(episode, offsetInMillis));
    String episodeIntroText = "Playing ";
    if (episode.getEpisodeNumber() == 1) {
      episodeIntroText += "the first episode";
    } else if (episode.getEpisodeNumber() == podcast.getEpisodeCount()) {
      episodeIntroText += "the latest episode";
    } else {
      episodeIntroText += "episode " + episode.getEpisodeNumber();
    }
    episodeIntroText += ".";
    // add a short intro blurb
    PlainTextOutputSpeech introBlurbSpeech = new PlainTextOutputSpeech();
    introBlurbSpeech.setText(episodeIntroText);

    // define the card to add to the app

    SimpleCard episodeCard = buildEpisodeCard(episode);

    List<Directive> directives = new ArrayList<Directive>();
    directives.add(playEpisodeDirective);

    SpeechletResponse response = new SpeechletResponse();
    response.setDirectives(directives);
    response.setShouldEndSession(true);
    if (!autoAdvance) {
      response.setOutputSpeech(introBlurbSpeech);
      response.setCard(episodeCard);
    }

    return response;
  }

  public AudioItem buildAudioItem(Episode episodeToPlay, long offset) {
    AudioItem audioItem = new AudioItem();
    Stream audioStream = new Stream();
    audioStream.setUrl(episodeToPlay.getUrl());
    audioStream.setToken("EP_" + episodeToPlay.getEpisodeNumber());
    audioStream.setOffsetInMilliseconds(offset);

    audioItem.setStream(audioStream);
    return audioItem;
  }

  private SimpleCard buildEpisodeCard(Episode episode) {
    SimpleCard episodeCard = new SimpleCard();
    episodeCard.setTitle(episode.getTitle());
    episodeCard.setContent(episode.getLongDescription() + " More on: http://investlikeaboss.com");
    return episodeCard;
  }

  private Object extractEpisodeNumberFromIntent(Intent intent) {
    if (!intent.getSlots().containsKey(Constants.EP_NUMBER_SLOT_NAME)
        || intent.getSlot(Constants.EP_NUMBER_SLOT_NAME) == null || StringUtils
        .isBlank(intent.getSlot(Constants.EP_NUMBER_SLOT_NAME).getValue())) {
      // ask the user for clarification
      return buildAskResponseWithText("I didn't get that. What episode would you like?", null);
    }

    int episodeNumber;
    String episodeNumberAsText = intent.getSlot(Constants.EP_NUMBER_SLOT_NAME).getValue();
    try {
      episodeNumber = Integer.parseInt(episodeNumberAsText);
    } catch (NumberFormatException nfe) {
      // did not get an episode number; ask again
      return buildAskResponseWithText("I didn't get that. What episode would you like?", null);
    }

    if (episodeNumber < 1 || episodeNumber > podcast.getEpisodeCount()) {
      // number out of range, ask again
      return buildAskResponseWithText("I only have episodes 1 through " + podcast.getEpisodeCount()
              + " . Which one would you like to hear?",
          "What episode do you want to listen to?");
    }

    return episodeNumber;
  }

  public SpeechletResponse handleConfigSetDirectionAsc(String userId) {
    persistenceStore.storeOrder(userId, true);
    return buildTellResponseWithCard("Got it.", null);
  }

  public SpeechletResponse handleConfigSetDirectionDesc(String userId) {
    persistenceStore.storeOrder(userId, false);
    return buildTellResponseWithCard("Got it.", null);

  }

  public SpeechletResponse handleHelpRequest() {
    SimpleCard helpCard = new SimpleCard();
    helpCard.setTitle("Invest Like a Boss Podcast Help");
    helpCard.setContent(
        "Unofficial skill to listen to all episodes of the investlikeaboss.com podcast. Here are some things you can say: play the last episode,"
            +
            "play the first episode," +
            "play episode 5," +
            "what's episode 10 about?," +
            "set newest first," +
            "set oldest first," +
            "loop on / off. You can also say, stop, if you're done. We'll save your progress and you can resume at any time.");
    String helpText = "Here are some things you can say: 'play the first episode', 'play the last episode', 'play episode 20', what's episode 10 about. You can also say, stop, if you're done. What would you like to do?";
    return buildAskResponseWithText(helpText, helpText);
  }

  public SpeechletResponse featureNotSupported(String feature) {
    return buildTellResponseWithCard(feature + " is not supported.", null);
  }

  public SpeechletResponse handleLoopToggle(String userId, boolean isLoopOn) {
    persistenceStore.storeLoop(userId, isLoopOn);
    return buildTellResponseWithCard("Ok.", null);
  }

  public SpeechletResponse handleAudioResume(String userId) {
    PodcastProgressState currentState = retrieveLastPlayedEpisode(userId);
    return buildPlayEpisodeResponse(currentState.getEpisode(), currentState.getOffsetInMillis(),
        false);
  }

  public SpeechletResponse handleAudioNext(String userId, boolean isAutoAdvance) {
    PodcastProgressState currentState = retrieveLastPlayedEpisode(userId);
    if (currentState == null || currentState.getEpisode() == null) {
      if (isAutoAdvance) {
        return null;
      }
      return buildAskResponseWithText(
          "There's no episode playing right now. What episode would you like to hear?",
          "What episode would you like to hear?");
    }

    if (!currentState.isAscendingOrder()) {
      return handleAudioPrevious(userId, isAutoAdvance);
    }

    int currentEpisode = (int) currentState.getEpisode().getEpisodeNumber();

    int nextEpisode = currentEpisode + 1;
    //check if we should be looping
    if (currentState.isLoop() && currentEpisode >= podcast.getEpisodes().size()) {
      nextEpisode = 1;
    } else if (!currentState.isLoop() && currentEpisode >= podcast.getEpisodes().size()) {
      if (isAutoAdvance) {
        return null;
      }
      return buildTellResponseWithCard("There are no more episodes.", null);
    }

    return buildPlayEpisodeResponse(podcast.getEpisodes().get(nextEpisode - 1), isAutoAdvance);
  }

  public SpeechletResponse handleAudioPrevious(String userId, boolean isAutoAdvance) {
    PodcastProgressState currentState = retrieveLastPlayedEpisode(userId);

    if (currentState == null || currentState.getEpisode() == null) {
      if (isAutoAdvance) {
        return null;
      }
      return buildAskResponseWithText(
          "There's no episode playing right now. What episode would you like to hear?",
          "What episode would you like to hear?");
    }

    if (currentState.isAscendingOrder()) {
      return handleAudioNext(userId, false);
    }

    int currentEpisode = (int) currentState.getEpisode().getEpisodeNumber();
    int previousEpisode = currentEpisode - 1;

    if (currentState.isLoop() && currentEpisode <= 1) {
      previousEpisode = (int) podcast.getEpisodeCount();
    } else if (!currentState.isLoop() && currentEpisode <= 1) {
      if (isAutoAdvance) {
        return null;
      }
      return buildTellResponseWithCard("There are no more episodes.", null);
    }

    return buildPlayEpisodeResponse(podcast.getEpisodes().get(previousEpisode - 1), isAutoAdvance);
  }

  private PodcastProgressState retrieveLastPlayedEpisode(String userId) {
    PodcastProgressState podcastState = persistenceStore.getState(userId);
    if (podcastState == null) {
      podcastState = new PodcastProgressState();
      podcastState.setUserId(userId);
      podcastState.setOffsetInMillis(0);
      podcastState.setEpisode(podcast.getEpisodes().get(podcast.getEpisodes().size() - 1));
    }
    return podcastState;
  }

  public SpeechletResponse handleUnknownIntent(Intent intent) {
    return null;
  }
}
