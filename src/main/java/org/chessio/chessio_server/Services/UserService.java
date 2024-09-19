// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class defines the user service used to communicate with the user table

package org.chessio.chessio_server.Services;

import org.chessio.chessio_server.Models.User;
import org.chessio.chessio_server.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService
{
    @Autowired
    private UserRepository userRepository;

    public void saveUser(User user)
    {
        userRepository.save(user);
    }

    public boolean userExistsByUsername(String userName)
    {
        return userRepository.findByuserName(userName).isPresent();
    }

    public Optional<User> getUserByUsername(String userName)
    {
        return userRepository.findByuserName(userName);
    }
}
