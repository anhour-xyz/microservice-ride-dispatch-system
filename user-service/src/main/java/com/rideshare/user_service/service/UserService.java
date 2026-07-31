package com.rideshare.user_service.service;

import com.rideshare.user_service.dto.CreateUserRequest;
import com.rideshare.user_service.dto.UpdateUserRequest;
import com.rideshare.user_service.dto.UserResponse;
import com.rideshare.user_service.entity.User;
import com.rideshare.user_service.exception.PhoneNumberAlreadyExistsException;
import com.rideshare.user_service.exception.UserNotFoundException;
import com.rideshare.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsById(request.userId())) {
            return getUser(request.userId());
        }
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new PhoneNumberAlreadyExistsException();
        }

        User user = new User(
                request.userId(),
                request.displayName().trim(),
                request.phoneNumber().trim(),
                request.roles()
        );
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(UUID userId) {
        return toResponse(findUser(userId));
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = findUser(userId);
        String displayName = request.displayName() == null
                ? user.getDisplayName()
                : request.displayName().trim();
        String phoneNumber = request.phoneNumber() == null
                ? user.getPhoneNumber()
                : request.phoneNumber().trim();

        if (userRepository.existsByPhoneNumberAndUserIdNot(phoneNumber, userId)) {
            throw new PhoneNumberAlreadyExistsException();
        }

        user.updateProfile(displayName, phoneNumber);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        User user = findUser(userId);
        user.deactivate();
        userRepository.save(user);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getUserId(), user.getDisplayName(), user.getPhoneNumber(),
                user.getRoles(), user.getStatus(), user.getCreatedAt(), user.getUpdatedAt()
        );
    }
}
