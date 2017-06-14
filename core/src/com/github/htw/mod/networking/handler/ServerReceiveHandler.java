package com.github.htw.mod.networking.handler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.badlogic.gdx.Gdx;
import com.github.htw.mod.networking.ServerThread;
import com.github.htw.mod.networking.message.Message;
import com.github.htw.mod.networking.message.MessageTag;
import com.github.htw.mod.point.MapPosition;

public class ServerReceiveHandler implements Runnable {

	private ObjectInputStream objectInputStream;
	private ObjectOutputStream objectOutputStream;
	private ServerThread serverThread;
	private Message message;
	
	public ServerReceiveHandler(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream,
			ServerThread serverThread) {
		super();
		this.objectInputStream = objectInputStream;
		this.objectOutputStream = objectOutputStream;
		this.serverThread = serverThread;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				Thread.sleep(1);
				
				message = (Message) objectInputStream.readObject();
				if( message != null){
					if(message.getLabelMessage().equals(MessageTag.POSITION)){
						//Gdx.app.log("SERVERTHREAD", message.getLabelMessage() + ":" + message.getPointPosition().getX() + ":" + message.getPointPosition().getY());
						serverThread.getServer().getPlayerAndPosition().put(serverThread.getId()+"",message.getMapPosition());
						
						// DEBUG: --------
						/*
						MapPosition mp = (MapPosition) serverThread.getServer().getPlayerAndPosition().get(serverThread.getId()+"");
						Gdx.app.log("SERVERTHREAD"+serverThread.getId(), mp.getX() +":"+ mp.getY());
						*/
						// DEBUG: --------
						
			    		serverThread.getServer().updateClients(message.getLabelMessage());
			    	}
				}
				
				message = null;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}

}
