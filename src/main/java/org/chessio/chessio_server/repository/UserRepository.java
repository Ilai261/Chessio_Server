package org.chessio.chessio_server.repository;

import org.chessio.chessio_server.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>
{

}
