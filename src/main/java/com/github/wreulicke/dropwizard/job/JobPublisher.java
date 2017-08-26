package com.github.wreulicke.dropwizard.job;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.RandomStringUtils;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JobPublisher {
  private final ExecutorService executorService;

  private final AmazonKinesis kinesis;

  private final String streamName;

  private static final ObjectMapper mapper = new ObjectMapper();

  public CompletableFuture<Void> publish(Job job) {
    return CompletableFuture.runAsync(() -> {
      PutRecordRequest request = new PutRecordRequest();
      request.setStreamName(streamName);
      try {
        request.setData(ByteBuffer.wrap(mapper.writeValueAsString(job)
          .getBytes(StandardCharsets.UTF_8)));
        request.setPartitionKey(RandomStringUtils.randomAlphabetic(10));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      kinesis.putRecord(request);
    }, executorService);

  }

}
