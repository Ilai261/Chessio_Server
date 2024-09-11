package org.chessio.chessio_server.Server_controller;

import org.chessio.chessio_server.Models.User;
import org.chessio.chessio_server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
public class Controllers
{
    @Autowired
    private UserRepository userRepository;


    @PostMapping(value = "register")
    public String attemptToRegisterUser(@RequestBody User user)
    {
        // check if username already exists
        if (this.userExistsByUsername(user.getUserName())) // if it does then return an error
        {
            return "User_already_exists";
        }
        else // else register the user and return success
        {
            userRepository.save(user);
            return "Registered_successfully";
        }
    }

    @PostMapping(value = "login")
    public String attemptToLoginUser(@RequestBody User user)
    {
        Optional<User> _user = this.getUserByUsername(user.getUserName());
        if (_user.isPresent()) {
            boolean userValid = _user.get().getPassword().equals(user.getPassword()); // compare both passwords
            if (userValid) {
                return "login_successful";
            }
        }
        return "login_failed";
    }


    private boolean userExistsByUsername(String userName)
    {
        return userRepository.findByuserName(userName).isPresent();
    }

    private Optional<User> getUserByUsername(String userName)
    {
        return userRepository.findByuserName(userName);
    }

}
