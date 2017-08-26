package com.github.wreulicke.dropwizard.job;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.hibernate.UnitOfWork;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Path("job")
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class JobResource {

  private final JobDao dao;

  private final JobPublisher publisher;

  @Path("/{name}")
  @GET
  @UnitOfWork
  @Timed(name = "job")
  public Job register(@PathParam("name") String name) {
    log.info("start job");
    Job job = new Job();
    job.setJob(name);
    Job registeredJob = dao.create(job);
    publisher.publish(registeredJob);
    log.info("registered job");
    return registeredJob;
  }
}
