package com.github.wreulicke.dropwizard;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.wreulicke.dropwizard.job.configure.JobConfigurer;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@EqualsAndHashCode(callSuper = false)
public class HelloWorldConfiguration extends Configuration {

  @Valid
  @NotNull
  @JsonProperty("database")
  private DataSourceFactory dataSourceFactory = new DataSourceFactory();


  @Valid
  @NotNull
  @JsonProperty("job")
  @Accessors(fluent = true)
  private JobConfigurer job;
}
