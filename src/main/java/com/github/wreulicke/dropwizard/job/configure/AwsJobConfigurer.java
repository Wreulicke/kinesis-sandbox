package com.github.wreulicke.dropwizard.job.configure;

import java.util.concurrent.ExecutorService;

import javax.validation.constraints.NotNull;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.github.wreulicke.dropwizard.job.JobProcessorFactory;

import lombok.Value;

@Value
public class AwsJobConfigurer extends JobConfigurer {
  @NotNull
  private final String region;


  @Override
  public AWSCredentialsProvider credentials() {
    return DefaultAWSCredentialsProviderChain.getInstance();
  }

  @Override
  public JobProcessorFactory processorFactory(ExecutorService service) {
    KinesisClientLibConfiguration configuration =
      new KinesisClientLibConfiguration(getApplicationName(), getStreamName(), credentials(), "test-worker")
        .withInitialPositionInStream(InitialPositionInStream.LATEST)
        .withRegionName(region);

    return new JobProcessorFactory(configuration, service);
  }

  @Override
  public AmazonKinesis kinesis() {
    return AmazonKinesisClientBuilder.standard()
      .withRegion(region)
      .build();
  }

}