package org.chessio.chessio_server.ServerControllers;

import org.chessio.chessio_server.Models.OnlineGame;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;

public class ChessWebSocketHandler extends TextWebSocketHandler {

    private Map<String, WebSocketSession> waitingRoom = new HashMap<>(); // Players waiting for an opponent
    private Map<String, OnlineGame> activeGames = new HashMap<>(); // Active games mapped by gameId

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
            OnlineGame game = new OnlineGame(gameId, session, opponentSession);
            activeGames.put(gameId, game);

            // Notify both players that the game is starting and send game ID
            sendGameStartMessage(session, opponentSession, gameId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // The message format should include the gameId for us to know which game this message belongs to
        String[] msgParts = message.getPayload().split("\\|");
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
        } else {
            session.sendMessage(new TextMessage("invalid_game"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
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

    private void sendGameStartMessage(WebSocketSession player1, WebSocketSession player2, String gameId) throws Exception {
        player1.sendMessage(new TextMessage(gameId + "|game_start|white"));
        player2.sendMessage(new TextMessage(gameId + "|game_start|black"));
    }
}
