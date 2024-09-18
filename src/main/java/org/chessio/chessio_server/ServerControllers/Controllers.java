package org.chessio.chessio_server.ServerControllers;

import org.chessio.chessio_server.Models.GameHistoryRequest;
import org.chessio.chessio_server.Models.GameSummary;
import org.chessio.chessio_server.Models.LightGameSummary;
import org.chessio.chessio_server.Models.User;
import org.chessio.chessio_server.Services.GameSummaryService;
import org.chessio.chessio_server.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


@RestController
public class Controllers
{

    @Autowired
    private UserService userService;

    @Autowired
    private GameSummaryService gameService;

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
    public LinkedList<LightGameSummary> getGameHistoryOfUser(@RequestBody GameHistoryRequest gameHistoryRequest)
    {
        Optional<User> _user = userService.getUserByUsername(gameHistoryRequest.getUserName());
        if (_user.isPresent()) {
            try
            {
                LinkedList<GameSummary> gameList = gameService.getPlayerGameHistory(_user.get().getUserID());
                System.out.println("game_history_retrieved_successfully: " + gameList.toString());
                return getLightGameSummaries(gameList);
            }
            catch (Exception e)
            {
                System.out.println("error in retrieving the games for user " + gameHistoryRequest.getUserName() + " , "
                        + Arrays.toString(e.getStackTrace()));
                return null;
            }
        }
        System.out.println("error in retrieving the games for user " + gameHistoryRequest.getUserName());
        return null;
    }

    private static LinkedList<LightGameSummary> getLightGameSummaries(LinkedList<GameSummary> gameList) {
        LinkedList<LightGameSummary> lightGameList = new LinkedList<>();
        for(GameSummary gameSummary : gameList)
        {
            LightGameSummary lightGameSummary = new LightGameSummary();
            lightGameSummary.setPlayer1(gameSummary.getPlayer1().getUserName());
            lightGameSummary.setPlayer2(gameSummary.getPlayer2().getUserName());
            if (gameSummary.getWinner() == null)
            {
                lightGameSummary.setWinner("draw");
            }
            else
            {
                lightGameSummary.setWinner(gameSummary.getWinner().getUserName());

            }
            lightGameList.add(lightGameSummary);
        }
        return lightGameList;
    }
}
