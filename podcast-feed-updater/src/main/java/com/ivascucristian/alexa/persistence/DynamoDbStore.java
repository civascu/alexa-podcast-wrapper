package com.ivascucristian.alexa.persistence;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.ConversionSchemas;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;

public class DynamoDbStore {

  public DynamoDBMapper mapper;
  protected DynamoDBMapperConfig mapperConfig;
  protected AmazonDynamoDB dynamoDB;

  public DynamoDbStore(AmazonDynamoDB dynamoDbClient) {
    this.dynamoDB = dynamoDbClient;
    this.mapper = new DynamoDBMapper(dynamoDB, new DynamoDBMapperConfig(ConversionSchemas.V2));
    this.mapperConfig = new DynamoDBMapperConfig(
        DynamoDBMapperConfig.SaveBehavior.CLOBBER,
        DynamoDBMapperConfig.ConsistentReads.CONSISTENT,
        DynamoDBMapperConfig.TableNameOverride.withTableNamePrefix("newt")
    );
  }
}

