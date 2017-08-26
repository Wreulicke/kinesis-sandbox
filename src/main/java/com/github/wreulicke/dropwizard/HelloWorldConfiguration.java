package com.github.wreulicke.dropwizard;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.wreulicke.dropwizard.job.JobDao;
import com.github.wreulicke.dropwizard.job.JobProcessorFactory;
import com.github.wreulicke.dropwizard.job.JobPublisher;
import com.github.wreulicke.dropwizard.job.JobResource;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.lifecycle.setup.ExecutorServiceBuilder;
import io.dropwizard.setup.Environment;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class HelloWorldConfiguration extends Configuration {

  @Valid
  @NotNull
  @JsonProperty("database")
  private DataSourceFactory dataSourceFactory = new DataSourceFactory();


  @Valid
  @NotNull
  @JsonProperty("job")
  private JobConfigurer jobConfigurer;

  @Data
  public static class JobConfigurer {
    private String kinesisEndpoint;
    private String dynamoEndpoint;

    @NotNull
    private String streamName;

    @NotNull
    private String applicationName;

    private String region;

    @Min(1)
    private int shardSize;

    @Min(1)
    private int jobPoolSize;

    public JobResource build(JobDao dao, ExecutorService service) {
      return new JobResource(dao, buildJobPublisher(service));
    }

    public JobPublisher buildJobPublisher(ExecutorService service) {
      return new JobPublisher(service, buildKinesis(), streamName);
    }

    public AmazonKinesis buildKinesis() {
      if (kinesisEndpoint != null && dynamoEndpoint != null) {
        return AmazonKinesisClientBuilder.standard()
          .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("dummy", "dummy")))
          .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(kinesisEndpoint, null))
          .build();
      }
      else if (region != null) {
        return AmazonKinesisClientBuilder.standard()
          .withRegion(region)
          .build();
      }
      throw new RuntimeException("local setting or cloud setting is required.");

    }

    public AWSCredentialsProvider buildCredentialProvider() {
      if (kinesisEndpoint != null && dynamoEndpoint != null) {
        return new AWSStaticCredentialsProvider(new BasicAWSCredentials("dummy", "dummy"));
      }
      else {
        return DefaultAWSCredentialsProviderChain.getInstance();
      }
    }

    public JobProcessorFactory buildJobProcessorFactory(ExecutorService service) {
      KinesisClientLibConfiguration configuration =
        new KinesisClientLibConfiguration(applicationName, streamName, buildCredentialProvider(), "test-worker")
          .withInitialPositionInStream(InitialPositionInStream.LATEST);

      if (kinesisEndpoint != null && dynamoEndpoint != null) {
        configuration = configuration.withKinesisEndpoint(kinesisEndpoint)
          .withDynamoDBEndpoint(dynamoEndpoint);
      }
      else if (region != null) {
        configuration = configuration.withRegionName(region);
      }

      return new JobProcessorFactory(configuration, service);
    }

    public void configure(Environment environment, JobDao dao) {
      if (kinesisEndpoint != null && dynamoEndpoint != null)
        System.setProperty("com.amazonaws.sdk.disableCbor", "1");
      ExecutorServiceBuilder publisherService = environment.lifecycle()
        .executorService("job-publisher-", new ThreadFactoryBuilder().setNameFormat("job-publisher-")
          .setDaemon(true)
          .build())
        .minThreads(jobPoolSize)
        .maxThreads(jobPoolSize);

      environment.jersey()
        .register(build(dao, publisherService.build()));

      ThreadFactory factory = new ThreadFactoryBuilder().setDaemon(true)
        .setNameFormat("job-processor-")
        .build();
      ExecutorService service = Executors.newFixedThreadPool(shardSize, factory);

      environment.lifecycle()
        .manage(buildJobProcessorFactory(service));

    }
  }
}
