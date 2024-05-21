package com.bank.app.components.role.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatch;

import java.time.LocalDate;
import java.util.*;

import jakarta.annotation.security.RolesAllowed;
import com.bank.app.shared.controllers.SuperController;
import com.bank.app.shared.repository.UowService;

import com.bank.app.shared.dto.Roles;
import com.bank.app.components.account.model.Account;
import com.bank.app.components.operation.model.Operation;
import com.bank.app.components.role.model.*;
import com.bank.app.components.role.repository.*;
import com.bank.app.components.user.model.User;

import org.springframework.data.domain.*;
import org.hibernate.exception.ConstraintViolationException;

@RestController
@RequestMapping(value ="api/roles", produces = { "application/json" })
public class RolesController extends SuperController<Role, Long> {

    public UowService uow;

    public RolesController(UowService uow) {
        super(uow.roles);
        this.uow = uow;
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/getAll/{startIndex}/{pageSize}/{sortBy}/{sortDir}/{name}")
    // @Override
    public ResponseEntity<?> GetAll(@PathVariable int startIndex, @PathVariable int pageSize,
            @PathVariable String sortBy, @PathVariable String sortDir, @PathVariable String name) {

        var sort = Sort.by(sortDir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);

        var query = repository.findAll((r, q, cb) -> name.equals("*") ? cb.and() : cb.like(cb.lower(r.get("name")), "%" + name.toLowerCase() + "%"),    
            PageRequest.of(startIndex, pageSize, sort));

        var list = query.getContent().stream().map(e -> new HashMap<String, Object>() {
            {
                put("id", e.getId());
                put("name", e.getName());
            }
        }).toList();

        var count = query.getTotalElements();

        return ResponseEntity.ok(Map.of("count", count, "list", list));
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/get")
    @Override
    public ResponseEntity<?> get() {
        return super.get();
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @PostMapping("/postRange")
    @Override
    public ResponseEntity<?> addRange(@RequestBody List<Role> models) {
        return super.addRange(models);
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @GetMapping("/getById/{id}")
    @Override
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return super.getById(id);
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @PutMapping("/update/{id}")
    @Override
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Role model) {
        return super.update(id, model);
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @PostMapping("/add")
    @Override
    public ResponseEntity<?> add(@RequestBody Role model) {
        return super.add(model);
    }

    @PatchMapping(path = "/patch/{id}")
    @Override
    public ResponseEntity<?> patch(@PathVariable Long id, @RequestBody JsonPatch patch) {
        Optional<Role> optional = repository.findById(id);

        if (optional.isPresent() == false) {
            return ResponseEntity.notFound().build();
        }

        Role target = optional.get();

        try {

            JsonNode patched = patch.apply(uow.objectMapper.convertValue(target, JsonNode.class));

            Role model = uow.objectMapper.treeToValue(patched, Role.class);

            repository.save(model);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            if (e.getCause() != null && e.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException se = (ConstraintViolationException) e.getCause();
                return new ResponseEntity<>(se.getSQLException().getMessage(), HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RolesAllowed({ Roles.CLIENT, Roles.AGENT_GUICHET })
    @DeleteMapping("/delete/{id}")
    @Override
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return super.delete(id);
    }

    @GetMapping("/generateTestingData")
    public ResponseEntity<?> generateTestingData() {

        // delete operations, accounts, users, roles
        uow.operations.deleteAll();
        uow.accounts.deleteAll();
        uow.users.deleteAll();
        // uow.roles.deleteAll();

        // generate 3 roles on each one 1 user as admin, 1 user as client, 1 user as
        // agent guichet
        // List<Role> roles = new ArrayList<Role>();

        // Role role = new Role();
        // role.setName("ADMIN");
        // roles.add(role);

        // role = new Role();
        // role.setName("CLIENT");
        // roles.add(role);

        // role = new Role();
        // role.setName("AGENT_GUICHET");
        // roles.add(role);

        // repository.saveAll(roles);

        // create users
        List<User> users = new ArrayList<User>();
        var password = uow.bCrypt.encode("123");

        User user = new User();
        user.setCin("AD1");
        user.setFirstname("Admin");
        user.setLastname("Admin");
        user.setAddress("Temara");
        user.setBirthdate(new Date(1990, 1, 1));
        user.setEmail("admin@bank.com");
        user.setRole_id(1L);
        user.setPassword(password);
        user.setActive(true);
        user.setPhone("0612345678");
        user.setGender("M");
        users.add(user);

        user = new User();
        user.setCin("AG1");
        user.setFirstname("Agent");
        user.setLastname("Agent");
        user.setAddress("Temara");
        user.setBirthdate(new Date(1990, 1, 1));
        user.setEmail("agent@bank.com");
        user.setRole_id(3L);
        user.setPassword(password);
        user.setPhone("0612345678");
        user.setGender("M");
        user.setActive(true);
        users.add(user);

        for (int i = 1; i <= 10; i++) {
            user = new User();
            user.setCin("AA" + i);
            user.setFirstname("Client_" + i);
            user.setLastname("Client_" + i);
            user.setAddress("Temara");
            user.setBirthdate(new Date(1990, 1, 1));
            user.setEmail("client_" + i + "@bank.com");
            user.setRole_id(2L);
            user.setPassword(password);
            user.setActive(true);
            user.setPhone("0612345678");
            user.setGender("M");
            users.add(user);
        }

        uow.users.saveAll(users);

        var accounts = new ArrayList<Account>();
        // for each user generate 2 accounts
        for (User u : users.stream().filter(e -> e.getRole_id().equals(2L)).toList()) {
            for (int i = 0; i < 2; i++) {
                Account account = new Account();
                account.setBalance(10000L);
                account.setAccountNumber("TN" + (int) (Math.random() * 1000000));
                account.setStatus(i == 0 ? "Clôturé" : "Ouvert");
                account.setUser_id(u.getId());
                accounts.add(account);
            }
        }

        uow.accounts.saveAll(accounts);

        var operations = new ArrayList<Operation>();
        // for each account generate 2 operations
        var firstFiveAccounts = accounts.stream().filter(e -> e.getStatus().equals("Ouvert")).limit(5).toList();

        var latestOpenAccounts = accounts.stream()
                .filter(e -> e.getStatus().equals("Ouvert"))
                .sorted(Comparator.comparing(Account::getId).reversed())
                .limit(5)
                .toList();

        for (int i = 0; i < 5; i++) {

            var debit = firstFiveAccounts.get(i);
            
            for (int j = 0; j < 10; j++) {
                var random = new Random();
                var randomNumber = random.nextInt(5);
                var credit = latestOpenAccounts.get(randomNumber);


                var operation = new Operation();

                operation.setAmount(150L);
                operation.setDescription("Testing data");

                operation.setDate(new Date());
                operation.setOperationType("");

                operation.setAccountDebit_id(debit.getId());
                operation.setAccountCredit_id(credit.getId());

                debit.setBalance(debit.getBalance() - 150);
                credit.setBalance(credit.getBalance() + 150);

                operations.add(operation);
            }
        }

        uow.operations.saveAll(operations);
        uow.accounts.saveAll(firstFiveAccounts);
        uow.accounts.saveAll(latestOpenAccounts);

        return ResponseEntity.ok(Map.of("message", "Testing data generated successfully"));
    }

}