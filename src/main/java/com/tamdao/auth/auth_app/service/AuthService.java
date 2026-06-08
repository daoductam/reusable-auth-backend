package com.tamdao.auth.auth_app.service;

import com.tamdao.auth.auth_app.dto.UserDTO;

public interface AuthService {
    UserDTO registerUser(UserDTO userDTO);
}
