package com.github.wreulicke.dropwizard.resources;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.github.wreulicke.dropwizard.core.Saying;
import com.github.wreulicke.dropwizard.core.Template;
import com.google.common.base.Optional;

import io.dropwizard.jersey.caching.CacheControl;
import lombok.RequiredArgsConstructor;

@Path("/hello-world")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class HelloWorldResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorldResource.class);
	
	private final Template template;
	
	// CAUTION!! This implementation is example.
	private final AtomicLong counter = new AtomicLong();
	
	
	@GET
	@Timed(name = "get-requests")
	@CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
	public Saying sayHello(@QueryParam("name") Optional<String> name) {
		return new Saying(counter.incrementAndGet(), template.render(name));
	}
	
	@POST
	public void receiveHello(@Valid Saying saying) {
		LOGGER.info("Received a saying: {}", saying);
	}
}
