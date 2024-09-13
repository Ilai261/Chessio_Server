package org.chessio.chessio_server.Services;

import org.springframework.transaction.annotation.Transactional;
import org.chessio.chessio_server.Models.Game;
import org.chessio.chessio_server.Repositories.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    // method to save a new game
    @Transactional
    public void saveGame(Game game)
    {
        gameRepository.save(game);
    }

    // method to retrieve a player's game history
    @Transactional(readOnly = true)
    public List<Game> getPlayerGameHistory(UUID userId)
    {
        return gameRepository.findAllByPlayerId(userId);
    }
}
