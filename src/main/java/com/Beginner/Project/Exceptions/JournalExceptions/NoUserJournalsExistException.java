package com.Beginner.Project.Exceptions.JournalExceptions;

public class NoUserJournalsExistException extends RuntimeException {
    public NoUserJournalsExistException(String message){
        super(message);
    }
}
