package com.tamdao.auth.auth_app.service.impl;

import com.tamdao.auth.auth_app.dto.UserDTO;
import com.tamdao.auth.auth_app.entity.Provider;
import com.tamdao.auth.auth_app.entity.User;
import com.tamdao.auth.auth_app.exception.ResourceNotFoundException;
import com.tamdao.auth.auth_app.helpers.UserHelper;
import com.tamdao.auth.auth_app.repository.UserRepository;
import com.tamdao.auth.auth_app.security.JwtAuthenticationFilter;
import com.tamdao.auth.auth_app.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;


    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {

        if (userDTO.getEmail()==null || userDTO.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = modelMapper.map(userDTO, User.class);

        user.setProvider(userDTO.getProvider()!=null ? userDTO.getProvider() : Provider.LOCAL);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDTO.class);
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given email id"));
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO, String userId) {
        UUID uid =UserHelper.parseUUID(userId);
        User existingUser = userRepository
                .findById(uid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given id"));

        if (userDTO.getName()!=null) existingUser.setName(userDTO.getName());
        if (userDTO.getImage()!=null) existingUser.setImage(userDTO.getImage());
        if (userDTO.getProvider()!=null) existingUser.setProvider(userDTO.getProvider());
        if (userDTO.getPassword()!=null) existingUser.setPassword(userDTO.getPassword());
        existingUser.setEnable(userDTO.isEnable());
        existingUser.setUpdatedAt(Instant.now());
        User updatedUser = userRepository.save(existingUser);
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    @Override
    public void deleteUser(String userId) {
        UUID uId = UserHelper.parseUUID(userId);
        userRepository.findById(uId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given id"));
        userRepository.deleteById(uId);
    }

    @Override
    public UserDTO getUserById(String userID) {
        User user = userRepository.findById(UserHelper.parseUUID(userID))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given id"));
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    @Transactional
    public Iterable<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .toList();
    }
}
