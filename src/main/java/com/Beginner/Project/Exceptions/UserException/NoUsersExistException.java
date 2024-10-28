package com.Beginner.Project.Exceptions.UserException;

public class NoUsersExistException extends RuntimeException {
    public NoUsersExistException(String message){
        super(message);
    }
}
