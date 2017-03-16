package com.ivascucristian.alexa.skill.ilab.persistence;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.ConversionSchemas;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.ConsistentReads;
import com.amazonaws.util.Base64;
import com.ivascucristian.alexa.skill.ilab.persistence.model.Podcast;
import java.nio.charset.Charset;

public class PodcastStore {

  private DynamoDBMapper mapper;

  public PodcastStore() {
    this.mapper = new DynamoDBMapper(new AmazonDynamoDBClient(),
        new DynamoDBMapperConfig(ConversionSchemas.V2));
  }


  public Podcast load(String podcastName) {
    String podcastId = Base64.encodeAsString(podcastName.toLowerCase().trim().getBytes(
        Charset.forName("UTF-8")));
    return mapper
        .load(Podcast.class, podcastId, new DynamoDBMapperConfig(ConsistentReads.CONSISTENT));
  }
}
