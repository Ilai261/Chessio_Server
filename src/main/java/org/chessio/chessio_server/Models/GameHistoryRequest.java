// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class defines the GameHistoryRequest object

package org.chessio.chessio_server.Models;

// game history request only contains the username of the client that sends the request
public class GameHistoryRequest
{
    private String userName;

    public GameHistoryRequest() {}

    public GameHistoryRequest(String userName)
    {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
