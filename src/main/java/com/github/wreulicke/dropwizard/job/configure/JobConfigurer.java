package com.github.wreulicke.dropwizard.job.configure;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.github.wreulicke.dropwizard.job.JobDao;
import com.github.wreulicke.dropwizard.job.JobProcessorFactory;
import com.github.wreulicke.dropwizard.job.JobPublisher;
import com.github.wreulicke.dropwizard.job.JobResource;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.dropwizard.lifecycle.ExecutorServiceManager;
import io.dropwizard.lifecycle.setup.ExecutorServiceBuilder;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import lombok.Data;

@JsonTypeInfo(include = As.PROPERTY, use = Id.NAME, property = "type")
@JsonSubTypes({
  @Type(name = "local", value = LocalJobConfigurer.class),
  @Type(name = "aws", value = AwsJobConfigurer.class)
})
@Data
public abstract class JobConfigurer {
  @NotNull
  private String streamName;

  @NotNull
  private String applicationName;

  @Min(1)
  private int shardSize;

  @Min(1)
  private int jobPoolSize;

  public void configure(Environment environment, JobDao dao) {
    ExecutorServiceBuilder publisherService = environment.lifecycle()
      .executorService("job-publisher-%d", new ThreadFactoryBuilder()
        .setDaemon(true)
        .build())
      .minThreads(jobPoolSize)
      .maxThreads(jobPoolSize);

    environment.jersey()
      .register(resource(dao, publisher(streamName, publisherService.build())));

    ThreadFactory factory = new ThreadFactoryBuilder().setDaemon(true)
      .setNameFormat("job-processor-%d")
      .build();
    ExecutorService service = Executors.newFixedThreadPool(shardSize, factory);

    environment.lifecycle()
      .manage(new ExecutorServiceManager(service, Duration.minutes(2), "job-processor"));
    environment.lifecycle()
      .manage(processorFactory(service));
  };

  public JobResource resource(JobDao dao, JobPublisher publisher) {
    return new JobResource(dao, publisher);
  }



  public JobPublisher publisher(String streamName, ExecutorService service) {
    return new JobPublisher(service, kinesis(), streamName);
  }


  public abstract AmazonKinesis kinesis();

  public abstract JobProcessorFactory processorFactory(ExecutorService service);

  public abstract AWSCredentialsProvider credentials();


}