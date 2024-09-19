// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class contains the user repo

package org.chessio.chessio_server.Repositories;

import org.chessio.chessio_server.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>
{
    // finds user object by userName attribute
    Optional<User> findByuserName(String userName);
}
