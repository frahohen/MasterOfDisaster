package com.github.htw.mod.networking;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.github.htw.mod.networking.handler.ClientReceiveHandler;
import com.github.htw.mod.networking.handler.ClientSendHandler;
import com.github.htw.mod.networking.message.Message;
import com.github.htw.mod.networking.message.MessageTag;
import com.github.htw.mod.point.MapPosition;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class Client implements Runnable {
    private String ip;
    private int port;

    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    
    private Message message;
    
    private int numberOfClientsConnected;
    private boolean startGame;
    
    private HashMap<String, MapPosition> playerAndPosition;
    private int id;
    
    private ClientSendHandler clientSendHandler;
    private ClientReceiveHandler clientReceiveHandler;

    public Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
        startGame = false;
        playerAndPosition = new HashMap<String, MapPosition>();
    }

    @Override
    public void run() {
        SocketHints hints = new SocketHints();
        Socket client = Gdx.net.newClientSocket(Net.Protocol.TCP, ip, port, hints);

        try {
            objectOutputStream = new ObjectOutputStream(client.getOutputStream());
            objectInputStream = new ObjectInputStream(client.getInputStream());
            
            // Connected
            message = new Message(MessageTag.CONNECTED);
            objectOutputStream.writeObject(message);
            
            // Communication Loop
            clientReceiveHandler = new ClientReceiveHandler(objectInputStream, objectOutputStream, this);
            clientSendHandler = new ClientSendHandler(objectInputStream, objectOutputStream, this);
            
            new Thread(clientSendHandler).start();
            new Thread(clientReceiveHandler).start();
            
        } catch (IOException e) {
            Gdx.app.log("CLIENT", "An error occured", e);
        } 

    }
    
	public ClientSendHandler getClientSendHandler() {
		return clientSendHandler;
	}

	public void setClientSendHandler(ClientSendHandler clientSendReceiveHandler) {
		this.clientSendHandler = clientSendReceiveHandler;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public synchronized HashMap<String, MapPosition> getPlayerAndPosition() {
		return playerAndPosition;
	}

	public synchronized void setPlayerAndPosition(HashMap<String, MapPosition> playerAndPosition) {
		this.playerAndPosition = playerAndPosition;
	}

	public boolean isStartGame() {
		return startGame;
	}

	public void setStartGame(boolean startGame) {
		this.startGame = startGame;
	}

	public int getNumberOfClientsConnected() {
		return numberOfClientsConnected;
	}

	public void setNumberOfClientsConnected(int numberOfClientsConnected) {
		this.numberOfClientsConnected = numberOfClientsConnected;
	}
}
