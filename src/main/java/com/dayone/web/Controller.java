package com.dayone.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class Controller {



    @GetMapping("/company/autocomplete")
    public ResponseEntity<?> autoComplete(@RequestParam String keyword){
        return null;
    }

    @GetMapping("/company")
    public ResponseEntity<?> searchCompany(){
        return null;
    }

    @PostMapping("/company")
    public ResponseEntity<?> addCompany(){
        return null;
    }

    @DeleteMapping("/company")
    public ResponseEntity<?> deleteCompany(){
        return null;
    }
}
