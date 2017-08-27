package com.github.wreulicke.dropwizard.job.configure;

import java.util.concurrent.ExecutorService;

import javax.validation.constraints.NotNull;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.github.wreulicke.dropwizard.job.JobDao;
import com.github.wreulicke.dropwizard.job.JobProcessorFactory;

import io.dropwizard.setup.Environment;
import lombok.Data;

@Data
public class LocalJobConfigurer extends JobConfigurer {
  @NotNull
  private String kinesisEndpoint;

  @NotNull
  private String dynamoEndpoint;

  @Override
  public JobProcessorFactory processorFactory(ExecutorService service) {

    KinesisClientLibConfiguration configuration =
      new KinesisClientLibConfiguration(getApplicationName(), getStreamName(), credentials(), "test-worker")
        .withInitialPositionInStream(InitialPositionInStream.LATEST)
        .withKinesisEndpoint(kinesisEndpoint)
        .withDynamoDBEndpoint(dynamoEndpoint);
    return new JobProcessorFactory(configuration, service);
  }

  @Override
  public AmazonKinesis kinesis() {
    return AmazonKinesisClientBuilder.standard()
      .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("dummy", "dummy")))
      .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(kinesisEndpoint, null))
      .build();
  }

  @Override
  public AWSCredentialsProvider credentials() {
    return new AWSStaticCredentialsProvider(new BasicAWSCredentials("dummy", "dummy"));
  }

  @Override
  public void configure(Environment environment, JobDao dao) {
    System.setProperty("com.amazonaws.sdk.disableCbor", "1");
    super.configure(environment, dao);
  }

}