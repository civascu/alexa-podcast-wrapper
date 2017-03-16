package com.ivascucristian.alexa;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.util.Base64;
import com.icosillion.podengine.exceptions.DateFormatException;
import com.icosillion.podengine.exceptions.InvalidFeedException;
import com.icosillion.podengine.exceptions.MalformedFeedException;
import com.icosillion.podengine.models.Episode;
import com.icosillion.podengine.models.Podcast;
import com.ivascucristian.alexa.persistence.DynamoDbStore;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FeedUpdater {

  private static final String SUCCESS = "SUCCESS";
  private static final String FAILURE = "FAILURE";


  private String feedUrl;
  private String podcastName;

  private static final Logger log = LoggerFactory.getLogger(FeedUpdater.class);
  private AmazonDynamoDB dynamoDbClient = new AmazonDynamoDBClient();

  public String handleFeedUpdate(Context context) {
    try {
      this.podcastName = System.getenv("PODCAST_NAME");
      this.feedUrl = System.getenv("FEED_URL");
      com.ivascucristian.alexa.model.Podcast podcast = loadPodcastFeed();
      saveorUpdate(podcast);
    } catch (InvalidFeedException | MalformedFeedException | MalformedURLException | DateFormatException e) {
      log.error(e.getMessage());
      return FAILURE;
    }

    return SUCCESS;
  }

  private void saveorUpdate(com.ivascucristian.alexa.model.Podcast podcast) {
    DynamoDbStore dbStore = new DynamoDbStore(dynamoDbClient);
    String id = Base64
        .encodeAsString(podcast.getName().toLowerCase().trim().getBytes(Charset.forName("UTF-8")));
    podcast.setId(id);
    dbStore.mapper.save(podcast,
        new DynamoDBMapperConfig(DynamoDBMapperConfig.SaveBehavior.CLOBBER)); // always overwrite
  }

  private com.ivascucristian.alexa.model.Podcast loadPodcastFeed()
      throws InvalidFeedException, MalformedFeedException, MalformedURLException, DateFormatException {
    URL feed = new URL(feedUrl);
    Podcast remotePodcast = new com.icosillion.podengine.models.Podcast(feed);
    if (remotePodcast.getTitle() == null) {
      throw new IllegalArgumentException("Invalid feed");
    }

    com.ivascucristian.alexa.model.Podcast podcast = new com.ivascucristian.alexa.model.Podcast();
    podcast.setName(podcastName);
    podcast.setDescription(remotePodcast.getDescription());

    podcast.setEpisodeCount(remotePodcast.getEpisodes().size());

    List<Episode> remoteEpisodes = remotePodcast.getEpisodes();
    List<com.ivascucristian.alexa.model.Episode> localEpisodes = new ArrayList<>(
        remotePodcast.getEpisodes().size());

    for (int i = remoteEpisodes.size() - 1; i > -1; i--) {
      Episode feedEpisode = remoteEpisodes.get(i);
      com.ivascucristian.alexa.model.Episode episode = new com.ivascucristian.alexa.model.Episode();
      episode.setEpisodeNumber(remoteEpisodes.size() - i);
      episode.setTitle(feedEpisode.getTitle());
      // some episodes have the number followed by : in the title
      String epNoPrefix = "" + episode.getEpisodeNumber() + ": ";
      if (episode.getTitle().startsWith(epNoPrefix)) {
        episode.setTitle(episode.getTitle().substring(epNoPrefix.length()));
      }
      episode.setLongDescription(feedEpisode.getDescription());
      episode.setUrl(feedEpisode.getEnclosure().getURL().toString()
          .replace("http://", "https://")); // alexa needs https
      episode.setDuration(feedEpisode.getEnclosure().getLength());
      localEpisodes.add(episode);
    }

    podcast.setEpisodes(localEpisodes);
    Date lastPublishedDate = remotePodcast.getLastBuildDate();
    if (lastPublishedDate == null && remotePodcast.getLastBuildDateString() != null) {
      // failed to parse, let's try one more format ...
      try {
        lastPublishedDate = (new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss"))
            .parse(remotePodcast.getLastBuildDateString().trim());
      } catch (ParseException e) {
        log.error("Failed to parse date from podcast last build string:" + remotePodcast
            .getLastBuildDateString());
      }
    }
    podcast.setLastUpdateTime(
        lastPublishedDate != null ? lastPublishedDate.getTime() : System.currentTimeMillis());

    return podcast;
  }

}
