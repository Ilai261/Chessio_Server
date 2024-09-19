package org.chessio.chessio_server.ServerControllers;

import org.chessio.chessio_server.Models.GameSummary;
import org.chessio.chessio_server.Models.OnlineGame;
import org.chessio.chessio_server.Models.User;
import org.chessio.chessio_server.Services.GameSummaryService;
import org.chessio.chessio_server.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChessWebSocketHandler extends TextWebSocketHandler {

    private Map<String, WebSocketSession> waitingRoom = new HashMap<>(); // Players waiting for an opponent
    private Map<String, OnlineGame> activeGames = new HashMap<>(); // Active games mapped by gameId
    private Map<String, String> sessionToUsername = new ConcurrentHashMap<>();  // Maps session IDs to usernames

    @Autowired
    private UserService userService;

    @Autowired
    private GameSummaryService gameSummaryService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String playerId = session.getId();

        if (waitingRoom.isEmpty()) {
            waitingRoom.put(playerId, session); // Add player to waiting room
            sendWaitingRoomMessage(session);
        } else {
            // Pair with the first player in the waiting room
            Map.Entry<String, WebSocketSession> waitingPlayer = waitingRoom.entrySet().iterator().next();
            String opponentId = waitingPlayer.getKey();
            WebSocketSession opponentSession = waitingPlayer.getValue();

            waitingRoom.remove(opponentId); // Remove from waiting room

            // Generate a unique game ID
            String gameId = UUID.randomUUID().toString();

            // Create a new game session
            boolean isPlayer1White = new Random().nextBoolean(); // Randomly decide who is white
            OnlineGame game;
            if(isPlayer1White) {
                game = new OnlineGame(gameId, session, opponentSession);
            }
            else
            {
                game = new OnlineGame(gameId, opponentSession, session);
            }
            activeGames.put(gameId, game);

            // Notify both players that the game is starting and send game ID
            checkAndStartGame(session, opponentSession, gameId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception
    {
        // The message format should include the gameId for us to know which game this message belongs to
        String[] msgParts = message.getPayload().split("\\|");

        // first message - includes username
        switch (msgParts[0]) {
            case "username" -> {
                // This message is for setting the username
                String username = msgParts[1];
                sessionToUsername.put(session.getId(), username);

                // Check if this session is part of an active game and if we can start it
                activeGames.values().forEach(game -> {
                    if (game.containsSession(session)) {
                        try
                        {
                            checkAndStartGame(game.getPlayerWhite(), game.getPlayerBlack(), game.getGameId());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            // handle draw offers
            case "draw_offer", "draw_offer_rejected", "draw_offer_accepted" -> handleDrawOfferMsg(session, msgParts);
            case "resignation" -> handleResignationMessage(session, msgParts);
            default -> handleGameMovesMessages(session, msgParts);
        }
    }

    private void handleGameMovesMessages(WebSocketSession session, String[] msgParts) throws IOException
    {
        // handle game moves
        String gameId = msgParts[0];  // gameId is the first part of the message
        String move = msgParts[1];    // move is the second part

        OnlineGame game = activeGames.get(gameId);

        if (game != null) {
            WebSocketSession opponentSession = game.getOpponent(session);

            // Forward the move to the opponent if it's their turn
            if (game.getTurn().equals(session.equals(game.getPlayerWhite()) ? "white" : "black")) {
                opponentSession.sendMessage(new TextMessage(gameId + "|" + move));

                // Switch the turn in the game
                game.switchTurn();
            } else {
                session.sendMessage(new TextMessage("not_your_turn"));
            }
        }
        // player ended the game
        else if (move.equals("white_win") || move.equals("black_win") || move.equals("draw")) {
            // Handle game end and store game summary in the database
            gameId = msgParts[2];
            handleGameEnd(gameId, move);
        }
        // in case no case has worked, we have an error
        else
        {
            session.sendMessage(new TextMessage("invalid message from client: " + Arrays.toString(msgParts)));
        }
    }

    private void handleDrawOfferMsg(WebSocketSession session, String[] msgParts) throws IOException
    {
        switch (msgParts[0]) {
            case "draw_offer" -> {
                String gameId = msgParts[1];
                OnlineGame game = activeGames.get(gameId);

                if (game != null) {
                    WebSocketSession opponentSession = game.getOpponent(session);
                    if (opponentSession != null && opponentSession.isOpen()) {
                        opponentSession.sendMessage(new TextMessage("draw_offer|" + gameId));
                    }
                }
            }
            case "draw_offer_accepted" -> {
                String gameId = msgParts[1];

                // Notify the opponent that the draw offer was accepted
                OnlineGame game = activeGames.get(gameId);
                if (game != null) {
                    WebSocketSession opponentSession = game.getOpponent(session);
                    if (opponentSession != null && opponentSession.isOpen()) {
                        opponentSession.sendMessage(new TextMessage("draw_offer_accepted|" + gameId));
                    }
                }
                // end game and save it to db
                handleGameEnd(gameId, "draw");
            }
            case "draw_offer_rejected" -> {
                String gameId = msgParts[1];
                OnlineGame game = activeGames.get(gameId);

                if (game != null) {
                    WebSocketSession opponentSession = game.getOpponent(session);
                    if (opponentSession != null && opponentSession.isOpen()) {
                        opponentSession.sendMessage(new TextMessage("draw_offer_rejected|" + gameId));
                    }
                }
            }
        }
    }

    private void handleResignationMessage(WebSocketSession session, String[] msgParts) throws IOException {
        // Handle game end and store game summary in the database
        String gameId = msgParts[2];
        String move = msgParts[1];
        OnlineGame game = activeGames.get(gameId);

        if (game != null) {
            WebSocketSession opponentSession = game.getOpponent(session);
            opponentSession.sendMessage(new TextMessage("enemy_resigned|" + gameId));
            handleGameEnd(gameId, move);
        }
    }

    private void handleGameEnd(String gameId, String result)
    {
        OnlineGame game = activeGames.get(gameId);

        if (game != null) {
            // Retrieve usernames for player1 and player2
            String player1Username = sessionToUsername.get(game.getPlayerWhite().getId());
            String player2Username = sessionToUsername.get(game.getPlayerBlack().getId());

            // Fetch player information from the database using the usernames
            User player1 = userService.getUserByUsername(player1Username).orElse(null);
            User player2 = userService.getUserByUsername(player2Username).orElse(null);

            // Determine winner based on result
            User winner = null;
            if (result.equals("white_win")) {
                winner = player1;
            } else if (result.equals("black_win")) {
                winner = player2;
            }

            // Create a GameSummary
            GameSummary summary = new GameSummary();
            summary.setPlayer1(player1);
            summary.setPlayer2(player2);
            summary.setWinner(winner);

            // save the game to the db and remove it from the active games map
            gameSummaryService.saveGameSummary(summary);
            activeGames.remove(gameId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception
    {
        // Remove the session from the username map when the connection is closed
        sessionToUsername.remove(session.getId());

        // Remove the game and notify the opponent
        for (Map.Entry<String, OnlineGame> entry : activeGames.entrySet()) {
            OnlineGame game = entry.getValue();
            if (game.getPlayerWhite().equals(session) || game.getPlayerBlack().equals(session)) {
                WebSocketSession opponentSession = game.getOpponent(session);
                if (opponentSession != null && opponentSession.isOpen()) {
                    opponentSession.sendMessage(new TextMessage("opponent_disconnected"));
                    opponentSession.close();
                }
                activeGames.remove(entry.getKey());
                break;
            }
        }
    }

    private void sendWaitingRoomMessage(WebSocketSession session) throws Exception {
        session.sendMessage(new TextMessage("waiting_for_opponent"));
    }

    private void checkAndStartGame(WebSocketSession player1, WebSocketSession player2, String gameId) throws Exception
    {
        String player1Username = sessionToUsername.get(player1.getId());
        String player2Username = sessionToUsername.get(player2.getId());

        if (player1Username != null && player2Username != null) {
            // Both usernames are available, start the game
            // player1 is white and player2 is black
            player1.sendMessage(new TextMessage(gameId + "|game_start|white|" + player2Username));
            player2.sendMessage(new TextMessage(gameId + "|game_start|black|" + player1Username));
        }
    }
}
