package org.chessio.chessio_server.Server_controller;

import org.chessio.chessio_server.Models.User;
import org.chessio.chessio_server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class Controllers
{
    @Autowired
    private UserRepository userRepository;

    @GetMapping(value = "/")
    public String getPage()
    {
        return "Hello World";
    }

    @GetMapping(value = "users")
    public List<User> getUsers()
    {
        return userRepository.findAll();
    }

    @PostMapping(value = "save")
    public String saveUser(@RequestBody User user)
    {
        userRepository.save(user);
        return "Saved";
    }

    @PutMapping(value = "update/{id}")
    public String updateUser(@PathVariable long id, @RequestBody User user)
    {
        User updatedUser = userRepository.findById(id).get();
        updatedUser.setUserName(user.getUserName());
        updatedUser.setPassword(user.getPassword());
        userRepository.save(updatedUser);
        return "Updated";
    }

    @DeleteMapping(value = "delete/{id}")
    public String deleteUser(@PathVariable long id)
    {
        userRepository.deleteById(id);
        return String.format("deleted user with id %d", id);
    }

}
