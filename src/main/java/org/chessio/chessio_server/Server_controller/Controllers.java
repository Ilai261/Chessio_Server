package org.chessio.chessio_server.Server_controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controllers
{
    @GetMapping(value = "/")
    public String getPage()
    {
        return "Hello World";
    }
}
