package org.chessio.chessio_server.Repositories;

import org.chessio.chessio_server.Models.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GameRepository extends JpaRepository<Game, UUID>
{
    @Query("SELECT g FROM Game g WHERE g.player1.userID = :playerId OR g.player2.userID = :playerId")
    List<Game> findAllByPlayerId(@Param("playerId") UUID playerId);
}

