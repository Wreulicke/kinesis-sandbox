package com.example.helloworld.core;

import static java.lang.String.format;

import com.google.common.base.Optional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Template {
	private final String content;
	
	private final String defaultName;
	
	
	public String render(Optional<String> name) {
		return format(content, name.or(defaultName));
	}
}
