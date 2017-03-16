package com.ivascucristian.alexa.skill.ilab.persistence.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

@DynamoDBDocument
public class Episode {

  private long episodeNumber;
  private String title;
  private String longDescription;
  private String url;
  private long duration;

  @DynamoDBAttribute(attributeName = "title")
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @DynamoDBAttribute(attributeName = "longDesc")
  public String getLongDescription() {
    return longDescription;
  }

  public void setLongDescription(String longDescription) {
    this.longDescription = longDescription;
  }

  @DynamoDBAttribute(attributeName = "url")
  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @DynamoDBAttribute(attributeName = "epNumber")
  public long getEpisodeNumber() {
    return episodeNumber;
  }

  public void setEpisodeNumber(long episodeNumber) {
    this.episodeNumber = episodeNumber;
  }

  @DynamoDBAttribute(attributeName = "duration")
  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }
}
