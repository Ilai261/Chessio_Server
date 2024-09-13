package org.chessio.chessio_server.ServerControllers;

import org.chessio.chessio_server.Models.Game;
import org.chessio.chessio_server.Models.User;
import org.chessio.chessio_server.Services.GameService;
import org.chessio.chessio_server.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
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
            return "user_already_exists";
        }
        else // else register the user and return success
        {
            userService.saveUser(user);
            return "registered_successfully";
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
        return "login_failed: UserName or password are incorrect";
    }

    @PostMapping(value = "game_history")
    public List<Game> getGameHistoryOfUser(@RequestBody String userName)
    {
        Optional<User> _user = userService.getUserByUsername(userName);
        if (_user.isPresent()) {
            try
            {
                List<Game> gameList = gameService.getPlayerGameHistory(_user.get().getUserID());
                System.out.println("game_history_retrieved_successfully");
                return gameList;
            }
            catch (Exception e)
            {
                System.out.println("error in retrieving the games for user " + userName + " , "
                        + Arrays.toString(e.getStackTrace()));
                return null;
            }
        }
        System.out.println("error in retrieving the games for user " + userName);
        return null;
    }
}
