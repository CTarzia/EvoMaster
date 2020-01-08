package com.foo.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/mongodb/customer")
public class CustomerRest {

    @Autowired
    private CustomerMongoRepository repository;

    @RequestMapping(
            method = RequestMethod.POST
    )
    public ResponseEntity post() {
        if (repository.findAll().isEmpty()) {
            CustomerEntity entity = new CustomerEntity("Alice", "Smith");
            repository.save(entity);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }


    @RequestMapping(
            method = RequestMethod.GET
    )
    public ResponseEntity get() {
        if (repository.findAll().isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok().build();
        }
    }

    @RequestMapping(
            method = RequestMethod.DELETE
    )
    public ResponseEntity delete() {
        if (repository.findAll().isEmpty()) {
        return ResponseEntity.notFound().build();
        } else {
            repository.deleteAll();
            return ResponseEntity.ok().build();
        }
    }
}
