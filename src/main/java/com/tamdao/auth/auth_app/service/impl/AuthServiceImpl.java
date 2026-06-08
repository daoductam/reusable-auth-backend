package com.tamdao.auth.auth_app.service.impl;

import com.tamdao.auth.auth_app.dto.UserDTO;
import com.tamdao.auth.auth_app.service.AuthService;
import com.tamdao.auth.auth_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO registerUser(UserDTO userDTO) {
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        return userService.createUser(userDTO);
    }


}
