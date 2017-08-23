package com.example.helloworld.resources;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonDAO;
import com.google.common.base.Optional;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.LongParam;
import lombok.RequiredArgsConstructor;

@Path("/people/{personId}")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class PersonResource {
	
	private final PersonDAO peopleDAO;
	
	
	@GET
	@UnitOfWork
	public Person getPerson(@PathParam("personId") LongParam personId) {
		final Optional<Person> person = peopleDAO.findById(personId.get());
		if (!person.isPresent()) {
			throw new NotFoundException("No such user.");
		}
		return person.get();
	}
	
}
