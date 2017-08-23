package com.example.helloworld.core;

import java.security.Principal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class User implements Principal {
	private final String name;
	
}
