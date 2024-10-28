package com.Beginner.Project.Service;

import com.Beginner.Project.Model.Role;
import com.Beginner.Project.Model.User;
import com.Beginner.Project.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImplementation implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImplementation(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username);

        if (user != null) {
            Role role = user.getRole();

            List<GrantedAuthority> authorities = new ArrayList<>();
            if (role != null) {
                authorities.add(new SimpleGrantedAuthority(role.getName()));
            }

            return new org.springframework.security.core.userdetails.User(
                    user.getUserName(),
                    user.getPassword(),
                    authorities
            );
        }

        throw new UsernameNotFoundException("User with the username " + username + " is not found!");
    }
}
