package org.chessio.chessio_server.ServerControllers;

import org.chessio.chessio_server.Models.Game;
import org.chessio.chessio_server.Models.User;
import org.chessio.chessio_server.Services.GameService;
import org.chessio.chessio_server.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
public class Controllers
{

    @Autowired
    private UserService userService;

    @Autowired
    private GameService gameService;

    @PostMapping(value = "register")
    public String attemptToRegisterUser(@RequestBody User user)
    {
        // check if username already exists
        if (userService.userExistsByUsername(user.getUserName())) // if it does then return an error
        {
            return "User_already_exists";
        }
        else // else register the user and return success
        {
            userService.saveUser(user);
            return "Registered_successfully";
        }
    }

    @PostMapping(value = "login")
    public String attemptToLoginUser(@RequestBody User user)
    {
        Optional<User> _user = userService.getUserByUsername(user.getUserName());
        if (_user.isPresent()) {
            boolean userValid = _user.get().getPassword().equals(user.getPassword()); // compare both passwords
            if (userValid) {
                return "login_successful";
            }
        }
        return "login_failed";
    }

    @PostMapping(value = "createGame")
    public String createGame(@RequestBody Game game) {
        gameService.saveGame(game);
        return "Game_created_successfully";
    }

}
