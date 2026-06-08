package com.tamdao.auth.auth_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Role {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(unique = true, nullable = false)
    private String name;
}
