package org.chessio.chessio_server;

import org.chessio.chessio_server.Models.GameSummary;
import org.chessio.chessio_server.Models.User;
import org.chessio.chessio_server.Repositories.GameSummaryRepository;
import org.chessio.chessio_server.Repositories.UserRepository;
import org.chessio.chessio_server.Services.GameSummaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ChessioServerApplicationTests {

	@Autowired
	private GameSummaryService gameService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private GameSummaryRepository gameRepository;

	@Test
	public void testCreateGame() {
		User player1 = new User();
		player1.setUserName("player1");
		player1.setPassword("password1");
		userRepository.save(player1);

		User player2 = new User();
		player2.setUserName("player2");
		player2.setPassword("password2");
		userRepository.save(player2);

		GameSummary game = new GameSummary();
		game.setPlayer1(player1);
		game.setPlayer2(player2);
		game.setWinner(player1);

		gameService.saveGameSummary(game);

		List<GameSummary> games = gameService.getPlayerGameHistory(player1.getUserID());
		assertFalse(games.isEmpty(), "Game history should not be empty for player1");

		// Check if the game created is in the list
		assertTrue(games.stream().anyMatch(g -> g.getPlayer1().equals(player1)
						&& g.getPlayer2().equals(player2)
						&& g.getWinner().equals(player1)
        ), "Game history should contain the game played by player1");
	}

	@Test
	public void testPlayerNotFound() {
		// Use a random UUID for non-existent player
		UUID nonExistentPlayerId = UUID.randomUUID();

		List<GameSummary> games = gameService.getPlayerGameHistory(nonExistentPlayerId);
		assertTrue(games.isEmpty(), "Game history should be empty for non-existent player");
	}

	@Test
	public void testEmptyGameHistory() {
		// Create a new player with no games
		User player = new User();
		player.setUserName("player");
		player.setPassword("password");
		userRepository.save(player);

		List<GameSummary> games = gameService.getPlayerGameHistory(player.getUserID());
		assertTrue(games.isEmpty(), "Game history should be empty for a new player with no games");
	}

}
