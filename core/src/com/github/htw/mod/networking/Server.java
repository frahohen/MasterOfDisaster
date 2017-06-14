package com.github.htw.mod.networking;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.github.htw.mod.networking.message.MessageTag;
import com.github.htw.mod.point.MapPosition;

import java.util.ArrayList;
import java.util.HashMap;

public class Server implements Runnable {

    private String ip;
    private int port;
    private int id;
    private ArrayList<ServerThread> serverThreads;
    
    private int numberOfClientsConnected;
    private HashMap<String, MapPosition> playerAndPosition;

    public Server(String ip, int port) {
        this.ip = ip;
        this.port = port;
        numberOfClientsConnected = 0;
        id = 0;
        serverThreads = new ArrayList<ServerThread>();
        playerAndPosition = new HashMap<String, MapPosition>();
    }

    @Override
    public void run() {
        ServerSocketHints hints = new ServerSocketHints();
        ServerSocket server = Gdx.net.newServerSocket(Net.Protocol.TCP, port, hints);
        // Wait for the next client connection
        while (true) {
            try {
                Socket client = server.accept(null);

                if(client.isConnected()) {
                    Gdx.app.log("SERVER", "Client connected");
                    serverThreads.add(new ServerThread(this, client, id++));
                    new Thread(serverThreads.get(serverThreads.size()-1)).start();
                }
            }catch (Exception e){
                //Gdx.app.log("SERVER", "An error occured", e);
            	Gdx.app.log("SERVER:", "Waiting");
            }
        }
    }

    public synchronized void updateClients(String labelMessage){
        for(int i = 0; i < serverThreads.size(); i++){
            // Update everyone else but not the client that occured a change
        	if(labelMessage.equals(MessageTag.START)){
        		serverThreads.get(i).setTriggerMessage(labelMessage);
        	}
        	
        	if(labelMessage.equals(MessageTag.CONNECTED)){
        		//Create for every Server Thread a Message that is been send
        		serverThreads.get(i).setTriggerMessage(labelMessage);
        	}
        	
        	if(labelMessage.equals(MessageTag.POSITION)){
        		//Create for every Server Thread a Message that is been send
        		serverThreads.get(i).setTriggerMessage(labelMessage);
        	}
        }
    }

    public int getThreadSize() {
        return serverThreads.size();
    }

    public synchronized int getNumberOfClientsConnected() {
		return numberOfClientsConnected;
	}

	public synchronized void setNumberOfClientsConnected(int numberOfClientsConnected) {
		this.numberOfClientsConnected = numberOfClientsConnected;
	}

	public synchronized HashMap<String, MapPosition> getPlayerAndPosition() {
		return playerAndPosition;
	}

	public synchronized void setPlayerAndPosition(HashMap<String, MapPosition> playerAndPosition) {
		this.playerAndPosition = playerAndPosition;
	}

	public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
