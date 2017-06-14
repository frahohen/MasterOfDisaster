package com.github.htw.mod.networking.handler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.badlogic.gdx.Gdx;
import com.github.htw.mod.networking.Client;
import com.github.htw.mod.networking.message.Message;
import com.github.htw.mod.networking.message.MessageTag;
import com.github.htw.mod.point.MapPosition;

public class ClientSendHandler implements Runnable {

	private ObjectInputStream objectInputStream;
	private ObjectOutputStream objectOutputStream;
	private Client client;
	private Message message;
	private String triggerMessage;
	private MapPosition mapPosition;
	
	public ClientSendHandler(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream,
			Client client) {
		super();
		this.objectInputStream = objectInputStream;
		this.objectOutputStream = objectOutputStream;
		this.client = client;
		this.triggerMessage = "";
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			try {
				Thread.sleep(1);
				
				if(triggerMessage.equals(MessageTag.POSITION)){
					//Gdx.app.log("Debug", "I am sending");
					message = new Message(MessageTag.POSITION, mapPosition);
					objectOutputStream.writeObject(message);
					triggerMessage = "";
				}
				
				// Has to be done otherwise the stream gets overloaded
				objectOutputStream.reset();
				message = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void updateClients(String labelMessage, MapPosition mapPosition){
		//Gdx.app.log("DEBUG", labelMessage);
    	this.mapPosition = mapPosition;
    	this.triggerMessage = labelMessage;
    }
}
