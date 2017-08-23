package com.github.wreulicke.dropwizard.core;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;

@Value
public class Saying {
	@JsonProperty
	private final long id;
	
	@JsonProperty
	@Length(max = 3)
	private final String content;
	
}
