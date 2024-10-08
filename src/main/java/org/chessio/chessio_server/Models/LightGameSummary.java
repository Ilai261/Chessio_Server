// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class defines the light game summary, used to send info to the client about a game. doesn't contain users
// passwords and UUIDs

package org.chessio.chessio_server.Models;

// the lighter game summary object
public class LightGameSummary
{
    private String player1;
    private String player2;
    private String winner;

    public LightGameSummary() {}

    public LightGameSummary(String player1, String player2, String winner)
    {
        this.player1 = player1;
        this.player2 = player2;
        this.winner = winner;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }
}
