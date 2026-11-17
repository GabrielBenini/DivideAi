package com.github.gabriel.benini.DivideAi.user;

import com.github.gabriel.benini.DivideAi.common.exception.ResourceNotFoundException;
import com.github.gabriel.benini.DivideAi.user.dto.CreateUserRequest;
import com.github.gabriel.benini.DivideAi.user.dto.UpdateUserRequest;
import com.github.gabriel.benini.DivideAi.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        User user = new User();
        user.setName(request.name());

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = findUserOrThrow(id);
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = findUserOrThrow(id);

        user.setName(request.name());

        User updatedUser = userRepository.save(user);

        return toResponse(updatedUser);
    }

    @Transactional
    public void delete(Long id) {
        User user = findUserOrThrow(id);
        userRepository.delete(user);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + id
                        )
                );
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getCreatedAt()
        );
    }
}