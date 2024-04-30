package com.bank.app.components.auth.controller;

import com.bank.app.components.auth.model.UserDto;
import com.bank.app.components.user.model.User;
import com.bank.app.configuration.JwtTokenUtil;
import com.bank.app.shared.dto.Roles;
import com.bank.app.shared.repository.UowService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import jakarta.annotation.security.RolesAllowed;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    private UowService uow;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private int pwd_token_expiration = 10000;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    // @RolesAllowed({Roles.ADMIN})
    @PatchMapping(path = "/patch/{id}")
    public ResponseEntity<?> patch(@PathVariable Long id, @RequestBody JsonPatch patch) {

        String role = uow.utils.getUser().get("role").toString();

        if (role.startsWith("A") || id == 0L) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Optional<User> optional = uow.users.findById(id);

        if (optional.isPresent() == false) {
            return ResponseEntity.notFound().build();
        }

        User target = optional.get();

        try {

            JsonNode patched = patch.apply(uow.objectMapper.convertValue(target, JsonNode.class));
        
            User model = uow.objectMapper.treeToValue(patched, User.class);
            
            uow.users.save(model);

            return ResponseEntity.noContent().build();

        } catch (JsonPatchException | JsonProcessingException e) {
            if(e.getCause() != null && e.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException se = (ConstraintViolationException)e.getCause();
                return new ResponseEntity<>(se.getSQLException().getMessage(),HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    @RolesAllowed({Roles.ADMIN})
    @PostMapping("/registerBy")
    public ResponseEntity<?> registerBy(@RequestBody User model) {
        Optional<User> userExist = uow.users.findByEmail(model.getEmail());

        if (userExist.isPresent() == true) {
            return ResponseEntity.ok(Map.of("code", -1, "message", "[DEV] email used"));
        }

        // if (model.getTermeAccepted() == false ) {
        //     return ResponseEntity.ok(Map.of("code", -2, "message", "Please accept the Terms of Use"));
        // }

        // model.setActive(Boolean.FALSE);

        // model.setRole(Roles.NEWBIE);

        model.setPassword(bCryptPasswordEncoder.encode(model.getPassword()));

       uow.users.save(model);

        return ResponseEntity.ok(Map.of("code", 1, "message", "Success"));

    }

    @GetMapping("/usersTest")
    public ResponseEntity<?> usersTest() {
        return ResponseEntity.ok(uow.users.findAll().stream().map(e -> Map.of("email", e.getEmail(), "role", e.getRole(), "id", e.getId())));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User model) {
        Optional<User> userExist = uow.users.findByEmail(model.getEmail());

        if (userExist.isPresent() == true) {
            return ResponseEntity.ok(Map.of("code", -1, "message", "[DEV] email used"));
        }

        // if (model.getTermeAccepted() == false ) {
        //     return ResponseEntity.ok(Map.of("code", -2, "message", "Please accept the Terms of Use"));
        // }

        // model.setActive(Boolean.FALSE);

        // model.setRole(Roles.NEWBIE);

        model.setPassword(bCryptPasswordEncoder.encode(model.getPassword()));

        User user = uow.users.save(model);

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("creation", new Date());

        String token = jwtTokenUtil.doGenerateToken(claims, user.getEmail());

        return ResponseEntity.ok(Map.of("code", 1, "message", "Success", "token", token));

    }

    

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDto model) {
        Optional<User> op = uow.users.findByEmail(model.email);

        if (op.isPresent() == false) {
            return ResponseEntity.ok(Map.of("code", -1, "message", "[DEV] email not found"));
        }

        User user = op.get();

        boolean pwEqual = bCryptPasswordEncoder.matches(model.password,user.getPassword());

        if (!pwEqual)
        {
            return ResponseEntity.ok(Map.of("code", -2, "message", "[DEV] password incorrect"));
        }

        if (!user.getActive())
        {
            return ResponseEntity.ok(Map.of("code", -3, "message", "[DEV] user disabled"));
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole());
        claims.put("id", user.getId());

        String token = jwtTokenUtil.doGenerateToken(claims, user.getEmail());

        return ResponseEntity.ok(Map.of("token", token, "user", user, "message", "Successful", "role", user.getRole()));
    }

    @PostMapping("/changePassword/{token}")
    public ResponseEntity<?> changePassword(@RequestBody UserDto model, @PathVariable("token") String token) {

        String email = null;
        Long creation = 0L;

        // if token is too short
        try {
            email = (String) jwtTokenUtil.getByClaim(token, "email");
            creation = (Long) jwtTokenUtil.getByClaim(token, "creation");
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("code", -1, "message", "Token not valid"));
        }

        if (token == null || token.trim().isEmpty() /*|| !email.toString().equals(model.email)*/)
            return ResponseEntity.ok(Map.of("code", -1, "message", "Token not valid"));

        // Token creation date
        Calendar cToken = Calendar.getInstance();
        cToken.setTimeInMillis(creation);
        cToken.add(Calendar.DATE, pwd_token_expiration);

        // New Date
        Date now = new Date();
        Calendar cNow = Calendar.getInstance();
        cNow.setTime(now);

        if (cToken.before(cNow))
            return ResponseEntity.ok(Map.of("code", -2, "message", "Expiration token"));

        Optional<User> op = uow.users.findByEmail(email.toString());

        if (op.isPresent() == false)
            return ResponseEntity.ok(Map.of("code", -1, "message", "No such a user"));

        User user = op.get();
        user.setPassword(bCryptPasswordEncoder.encode(model.password));
        user.setActive(Boolean.TRUE);

        uow.users.save(user);

        return ResponseEntity.ok(Map.of("code", 1, "message", "Successful", "email",user.getEmail(),"username",user.getFirstname() + " " + user.getLastname() ));
    }

    @PostMapping("/forgetPassword/{email}")
    public ResponseEntity<?> forgetPassword(@PathVariable("email") String email) {

        if (email == null || email.trim().isEmpty() || !email.contains("@") || !email.contains(".")) {
            return ResponseEntity.ok(Map.of("code", 1, "message", "Success"));
        }

        Optional<User> op = uow.users.findByEmail(email);

        if (!op.isPresent()) {
            return ResponseEntity.ok(Map.of("code", 1, "message", "Success"));
        }

        User user = op.get();

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("creation", new Date());

        String token = jwtTokenUtil.doGenerateToken(claims, user.getEmail());

        // mailService.forgetPassword(user, token);

        return ResponseEntity.ok(Map.of("code", 1, "message", "Success", "token", token));
    }

    @PostMapping("/disableCredentials/{email}")
    public ResponseEntity<?> disableCredentials(@PathVariable("email") String email) {

        if (email == null || email.trim().isEmpty() || !email.contains("@") || !email.contains(".")) {
            return ResponseEntity.ok(Map.of("code", -1, "message", "Format email not satisfied"));
        }

        Optional<User> op = uow.users.findByEmail(email);
        User user = op.get();
        if (user == null) {
            return ResponseEntity.ok(Map.of("code", -1, "message", "No such a user"));
        }

        user.setActive(false);
        uow.users.save(user);

        return ResponseEntity.ok(Map.of("code", 1, "message", "Success"));
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("target");
    }


}