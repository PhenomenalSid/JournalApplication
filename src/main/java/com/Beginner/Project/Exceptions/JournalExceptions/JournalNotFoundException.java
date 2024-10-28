package com.Beginner.Project.Exceptions.JournalExceptions;

public class JournalNotFoundException extends RuntimeException {
    public JournalNotFoundException(String message) {
        super(message);
    }
}