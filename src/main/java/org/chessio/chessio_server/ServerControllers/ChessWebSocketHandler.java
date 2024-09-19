// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class contains the ChessWebSocketHandler class which defines every game websocket

package org.chessio.chessio_server.ServerControllers;

import org.chessio.chessio_server.Models.GameSummary;
import org.chessio.chessio_server.Models.OnlineGame;
import org.chessio.chessio_server.Models.User;
import org.chessio.chessio_server.Services.GameSummaryService;
import org.chessio.chessio_server.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChessWebSocketHandler extends TextWebSocketHandler
{

    private final Map<String, WebSocketSession> waitingRoom = new HashMap<>(); // players waiting for an opponent
    private final Map<String, OnlineGame> activeGames = new HashMap<>(); // active games mapped by gameId
    private final Map<String, String> sessionToUsername = new ConcurrentHashMap<>();  // maps session IDs to usernames

    @Autowired
    private UserService userService;

    @Autowired
    private GameSummaryService gameSummaryService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception
    {
        String playerId = session.getId();

        if (waitingRoom.isEmpty()) {
            waitingRoom.put(playerId, session); // add player to waiting room
            sendWaitingRoomMessage(session);
        } else {
            // pair with the first player in the waiting room
            Map.Entry<String, WebSocketSession> waitingPlayer = waitingRoom.entrySet().iterator().next();
            String opponentId = waitingPlayer.getKey();
            WebSocketSession opponentSession = waitingPlayer.getValue();

            waitingRoom.remove(opponentId); // remove from waiting room

            // generate a unique game ID
            String gameId = UUID.randomUUID().toString();

            // create a new game session
            boolean isPlayer1White = new Random().nextBoolean(); // randomly decide who is white
            OnlineGame game;
            if(isPlayer1White) {
                game = new OnlineGame(gameId, session, opponentSession);
            }
            else
            {
                game = new OnlineGame(gameId, opponentSession, session);
            }
            activeGames.put(gameId, game);

            // notify both players that the game is starting and send game ID
            checkAndStartGame(session, opponentSession, gameId);
        }
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception
    {
        // the message format should include the gameId for us to know which game this message belongs to
        String[] msgParts = message.getPayload().split("\\|");

        // first message - includes username
        switch (msgParts[0]) {
            case "username" -> handleUsernameMessage(session, msgParts);
            case "quit_waiting_room" -> handleQuitWaitingRoom(session, msgParts[1]);  // msgParts[1] is the username
            // handle draw offers
            case "draw_offer", "draw_offer_rejected", "draw_offer_accepted" -> handleDrawOfferMsg(session, msgParts);
            case "resignation" -> handleResignationMessage(session, msgParts);
            default -> handleGameMovesMessages(session, msgParts);
        }
    }

    private void handleUsernameMessage(WebSocketSession session, String[] msgParts) {
        // this message is for setting the username
        String username = msgParts[1];
        sessionToUsername.put(session.getId(), username);

        // check if this session is part of an active game and if we can start it
        activeGames.values().forEach(game -> {
            if (game.containsSession(session)) {
                try
                {
                    checkAndStartGame(game.getPlayerWhite(), game.getPlayerBlack(), game.getGameId());
                } catch (Exception e) {
                    System.out.println("Game starting failed: " + e.getMessage());
                }
            }
        });
    }

    private void handleQuitWaitingRoom(WebSocketSession session, String username) {
        // remove the player from the waiting room if present
        waitingRoom.values().removeIf(existingSession -> existingSession.equals(session));
        System.out.println("Player " + username + " removed from waiting room.");
    }

    private void handleGameMovesMessages(WebSocketSession session, String[] msgParts) throws IOException
    {
        // handle game moves
        String gameId = msgParts[0];  // gameId is the first part of the message
        String move = msgParts[1];    // move is the second part

        OnlineGame game = activeGames.get(gameId);

        if (game != null)
        {
            WebSocketSession opponentSession = game.getOpponent(session);

            // forward the move to the opponent if it's their turn
            if (game.getTurn().equals(session.equals(game.getPlayerWhite()) ? "white" : "black")) {
                opponentSession.sendMessage(new TextMessage(gameId + "|" + move));

                // switch the turn in the game
                game.switchTurn();
            }
            else
            {
                session.sendMessage(new TextMessage("not_your_turn"));
            }
        }
        // player ended the game
        else if (move.equals("white_win") || move.equals("black_win") || move.equals("draw")) {
            // handle game end and store game summary in the database
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
            // if player offers a draw to opponent, send it to him
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
            // notify the sender that his draw offer has been rejected
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
        // handle game end and store game summary in the database
        String gameId = msgParts[2];
        String move = msgParts[1];
        OnlineGame game = activeGames.get(gameId);

        if (game != null) {
            // send resignation message to opponent
            WebSocketSession opponentSession = game.getOpponent(session);
            opponentSession.sendMessage(new TextMessage("enemy_resigned|" + gameId));
            handleGameEnd(gameId, move);
        }
    }

    private void handleGameEnd(String gameId, String result)
    {
        OnlineGame game = activeGames.get(gameId);

        if (game != null) {
            // retrieve usernames for player1 and player2
            String player1Username = sessionToUsername.get(game.getPlayerWhite().getId());
            String player2Username = sessionToUsername.get(game.getPlayerBlack().getId());

            // fetch player information from the database using the usernames
            User player1 = userService.getUserByUsername(player1Username).orElse(null);
            User player2 = userService.getUserByUsername(player2Username).orElse(null);

            // determine winner based on result
            User winner = null;
            if (result.equals("white_win")) {
                winner = player1;
            } else if (result.equals("black_win")) {
                winner = player2;
            }

            // create a GameSummary
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
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) throws Exception
    {
        // remove the session from the username map when the connection is closed
        sessionToUsername.remove(session.getId());

        // remove the game and notify the opponent
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

    // sends a message indicating to the user that they are in the waiting room
    private void sendWaitingRoomMessage(WebSocketSession session) throws Exception {
        session.sendMessage(new TextMessage("waiting_for_opponent"));
    }

    private void checkAndStartGame(WebSocketSession player1, WebSocketSession player2, String gameId) throws Exception
    {
        String player1Username = sessionToUsername.get(player1.getId());
        String player2Username = sessionToUsername.get(player2.getId());

        if (player1Username != null && player2Username != null) {
            // both usernames are available, start the game
            // player1 is white and player2 is black
            player1.sendMessage(new TextMessage(gameId + "|game_start|white|" + player2Username));
            player2.sendMessage(new TextMessage(gameId + "|game_start|black|" + player1Username));
        }
    }
}
