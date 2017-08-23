package com.example.helloworld.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonDAO;

import io.dropwizard.hibernate.UnitOfWork;
import lombok.RequiredArgsConstructor;

@Path("/people")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class PeopleResource {
	
	private final PersonDAO peopleDAO;
	
	
	@POST
	@UnitOfWork
	public Person createPerson(Person person) {
		return peopleDAO.create(person);
	}
	
	@GET
	@UnitOfWork
	public List<Person> listPeople() {
		return peopleDAO.findAll();
	}
	
}
