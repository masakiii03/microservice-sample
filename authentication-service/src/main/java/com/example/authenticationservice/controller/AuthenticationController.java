package com.example.authenticationservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.example.authenticationservice.controller.parameter.GetAccessTokenParameter;
import com.example.authenticationservice.service.AuthenticationService;

@RestController
public class AuthenticationController {

    @Value("${github-oauth.redirect-url}")
    private String redirectUrl;

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/access_token")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<String> getAccessToken(@RequestBody GetAccessTokenParameter param) {

        String res = authenticationService.getAccessToken(param.getCode());
        return new ResponseEntity<>(res, HttpStatus.OK);

    }

    @GetMapping("/authentication")
    public ResponseEntity<String> getAuthentication(@RequestHeader("Authorization") String token) {

        String res = authenticationService.checkAccessToken(token);
        return new ResponseEntity<>(res, HttpStatus.OK);

    }

    @GetMapping("/redirect")
    public ModelAndView redirect(ModelMap model, String code) {
        model.addAttribute("code", code);
        return new ModelAndView("redirect:" + redirectUrl, model);
    }

}
