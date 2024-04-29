package com.bank.app.components.user.model;

import com.bank.app.components.role.model.Role;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstname;
    private String lastname;
    private String phone;
    private LocalDateTime birthdate;
    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String cin;

    private String password;
    private String avater;
    private String gender;
    private String address;
    private boolean active;

    @Column
    private Long roleId;

    @ManyToOne()
    @JoinColumn(name = "roleId", updatable = false, insertable = false)
    private Role role;
}
