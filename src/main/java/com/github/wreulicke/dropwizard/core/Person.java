package com.github.wreulicke.dropwizard.core;

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
@Table(name = "people")
@NamedQueries({
	@NamedQuery(name = "com.example.helloworld.core.Person.findAll", query = "SELECT p FROM Person p"),
	@NamedQuery(name = "com.example.helloworld.core.Person.findById", query = "SELECT p FROM Person p WHERE p.id = :id")
})
@Data
public class Person {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(name = "fullName", nullable = false)
	private String fullName;
	
	@Column(name = "jobTitle", nullable = false)
	private String jobTitle;
	
}
