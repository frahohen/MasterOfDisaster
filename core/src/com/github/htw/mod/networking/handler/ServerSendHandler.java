package com.github.htw.mod.networking.handler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.github.htw.mod.networking.ServerThread;
import com.github.htw.mod.networking.message.Message;
import com.github.htw.mod.networking.message.MessageTag;
import com.github.htw.mod.point.MapPosition;

public class ServerSendHandler implements Runnable {

	private ObjectInputStream objectInputStream;
	private ObjectOutputStream objectOutputStream;
	private ServerThread serverThread;
	private Message message;
	
	public ServerSendHandler(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream,
			ServerThread serverThread) {
		super();
		this.objectInputStream = objectInputStream;
		this.objectOutputStream = objectOutputStream;
		this.serverThread = serverThread;
	}

	@Override
	public void run() {
		while (true) {
        	// Tick rate of 1 millisecond
        	try {
				Thread.sleep(1);
				
				if(serverThread.getTriggerMessage().equals(MessageTag.CONNECTED)){
	        		// Update Clients with new value
	        		message = new Message(MessageTag.CONNECTED, serverThread.getServer().getNumberOfClientsConnected());
	        		objectOutputStream.writeObject(message);
	        		// Reset triggerMessage
	        		serverThread.setTriggerMessage("");
	        	}
	        	
	        	if(serverThread.getTriggerMessage().equals(MessageTag.START)){
	        		message = new Message(MessageTag.START, true);
	        		objectOutputStream.writeObject(message);
	        		serverThread.setTriggerMessage("");
	        	}
	        	
	        	if(serverThread.getTriggerMessage().equals(MessageTag.POSITION)){
	        		
	        		// DEBUG: --------
					/*
					MapPosition mp = (MapPosition) serverThread.getServer().getPlayerAndPosition().get(serverThread.getId()+"");
					Gdx.app.log("SERVERTHREAD"+serverThread.getId(), mp.getX() +":"+ mp.getY());
					*/
					// DEBUG: --------
	        		
	        		message = new Message(MessageTag.POSITION, serverThread.getServer().getPlayerAndPosition());
	        		
	        		// DEBUG: --------
	        		/*
	        		MapPosition mp = (MapPosition) message.getHashmapMessage().get(serverThread.getId()+"");
					Gdx.app.log("SERVERTHREAD"+serverThread.getId(), mp.getX() +":"+ mp.getY());
					*/
					// DEBUG: --------
	        		
	        		objectOutputStream.writeObject(message);
	        		serverThread.setTriggerMessage("");
	        	}
	        	
	        	// Has to be done otherwise the stream gets overloaded
	        	objectOutputStream.reset();
	        	message = null;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	
        }
	}
	
}
