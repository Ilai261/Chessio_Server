// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class defines the online game object

package org.chessio.chessio_server.Models;

import org.springframework.web.socket.WebSocketSession;

// this object is used in the websocket handler, represents an ongoing game
public class OnlineGame
{
    // each game has an ID, two websocket sessions (one for each user) and current turn value
    private final String gameId;
    private final WebSocketSession playerWhite;
    private final WebSocketSession playerBlack;
    private String turn;

    public OnlineGame(String gameId, WebSocketSession playerWhite, WebSocketSession playerBlack) {
        this.gameId = gameId;
        this.playerWhite = playerWhite;
        this.playerBlack = playerBlack;
        this.turn = "white"; // White starts
    }

    public boolean containsSession(WebSocketSession session) {
        return session.equals(playerWhite) || session.equals(playerBlack);
    }

    public String getGameId() {
        return gameId;
    }

    public WebSocketSession getPlayerWhite() {
        return playerWhite;
    }

    public WebSocketSession getPlayerBlack() {
        return playerBlack;
    }

    public WebSocketSession getOpponent(WebSocketSession session) {
        return playerWhite.equals(session) ? playerBlack : playerWhite;
    }

    public String getTurn() {
        return turn;
    }

    public void switchTurn() {
        this.turn = this.turn.equals("white") ? "black" : "white";
    }
}

