package com.notif.backend.controller;

import com.notif.backend.dto.UserDTO;
import com.notif.backend.entity.User;
import com.notif.backend.service.UserService;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    private UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping(value = "/api/users")
    public ResponseEntity<List<UserDTO>> getUserData() {
        try {
            return new ResponseEntity<>(userService.getUserData(), HttpStatusCode.valueOf(200));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(500));
        }
    }

    @PutMapping(value = "/user")
    public ResponseEntity<UserDTO> updateUserData(@RequestBody UserDTO userDto) {
        try {
            UserDTO updated = userService.updateUserData(userDto);
            return ResponseEntity.ok(updated); //Status 200
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping(value = "/user")
    public ResponseEntity updateUserData(@RequestParam(value = "userId") Long userId) {
        try {
            userService.deleteUserData(userId);
            return new ResponseEntity<>(HttpStatusCode.valueOf(200));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(500));
        }
    }
}
