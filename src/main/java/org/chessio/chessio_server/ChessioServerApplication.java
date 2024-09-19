// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class is the main spring server application class

package org.chessio.chessio_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChessioServerApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run(ChessioServerApplication.class, args);
	}
}