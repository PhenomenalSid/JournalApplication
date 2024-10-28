package com.Beginner.Project.Repository;

import com.Beginner.Project.Model.Role;
import com.Beginner.Project.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserName(String userName);
    List<User> findByRole(Role role);
}
