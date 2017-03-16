package com.ivascucristian.alexa.skill.ilab.persistence;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.ivascucristian.alexa.skill.ilab.Constants;
import com.ivascucristian.alexa.skill.ilab.persistence.model.PodcastProgressState;

public class PodcastProgressStore {

  private Table podcastProgressTable;
  private com.ivascucristian.alexa.skill.ilab.persistence.model.Podcast podcast;

  public PodcastProgressStore(
      com.ivascucristian.alexa.skill.ilab.persistence.model.Podcast podcast) {
    AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient();
    DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
    podcastProgressTable = dynamoDB.getTable(Constants.PROGRESS_DYNAMODB_TABLE_NAME);
    this.podcast = podcast;
  }

  public void storeLoop(String userId, boolean isLoopOn) {
    Item item = podcastProgressTable.getItem("userId", userId);
    if (item == null) {
      item = new Item()
          .withPrimaryKey("userId", userId)
          .withBoolean(Constants.ORDER_DYNDB_ATTR, false);
    }
    item.withBoolean(Constants.LOOP_DYNDB_ATTR, isLoopOn);
    podcastProgressTable.putItem(item);
  }

  public void storeOrder(String userId, boolean isAsc) {
    Item item = podcastProgressTable.getItem("userId", userId);
    if (item == null) {
      item = new Item()
          .withPrimaryKey("userId", userId)
          .withBoolean(Constants.LOOP_DYNDB_ATTR, false);
    }
    item.withBoolean(Constants.ORDER_DYNDB_ATTR, isAsc);
    podcastProgressTable.putItem(item);
  }

  public void storeState(String userId, int episodeNumber, long offsetInMillis) {
    // check if we have a new item in there ...

    Item item = podcastProgressTable.getItem("userId", userId);
    if (item == null) {
      item = new Item()
          .withPrimaryKey("userId", userId)
          .withBoolean(Constants.LOOP_DYNDB_ATTR, false)
          .withBoolean(Constants.ORDER_DYNDB_ATTR, true);
    }

    item.withNumber(Constants.EPISODE_DYNDB_ATTR, episodeNumber)
        .withNumber(Constants.OFFSET_DYNDB_ATTR, offsetInMillis);

    podcastProgressTable.putItem(item);
  }

  public PodcastProgressState getState(String userId) {
    Item userProgress = podcastProgressTable.getItem("userId", userId);
    if (userProgress == null) {
      return null;
    }

    PodcastProgressState storedState = new PodcastProgressState();
    storedState.setOffsetInMillis(userProgress.getNumber(Constants.OFFSET_DYNDB_ATTR).longValue());

    int episodeNumber = userProgress.getNumber(Constants.EPISODE_DYNDB_ATTR).intValue();
    storedState.setEpisode(podcast.getEpisodes().get(episodeNumber - 1));
    storedState.setUserId(userId);
    if (userProgress.isPresent(Constants.LOOP_DYNDB_ATTR)) {
      storedState.setLoop(userProgress.getBoolean(Constants.LOOP_DYNDB_ATTR));
    }
    if (userProgress.isPresent(Constants.ORDER_DYNDB_ATTR)) {
      storedState.setAscendingOrder(userProgress.getBoolean(Constants.ORDER_DYNDB_ATTR));
    }

    return storedState;
  }
}
