package com.plasticaudit.service;

import com.plasticaudit.entity.Role;
import com.plasticaudit.entity.User;
import com.plasticaudit.repository.RoleRepository;
import com.plasticaudit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * CO3 — Service layer for User management.
 * 
 * @Transactional ensures atomic operations on User entity.
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user, String roleName) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        user.addRole(role);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> findUsersByRole(String roleName) {
        return userRepository.findByRoleName(roleName);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void toggleUserStatus(Long userId) {
        userRepository.findById(userId).ifPresent(u -> {
            u.setEnabled(!u.isEnabled());
            userRepository.save(u);
        });
    }

    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
}
