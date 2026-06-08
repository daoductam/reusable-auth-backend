package com.tamdao.auth.auth_app.service;

import com.tamdao.auth.auth_app.dto.UserDTO;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    UserDTO getUserByEmail(String email);
    UserDTO updateUser(UserDTO userDTO, String userId);
    void deleteUser(String userId);
    UserDTO getUserById(String userID);
    Iterable<UserDTO> getAllUsers();
}
