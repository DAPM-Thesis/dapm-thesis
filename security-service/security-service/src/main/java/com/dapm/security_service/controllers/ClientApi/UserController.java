package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.User;
import com.dapm.security_service.models.dtos.UserDto;
import com.dapm.security_service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(UserDto::new)
                .toList();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable UUID id) {
        return userRepository.findById(id).map(UserDto::new).orElse(null);
    }

    @PostMapping
    public UserDto createUser(@RequestBody User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
        }
        return new UserDto(userRepository.save(user));
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable UUID id, @RequestBody User user) {
        user.setId(id);
        return new UserDto(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {
        userRepository.deleteById(id);
    }
}
