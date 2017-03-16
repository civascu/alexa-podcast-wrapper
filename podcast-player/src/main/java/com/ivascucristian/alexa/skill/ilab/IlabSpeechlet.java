package com.ivascucristian.alexa.skill.ilab;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Directive;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.interfaces.audioplayer.AudioPlayer;
import com.amazon.speech.speechlet.interfaces.audioplayer.ClearBehavior;
import com.amazon.speech.speechlet.interfaces.audioplayer.PlayBehavior;
import com.amazon.speech.speechlet.interfaces.audioplayer.directive.PlayDirective;
import com.amazon.speech.speechlet.interfaces.audioplayer.request.PlaybackFailedRequest;
import com.amazon.speech.speechlet.interfaces.audioplayer.request.PlaybackFinishedRequest;
import com.amazon.speech.speechlet.interfaces.audioplayer.request.PlaybackNearlyFinishedRequest;
import com.amazon.speech.speechlet.interfaces.audioplayer.request.PlaybackStartedRequest;
import com.amazon.speech.speechlet.interfaces.audioplayer.request.PlaybackStoppedRequest;
import com.amazon.speech.speechlet.interfaces.system.SystemInterface;
import com.ivascucristian.alexa.skill.ilab.persistence.PodcastProgressStore;
import com.ivascucristian.alexa.skill.ilab.persistence.PodcastStore;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IlabSpeechlet implements Speechlet, AudioPlayer {

  private static final Logger log = LoggerFactory.getLogger(IlabSpeechlet.class);
  private PodcastProgressStore persistenceStore;
  private IlabManager ilabManager;
  private com.ivascucristian.alexa.skill.ilab.persistence.model.Podcast podcast;

  public IlabSpeechlet() {
    PodcastStore podcastStore = new PodcastStore();

    // set an env variable on the lambda function
    String podcastName = System.getenv("PODCAST_NAME");
    this.podcast = podcastStore.load(podcastName);
    this.persistenceStore = new PodcastProgressStore(podcast);

    this.ilabManager = new IlabManager(this.podcast, this.persistenceStore);
  }

  // Session management
  @Override
  public void onSessionStarted(SessionStartedRequest sessionStartedRequest, Session session)
      throws SpeechletException {
    // nothing for now
  }

  @Override
  public void onSessionEnded(SessionEndedRequest sessionEndedRequest, Session session)
      throws SpeechletException {
    // nothing for now
  }


  @Override
  public SpeechletResponse onLaunch(LaunchRequest launchRequest, Session session)
      throws SpeechletException {
    return ilabManager.handleSkillLaunch();
  }

  @Override
  public SpeechletResponse onIntent(IntentRequest intentRequest, Session session)
      throws SpeechletException {
    String userId = session.getUser().getUserId();
    Intent intent = intentRequest.getIntent();

    switch (intent.getName()) {
      case Intents.PlayFirstEpisode:
        return ilabManager.handlePlayFirstEpisode(intent);
      case Intents.PlayLatestEpisode:
        return ilabManager.handlePlayLastEpisode(intent);
      case Intents.PlayEpisode:
        return ilabManager.handlePlayExactEpisode(intent);
      case Intents.EpisodeDescription:
        return ilabManager.handleEpisodeDescription(intent);
      case Intents.LastEpisodeDescription:
        return ilabManager.handleLastEpisodeDescription(intent);
      case Intents.DirectionAsc:
        return ilabManager.handleConfigSetDirectionAsc(userId);
      case Intents.DirectionDesc:
        return ilabManager.handleConfigSetDirectionDesc(userId);
      case Intents.AmazonCancel:
        return ilabManager.handleAudioStop();
      case Intents.AmazonStop:
        return ilabManager.handleAudioStop();
      case Intents.AmazonHelp:
        return ilabManager.handleHelpRequest();
      case Intents.AmazonLoopOn:
        return ilabManager.handleLoopToggle(userId, true);
      case Intents.AmazonLoopOff:
        return ilabManager.handleLoopToggle(userId, false);
      case Intents.AmazonPause:
        return ilabManager.handleAudioStop();
      case Intents.AmazonResume:
        return ilabManager.handleAudioResume(userId);
      case Intents.AmazonNext:
        return ilabManager.handleAudioNext(userId, false);
      case Intents.AmazonPrevious:
        return ilabManager.handleAudioPrevious(userId, false);
      case Intents.AmazonShuffleOn:
        return ilabManager.featureNotSupported("shuffle");
      case Intents.AmazonShuffleOff:
        return ilabManager.featureNotSupported("shuffle");
      default:
        return ilabManager.handleSkillLaunch();
    }
  }

  @Override
  public SpeechletResponse onPlaybackFailed(
      SpeechletRequestEnvelope<PlaybackFailedRequest> speechletRequestEnvelope) {
    // log failure
    log.error("Failed to play!!");
    return null;
  }

  @Override
  public SpeechletResponse onPlaybackFinished(
      SpeechletRequestEnvelope<PlaybackFinishedRequest> speechletRequestEnvelope) {
    String userId = speechletRequestEnvelope.getContext()
        .getState(SystemInterface.class, SystemInterface.STATE_TYPE).getUser().getUserId();
    return ilabManager.handleAudioNext(userId, true);
  }

  @Override
  public SpeechletResponse onPlaybackNearlyFinished(
      SpeechletRequestEnvelope<PlaybackNearlyFinishedRequest> speechletRequestEnvelope) {
    String episodeToken = speechletRequestEnvelope.getRequest().getToken();
    long offset = speechletRequestEnvelope.getRequest().getOffsetInMilliseconds();
    String userId = speechletRequestEnvelope.getContext()
        .getState(SystemInterface.class, SystemInterface.STATE_TYPE).getUser().getUserId();
    int episodeNumber = Integer.parseInt(episodeToken.substring(3, episodeToken.length()));
    if (episodeNumber < podcast.getEpisodeCount()) {
      episodeNumber++;
    } else {
      return ilabManager.buildClearQueueResponse(ClearBehavior.CLEAR_ENQUEUED);
    }

    SpeechletResponse enqueueNextEpisodeResponse = new SpeechletResponse();
    PlayDirective nextEpDirective = new PlayDirective();
    nextEpDirective.setPlayBehavior(PlayBehavior.ENQUEUE);
    nextEpDirective
        .setAudioItem(ilabManager.buildAudioItem(podcast.getEpisodes().get(episodeNumber), 0));
    List<Directive> directives = new ArrayList<>();
    directives.add(nextEpDirective);
    enqueueNextEpisodeResponse.setDirectives(directives);
    return enqueueNextEpisodeResponse;
  }

  @Override
  public SpeechletResponse onPlaybackStarted(
      SpeechletRequestEnvelope<PlaybackStartedRequest> speechletRequestEnvelope) {
    String episodeToken = speechletRequestEnvelope.getRequest().getToken();
    long offset = speechletRequestEnvelope.getRequest().getOffsetInMilliseconds();
    String userId = speechletRequestEnvelope.getContext()
        .getState(SystemInterface.class, SystemInterface.STATE_TYPE).getUser().getUserId();
    int episodeNumber = Integer.parseInt(episodeToken.substring(3, episodeToken.length()));
    persistenceStore.storeState(userId, (episodeNumber), offset);
    return ilabManager.buildClearQueueResponse(ClearBehavior.CLEAR_ENQUEUED);
  }

  @Override
  public SpeechletResponse onPlaybackStopped(
      SpeechletRequestEnvelope<PlaybackStoppedRequest> speechletRequestEnvelope) {
    String episodeToken = speechletRequestEnvelope.getRequest().getToken();
    long offset = speechletRequestEnvelope.getRequest().getOffsetInMilliseconds();
    String userId = speechletRequestEnvelope.getContext()
        .getState(SystemInterface.class, SystemInterface.STATE_TYPE).getUser().getUserId();
    int episodeNumber = Integer.parseInt(episodeToken.substring(3, episodeToken.length()));
    persistenceStore.storeState(userId, (episodeNumber), offset);
    return ilabManager.handlePlaybackStopped();
  }


}
