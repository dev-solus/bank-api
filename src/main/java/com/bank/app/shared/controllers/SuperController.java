package com.bank.app.shared.controllers;

import com.bank.app.shared.repositories.GenericRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SuperController<T extends Serializable, ID> {


    protected  GenericRepository<T, ID> repository;

    public SuperController(GenericRepository<T, ID> repository) {
        this.repository = repository ;
    }

    @GetMapping("/getAll/{startIndex}/{pageSize}/{sortBy}/{sortDir}")
    public ResponseEntity<?> GetAll(@PathVariable int startIndex, @PathVariable int pageSize, @PathVariable String sortBy, @PathVariable String sortDir) {

        Sort sort = Sort.by(sortDir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);

        Page<T> query = repository.findAll(PageRequest.of(startIndex, pageSize, sort));

        List<T> list = query.getContent();

        Long count = query.getTotalElements();

        return ResponseEntity.ok(Map.of("count", count, "list", list));
    }

    @GetMapping("/get")
    public ResponseEntity<?> get(){
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<?> getById(@PathVariable ID id){

        Optional<T> model = repository.findById(id);

        if(model.isPresent() == false){
            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(model.get());
    }
    @PutMapping("/put/{id}")
    public ResponseEntity<?> put(@PathVariable ID id, @RequestBody T model){

        Optional<T> optional = repository.findById(id);

        if(optional.isPresent() == false){
            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        }

        T o = repository.save(model);

        return ResponseEntity.ok(o);
    }
    @PostMapping("/post")
    public ResponseEntity<?> post(@RequestBody T model){
        try {
            T o = repository.save(model);
            return ResponseEntity.ok(o);
        } catch (Exception e) {
            if(e.getCause() != null && e.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException se = (ConstraintViolationException)e.getCause();
                return new ResponseEntity<>(Map.of("message", se.getSQLException().getMessage()),HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(Map.of("message",e.getMessage()),HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/postRange")
    public ResponseEntity<?> postRange(@RequestBody List<T> models){
        try {
            List<T> list = repository.saveAllAndFlush(models);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            if(e.getCause() != null && e.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException se = (ConstraintViolationException)e.getCause();
                return new ResponseEntity<>(Map.of("message", se.getSQLException().getMessage()),HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(Map.of("message",e.getMessage()),HttpStatus.BAD_REQUEST);
        }
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable ID id){
        repository.deleteById(id);

        return ResponseEntity.ok(Boolean.TRUE);
    }
  /*  @PatchMapping(path = "/patch/{id}", consumes = "application/json-patch+json")
    public ResponseEntity<?> patch(@PathVariable ID id, @RequestBody JsonPatch patch) {

        Optional<T> optional = repository.findById(id);

        if (optional.isPresent() == false) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            T model = optional.get();

            JsonNode patched = patch.apply(objectMapper.convertValue(model, JsonNode.class));

            objectMapper.treeToValue(patched, model.getClass());

            repository.save(model);

        } catch (JsonPatchException | JsonProcessingException e) {
            if(e.getCause() != null && e.getCause() instanceof ConstraintViolationException) {
                ConstraintViolationException se = (ConstraintViolationException)e.getCause();
                return new ResponseEntity<>(se.getSQLException().getMessage(),HttpStatus.BAD_REQUEST);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }*/

}
