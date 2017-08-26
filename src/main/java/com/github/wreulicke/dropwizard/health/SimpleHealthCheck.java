package com.github.wreulicke.dropwizard.health;

import com.codahale.metrics.health.HealthCheck;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleHealthCheck extends HealthCheck {


  @Override
  protected Result check() throws Exception {
    return Result.healthy();
  }
}
