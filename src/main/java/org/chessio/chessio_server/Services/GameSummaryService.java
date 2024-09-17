package org.chessio.chessio_server.Services;

import org.chessio.chessio_server.Repositories.GameSummaryRepository;
import org.springframework.transaction.annotation.Transactional;
import org.chessio.chessio_server.Models.GameSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GameSummaryService {

    @Autowired
    private GameSummaryRepository gameRepository;

    // method to save a new game
    @Transactional
    public void saveGameSummary(GameSummary game)
    {
        gameRepository.save(game);
    }

    // method to retrieve a player's game history
    @Transactional(readOnly = true)
    public List<GameSummary> getPlayerGameHistory(UUID userId)
    {
        return gameRepository.findAllByPlayerId(userId);
    }
}
