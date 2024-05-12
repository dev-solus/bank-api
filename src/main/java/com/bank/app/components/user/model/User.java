package com.bank.app.components.user.model;

import com.bank.app.components.role.model.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.bank.app.components.account.model.*;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String firstname;

    @Column
    private String lastname;

    @Column(unique = true)
    private String cin;

    @Column(unique = true)
    private String email;

    @Column
    private String password;

    @Column
    private String phone;

    @Column
    private Date birthdate;

    @Column
    private String avatar;

    @Column
    private String gender;

    @Column
    private String address;

    @Column
    private Boolean active;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", updatable = false, insertable = false)
    // @JsonIgnore
    private Role role;

    @Column
    private Long role_id;

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "user")
    private Set<Account> accounts;

}
