package com.Beginner.Project.Exceptions;

import com.Beginner.Project.Exceptions.AdminException.DeleteAdminException;
import com.Beginner.Project.Exceptions.AdminException.DeleteAdminJournalException;
import com.Beginner.Project.Exceptions.JournalExceptions.JournalNotFoundException;
import com.Beginner.Project.Exceptions.JournalExceptions.NoUserJournalsExistException;
import com.Beginner.Project.Exceptions.UserException.NoUsersExistException;
import com.Beginner.Project.Exceptions.UserException.UserAlreadyExistsException;
import com.Beginner.Project.Exceptions.UserException.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(JournalNotFoundException.class)
    public ResponseEntity<String> handleJournalNotFound(JournalNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DeleteAdminException.class)
    public ResponseEntity<String> handleDeleteAdminException(DeleteAdminException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DeleteAdminJournalException.class)
    public ResponseEntity<String> handleDeleteAdminJournalException(DeleteAdminJournalException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoUsersExistException.class)
    public ResponseEntity<String> handleNoUsersExistException(NoUsersExistException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoUserJournalsExistException.class)
    public ResponseEntity<String> handleNoUserJournalsExistException(NoUserJournalsExistException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
