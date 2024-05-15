package com.bank.app.components.operation.model;

import com.bank.app.components.account.model.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.bank.app.components.account.model.*;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "operations")
@Data
@NoArgsConstructor
@AllArgsConstructor
// @Transactional
public class Operation implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String operationType;

    @Column
    private String description;

    @Column
    private Long amount;

    @Column
    private Date date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "accountDebit_id", updatable = false, insertable = false)
    // @JsonIgnore
    private Account accountDebit;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "accountCredit_id", updatable = false, insertable = false)
    // @JsonIgnore
    private Account accountCredit;

    @Column
    private Long accountDebit_id;

    @Column
    private Long accountCredit_id;

}
