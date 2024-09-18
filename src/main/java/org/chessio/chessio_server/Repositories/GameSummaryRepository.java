package org.chessio.chessio_server.Repositories;

import org.chessio.chessio_server.Models.GameSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public interface GameSummaryRepository extends JpaRepository<GameSummary, UUID>
{
    @Query("SELECT g FROM GameSummary g WHERE g.player1.userID = :playerId OR g.player2.userID = :playerId")
    LinkedList<GameSummary> findAllByPlayerId(@Param("playerId") UUID playerId);
}

