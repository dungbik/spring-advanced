package org.example.expert.config;

public class AdminAccessDeniedException extends RuntimeException {
	
	public AdminAccessDeniedException(String message) {
		super(message);
	}
}
