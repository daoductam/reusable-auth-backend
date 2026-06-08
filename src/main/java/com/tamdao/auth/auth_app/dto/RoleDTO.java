package com.tamdao.auth.auth_app.dto;

import jakarta.persistence.Column;
import lombok.*;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDTO {
    private UUID id;
    private String name;
}
