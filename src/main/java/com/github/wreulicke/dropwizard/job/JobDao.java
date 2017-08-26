package com.github.wreulicke.dropwizard.job;

import java.util.List;

import org.hibernate.SessionFactory;

import com.google.common.base.Optional;

import io.dropwizard.hibernate.AbstractDAO;

public class JobDao extends AbstractDAO<Job> {
  public JobDao(SessionFactory factory) {
    super(factory);
  }

  public Optional<Job> findById(Long id) {
    return Optional.fromNullable(get(id));
  }

  public Job create(Job person) {
    return persist(person);
  }

  public List<Job> findAll() {
    return list(namedQuery("com.example.helloworld.core.Person.findAll"));
  }
}
