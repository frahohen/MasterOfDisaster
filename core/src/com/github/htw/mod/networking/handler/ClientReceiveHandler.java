package com.github.htw.mod.networking.handler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.badlogic.gdx.Gdx;
import com.github.htw.mod.networking.Client;
import com.github.htw.mod.networking.message.Message;
import com.github.htw.mod.networking.message.MessageTag;
import com.github.htw.mod.point.MapPosition;

public class ClientReceiveHandler implements Runnable{

	private ObjectInputStream objectInputStream;
	private ObjectOutputStream objectOutputStream;
	private Client client;
	
	private Message message;
	
	public ClientReceiveHandler(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream,
			Client client) {
		super();
		this.objectInputStream = objectInputStream;
		this.objectOutputStream = objectOutputStream;
		this.client = client;
	}

	@Override
	public void run() {
		while (true) {
        	try {
        		Thread.sleep(1);
        		
				message = (Message)objectInputStream.readObject();
				
				if( message != null){
					
					if(message.getLabelMessage().equals(MessageTag.ID)){
						Gdx.app.log("DEBUG", message.getLabelMessage() + ":" + message.getIntMessage());
						client.setId(message.getIntMessage());
					}
					
					if(message.getLabelMessage().equals(MessageTag.CONNECTED)){
						Gdx.app.log("DEBUG", message.getLabelMessage() + ":" + message.getIntMessage());
						client.setNumberOfClientsConnected(message.getIntMessage());
					}
					
					if(message.getLabelMessage().equals(MessageTag.START)){
						Gdx.app.log("DEBUG", message.getLabelMessage() + ":" + message.isBooleanMessage());
						client.setStartGame(message.isBooleanMessage());
					}
					
					if(message.getLabelMessage().equals(MessageTag.POSITION)){
						
						// DEBUG -------
						/*
						for(int i = 0; i < message.getHashmapMessage().size(); i++) {
							MapPosition mp = message.getHashmapMessage().get(i+"");
							
							Gdx.app.log("Player"+i, mp.getX() + ":" +mp.getY());
						}
						*/
						// DEBUG -------
						
						client.setPlayerAndPosition(message.getHashmapMessage());
					}
				}
				
				message = null;
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}

}
