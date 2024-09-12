package org.chessio.chessio_server.Models;

import jakarta.persistence.*;

import java.util.Set;
import java.util.UUID;

@Entity
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)", unique = true, nullable = false)
    private UUID userID;

    @Column(unique = true, nullable = false)
    private String userName;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "player1")
    private Set<Game> gamesAsPlayer1;

    @OneToMany(mappedBy = "player2")
    private Set<Game> gamesAsPlayer2;

    @OneToMany(mappedBy = "winner")
    private Set<Game> gamesWon;

    // Getters and Setters
    public UUID getUserID() {
        return userID;
    }

    public void setUserID(UUID userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Game> getGamesAsPlayer1() {
        return gamesAsPlayer1;
    }

    public void setGamesAsPlayer1(Set<Game> gamesAsPlayer1) {
        this.gamesAsPlayer1 = gamesAsPlayer1;
    }

    public Set<Game> getGamesAsPlayer2() {
        return gamesAsPlayer2;
    }

    public void setGamesAsPlayer2(Set<Game> gamesAsPlayer2) {
        this.gamesAsPlayer2 = gamesAsPlayer2;
    }

    public Set<Game> getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(Set<Game> gamesWon) {
        this.gamesWon = gamesWon;
    }
}

