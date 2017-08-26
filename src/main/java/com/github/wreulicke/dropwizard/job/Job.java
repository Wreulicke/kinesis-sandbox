package com.github.wreulicke.dropwizard.job;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "job")
@NamedQueries({
  @NamedQuery(name = "com.example.helloworld.core.Job.findAll", query = "SELECT p FROM Job p"),
  @NamedQuery(name = "com.example.helloworld.core.Job.findById", query = "SELECT p FROM Job p WHERE p.id = :id")
})
@Data
public class Job {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(name = "job", nullable = false)
  private String job;

}
