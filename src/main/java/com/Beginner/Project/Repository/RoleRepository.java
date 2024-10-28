package com.Beginner.Project.Repository;

import com.Beginner.Project.Model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByName(String name);

//    @Query("SELECT u FROM User u WHERE u.role.id = :roleId")
//    Set<User> findUsersByRoleId(@Param("roleId") Long roleId);
}
