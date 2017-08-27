package com.github.wreulicke.dropwizard;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.assertj.core.api.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehose;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseClientBuilder;
import com.amazonaws.services.kinesisfirehose.model.CreateDeliveryStreamRequest;
import com.amazonaws.services.kinesisfirehose.model.DeleteDeliveryStreamRequest;
import com.amazonaws.services.kinesisfirehose.model.DescribeDeliveryStreamRequest;
import com.amazonaws.services.kinesisfirehose.model.PutRecordBatchRequest;
import com.amazonaws.services.kinesisfirehose.model.PutRecordRequest;
import com.amazonaws.services.kinesisfirehose.model.Record;
import com.amazonaws.services.kinesisfirehose.model.ResourceNotFoundException;
import com.amazonaws.services.kinesisfirehose.model.S3DestinationConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class FirehoseTest {
  private AmazonKinesisFirehose firehose;

  private AWSCredentialsProvider dummyProvider;

  private AmazonS3 s3;


  @Before
  public void setup() {
    System.setProperty("com.amazonaws.sdk.disableCbor", "1");
    dummyProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials("dummy", "dummy"));;
    firehose = AmazonKinesisFirehoseClientBuilder.standard()
      .withCredentials(dummyProvider)
      .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4573", null))
      .build();;

    s3 = AmazonS3ClientBuilder.standard()
      .withCredentials(dummyProvider)
      .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4572", null))
      .withPathStyleAccessEnabled(true)
      .build();;

    s3.createBucket("test");

    firehose.createDeliveryStream(
      new CreateDeliveryStreamRequest()
        .withDeliveryStreamName("testStream")
        .withS3DestinationConfiguration(
          new S3DestinationConfiguration()
            .withBucketARN("arn:aws:s3:::test")
            .withPrefix("firehose/")
            .withRoleARN("arn:aws:iam::dummy:role/dummy")));

    while (true) {
      try {
        firehose.describeDeliveryStream(new DescribeDeliveryStreamRequest().withDeliveryStreamName("testStream"));
        break;
      } catch (ResourceNotFoundException e) {
      }
    }

  }

  @Test
  public void test() {
    firehose.putRecord(
      new PutRecordRequest()
        .withDeliveryStreamName("testStream")
        .withRecord(
          new Record()
            .withData(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8)))));

    assertThat(s3.listObjects("test")
      .getObjectSummaries()).anySatisfy(summary -> {
        assertThat(summary.getKey()).startsWith("firehose/");
      });
  }

  @Test
  public void test2() {
    List<Record> records = IntStream.range(0, 10)
      .mapToObj(i -> new Record()
        .withData(ByteBuffer.wrap("test".getBytes(StandardCharsets.UTF_8))))
      .collect(Collectors.toList());;
    PutRecordBatchRequest batchRequest = new PutRecordBatchRequest()
      .withDeliveryStreamName("testStream")
      .withRecords(records);

    Integer count = firehose.putRecordBatch(batchRequest)
      .getFailedPutCount();
    assertThat(count).isEqualTo(0);


    assertThat(s3.listObjects("test")
      .getObjectSummaries()).areAtLeast(10, new Condition<>(s -> s.getKey()
        .startsWith("firehose/"), "containes prefix"));

  }


  @After
  public void tearDown() {
    firehose.deleteDeliveryStream(new DeleteDeliveryStreamRequest().withDeliveryStreamName("testStream"));

    s3.listObjects("test")
      .getObjectSummaries()
      .forEach(s -> {
        s3.deleteObject(s.getBucketName(), s.getKey());
      });

    s3.deleteBucket("test");
  }
}
