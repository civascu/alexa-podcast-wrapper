package com.ivascucristian.alexa.skill.ilab.persistence.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import java.util.List;

@DynamoDBTable(tableName = "ilabPodcastEpisodes")
public class Podcast {

  private String id; //primary key
  private String name; // podcast name
  private long episodeCount;
  private long lastUpdateTime;
  private List<Episode> episodes;
  private String description;

  @DynamoDBHashKey(attributeName = "id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @DynamoDBAttribute(attributeName = "name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @DynamoDBAttribute(attributeName = "totalEps")
  public long getEpisodeCount() {
    return episodeCount;
  }

  public void setEpisodeCount(long episodeCount) {
    this.episodeCount = episodeCount;
  }

  @DynamoDBAttribute(attributeName = "lastUpdateTime")
  public long getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(long lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  @DynamoDBAttribute(attributeName = "episodes")
  public List<Episode> getEpisodes() {
    return episodes;
  }

  public void setEpisodes(List<Episode> episodes) {
    this.episodes = episodes;
  }

  @DynamoDBAttribute(attributeName = "description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}

