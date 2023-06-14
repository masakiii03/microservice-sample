package com.example.authenticationservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.authenticationservice.AuthenticationService;
import com.example.authenticationservice.controller.parameter.GetAccessTokenParameter;

@RestController
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/jwt")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<String> getJwt(@RequestBody GetAccessTokenParameter param) {

        String res = authenticationService.getJwt(param.getCode());
        return new ResponseEntity<>(res, HttpStatus.OK);

    }

}
