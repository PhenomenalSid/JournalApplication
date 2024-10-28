package com.Beginner.Project.Initializer;

import com.Beginner.Project.Model.Role;
import com.Beginner.Project.Model.User;
import com.Beginner.Project.Repository.RoleRepository;
import com.Beginner.Project.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        if (roleRepository.findByName("ROLE_USER") == null) {
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            roleRepository.save(userRole);
        }

        if (roleRepository.findByName("ROLE_ADMIN") == null) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepository.save(adminRole);
        }

        User check = userRepository.findByUserName("admin");

        if(check == null)
        {
            User admin = new User();
            admin.setUserName("admin");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setRole(roleRepository.findByName("ROLE_ADMIN"));
            userRepository.save(admin);
        }
    }
}

