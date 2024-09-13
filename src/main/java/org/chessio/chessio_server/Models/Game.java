package org.chessio.chessio_server.Models;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Game
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)", unique = true, nullable = false)
    private UUID gameID;

    @ManyToOne()
    @JoinColumn(name = "player1_id", nullable = false)
    private User player1;

    @ManyToOne()
    @JoinColumn(name = "player2_id", nullable = false)
    private User player2;

    @ManyToOne()
    @JoinColumn(name = "winner_id")
    private User winner;

    // Getters and Setters
    public UUID getGameID() {
        return gameID;
    }

    public void setGameID(UUID gameID) {
        this.gameID = gameID;
    }

    public User getPlayer1() {
        return player1;
    }

    public void setPlayer1(User player1) {
        this.player1 = player1;
    }

    public User getPlayer2() {
        return player2;
    }

    public void setPlayer2(User player2) {
        this.player2 = player2;
    }

    public User getWinner() {
        return winner;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }
}
