package com.bank.app.components.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.bank.app.components.user.model.User;
import com.bank.app.shared.controllers.SuperController;
import com.bank.app.shared.repositories.UowService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("users")
public class UserController  extends SuperController<User, Long> {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private UowService uow;

	public UserController(UowService uow) {
		super(uow.users);
		this.uow = uow;
	}

    @GetMapping("/getAll/{startIndex}/{pageSize}/{sortBy}/{sortDir}/{name}/{roleId}")
    // @Override
    public ResponseEntity<?> GetAll(@PathVariable int startIndex, @PathVariable int pageSize,
                                    @PathVariable String sortBy,@PathVariable String sortDir,
                                    @PathVariable String name, @PathVariable Long roleId) {

        Sort sort = Sort.by(Sort.Direction.DESC, sortBy);




        Page<User> query = repository.findAll( (r, q, cb) -> cb.and(
                        (name.equals("*") ? cb.and() : cb.or(
                                cb.like(cb.lower(r.get("firstname")), "%" + name.toLowerCase() + "%"),
                                cb.like(cb.lower(r.get("lastname")), "%" + name.toLowerCase() + "%")
                        )),

                        roleId == null || roleId <= 0 ?  cb.and() : cb.equal(r.get("roleId"), roleId)
                ),
                PageRequest.of(startIndex, pageSize, sort)
        );

        List<?> list = query.getContent().stream().map(e -> new HashMap<String, Object>() {
            {
                put("id", e.getId());
                put("firstname",  e.getFirstname() );
                put("lastname",  e.getLastname() );
                put("phone",  e.getPhone() );
                put("birthdate",  e.getBirthdate() );
                put("email",  e.getEmail() );
                put("address",  e.getAddress() );
                put("roleId",  e.getRoleId() );
                put("role",  e.getRole() );
                put("avater",e.getAvater());


            }
        }).toList();
        ;

        Long count = query.getTotalElements();

        return ResponseEntity.ok(Map.of("count", count, "list", list));
    }
    @GetMapping("/getById/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id){

        Optional<User> model = repository.findById(id);

        if(model.isPresent() == false){
            return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
        }
        model.get().setPassword(null);

        return ResponseEntity.ok(model.get());
    }


    @GetMapping("/get")
    public ResponseEntity<List<User>> get(){
        return ResponseEntity.ok(repository.findAll());
    }

  

}

