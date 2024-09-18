package org.chessio.chessio_server.Models;

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
