package org.chessio.chessio_server;

import org.chessio.chessio_server.Models.Game;
import org.chessio.chessio_server.Models.User;
import org.chessio.chessio_server.Repositories.GameRepository;
import org.chessio.chessio_server.Repositories.UserRepository;
import org.chessio.chessio_server.Services.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ChessioServerApplicationTests {

	@Autowired
	private GameService gameService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private GameRepository gameRepository;

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

		Game game = new Game();
		game.setPlayer1(player1);
		game.setPlayer2(player2);
		game.setWinner(player1);

		gameService.saveGame(game);

		List<Game> games = gameService.getPlayerGameHistory(player1.getUserID());
		assertFalse(games.isEmpty(), "Game history should not be empty for player1");

		// Check if the game created is in the list
		assertTrue(games.stream().anyMatch(g -> g.getPlayer1().getUserName().equals(player1.getUserName())
						&& g.getPlayer1().getPassword().equals(player1.getPassword())
						&& g.getPlayer2().getUserName().equals(player2.getUserName())
						&& g.getPlayer2().getPassword().equals(player2.getPassword())
						&& g.getWinner().getUserName().equals(player1.getUserName())
						&& g.getWinner().getPassword().equals(player1.getPassword())
        ), "Game history should contain the game played by player1");
	}

	@Test
	public void testPlayerNotFound() {
		// Use a random UUID for non-existent player
		UUID nonExistentPlayerId = UUID.randomUUID();

		List<Game> games = gameService.getPlayerGameHistory(nonExistentPlayerId);
		assertTrue(games.isEmpty(), "Game history should be empty for non-existent player");
	}

	@Test
	public void testEmptyGameHistory() {
		// Create a new player with no games
		User player = new User();
		player.setUserName("player");
		player.setPassword("password");
		userRepository.save(player);

		List<Game> games = gameService.getPlayerGameHistory(player.getUserID());
		assertTrue(games.isEmpty(), "Game history should be empty for a new player with no games");
	}

}
