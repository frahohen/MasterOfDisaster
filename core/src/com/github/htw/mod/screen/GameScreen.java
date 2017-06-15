package com.github.htw.mod.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.htw.mod.gameobject.Bullet;
import com.github.htw.mod.gameobject.GameObject;
import com.github.htw.mod.gameobject.Healthbar;
import com.github.htw.mod.gameobject.ItemInvulnerability;
import com.github.htw.mod.gameobject.ItemType;
import com.github.htw.mod.gameobject.Player;
import com.github.htw.mod.map.GameMap;
import com.github.htw.mod.networking.Client;
import com.github.htw.mod.networking.Server;
import com.github.htw.mod.networking.message.MessageTag;
import com.github.htw.mod.point.MapPosition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GameScreen implements Screen {

    private final MasterOfDisaster screenManager;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Stage stage;

    private GameMap map;
    private Player player;
    private ArrayList<GameObject> items;
    private ArrayList<Bullet> playerBullets;
    private ArrayList<Bullet> enemyBullets;
    private ArrayList<GameObject> enemies;

    private Healthbar hp;

    private float playerSpeed = 5.0f;
    private float scale = 6.0f;

    private TextureAtlas atlas;
    private Skin skin;
    private BitmapFont font;

    private Label labelScore;
    private Label labelRound;
    private int score;
    private int round;

    private String collidedItemName;
    private String collidedEnemyName;
    
    private boolean isMouse;
    private int percentX = 0, percentY = 0;
    
    private boolean host;
    private Server server;
    private Client client;
    private boolean respawn = false;
    
    private boolean itemMode = false;

    public GameScreen(MasterOfDisaster screenManager) {
        this.screenManager = screenManager;
        create();
    }
    
    public GameScreen(MasterOfDisaster screenManager, boolean host,Client client,Server server) {
        this.screenManager = screenManager;
        this.host = host;
        this.client = client;
        this.server = server;
        create();
    }
    
    private void create(){
        batch = new SpriteBatch();

        camera = new OrthographicCamera();
        camera.setToOrtho(false,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        camera.update();

        collidedEnemyName = "";
        collidedItemName = "";

        isMouse = true;
        
        //** GAME ** -START
        map = new GameMap("map.tmx",scale);
        enemies = new ArrayList<GameObject>();
        playerBullets = new ArrayList<Bullet>();
        enemyBullets = new ArrayList<Bullet>();
        //** GAME ** -END
        
        //** SERVER ** - START
        if(host){
        	try {
	        	// Set position of all Player and Items
	        	for(int i = 0; i < server.getThreadSize(); i++){
	        		server.getPlayerAndPosition().put(
	        				i+"", 
	        				new MapPosition(
	        						map.getSpawnMap().getSpawnPoint(i).getX(),
	        						map.getSpawnMap().getSpawnPoint(i).getY()
	        						)
	        				);
	        		server.getPlayerAndHealth().put(i+"", 100);
	        	}
	        	
	        	server.updateClients(MessageTag.PLAYERPOSITION);
	        	// Wait until the Clients are updated
				Thread.sleep(1000);
			
	        	server.updateClients(MessageTag.PLAYERHEALTH);
				Thread.sleep(100);
	        	
	        	for(int i = 0; i < 10; i++){
	                int position = new Random().nextInt(map.getFloorMap().getSize());
	                server.getItemAndPosition().put(
	                		i+":"+ItemType.GODMODE, 
	                		new MapPosition(
	                				map.getFloorMap().getFloorPoint(position).getX(), 
	                				map.getFloorMap().getFloorPoint(position).getY()
	                				)
	                		);
	            }
	        	
	        	server.updateClients(MessageTag.ITEMPOSITION);
	        	Thread.sleep(100);
        	} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	// Set on Server every position of a Player
        	spawn();
        }else{
        	try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	// Set on Client every position of a Player
        	spawn();
        }
        //** SERVER ** - END

        //** GUI ** - START
        hp= new Healthbar();
        stage = new Stage(new ScreenViewport(), batch);

        // Button for shooting and Score
        atlas = new TextureAtlas("button/button.pack");
        skin = new Skin(atlas);

        font = new BitmapFont();
        font.getData().setScale(1.0f);

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.getDrawable("buttonOff");
        textButtonStyle.down = skin.getDrawable("buttonOn");
        textButtonStyle.font = font;
        textButtonStyle.fontColor = Color.WHITE;

        score = 0;
        Label.LabelStyle labelStyle = new Label.LabelStyle( font, Color.WHITE);
        labelScore = new Label("Score: "+score, labelStyle);
        labelScore.setWidth(100);
        labelScore.setPosition(Gdx.graphics.getWidth()- labelScore.getWidth()-40, Gdx.graphics.getHeight() - labelScore.getHeight()-20);

        round = 0;
        labelRound = new Label("Round: "+round, labelStyle);
        labelRound.setWidth(100);
        labelRound.setPosition(40, Gdx.graphics.getHeight() - labelScore.getHeight()-20);

        stage.addActor(labelRound);
        stage.addActor(labelScore);
        stage.addActor(hp.getBar());
        Gdx.input.setInputProcessor(stage);
        //** GUI ** - END
    }

    @Override
    public void show() {
    }
    int test=0;
    @Override
    public void render(float delta) {
    	
    	// Respawn
        respawn();
    	
        Gdx.gl.glClearColor(0.100f, 0.314f, 0.314f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        if(Gdx.input.isKeyPressed(Input.Keys.W)){
        	percentX = 0;
        	percentY = 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)){
        	percentX = 0;
        	percentY = -1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A)){
        	percentX = -1;
        	percentY = 0;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D)){
        	percentX = 1;
        	percentY = 0;
        }
        
        
        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT) && player.getHealthPoints() > 0 && respawn == false){	
        	if(isMouse){
	        	playerBullets.add(
	                    new Bullet(
	                        "data/bullet.png",
	                        player.getX()+64-16,
	                        player.getY()+64-16,
	                        player.getName()+":"+playerBullets.size()
	                        )
	            );
	        	
	        	float pathX = Gdx.input.getX() - ((Gdx.graphics.getWidth()/2));
	        	float pathY = Gdx.input.getY() - ((Gdx.graphics.getHeight()/2));
	        	
	        	float distance = (float) Math.sqrt(pathX*pathX+pathY*pathY);
	        	
	        	float directionX = pathX / distance;
	        	float directionY = pathY / distance;
	        	
	        	client.getClientSendHandler().updateClients(
	        			MessageTag.PLAYERBULLETEXIST, 
	        			playerBullets.get(playerBullets.size()-1).getName()+"!"+"true"
	        			);
	        	// Waiting until message is send
	        	try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	client.getClientSendHandler().updateClients(
	        			MessageTag.PLAYERBULLETPOSITION, 
	        			playerBullets.get(playerBullets.size()-1).getName()+"!"+
	        			playerBullets.get(playerBullets.size()-1).getX()+":"+
	        			playerBullets.get(playerBullets.size()-1).getY()
	        			);
	        	// Waiting until message is send
	        	try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	
        		playerBullets.get(playerBullets.size()-1).setDirectionX(directionX);
        		playerBullets.get(playerBullets.size()-1).setDirectionY(-directionY);
	        	
	            isMouse = false;
        	}
        }else{
        	isMouse = true;
        }
        
        if(player != null){
        	if(player.getHealthPoints() > 0){
        		player.collideWithMap(map.getCollisionMap());
    	        player.move(percentX * playerSpeed, percentY * playerSpeed);
        	} else {
        		player.setRender(false);
        	}
	        
	        percentX = 0;
	        percentY = 0;
	
	        camera.position.x = player.getX()+64;
	        camera.position.y = player.getY()+64;
	        
	        collidedItemName = player.collideWithObject(items);
	        
	        // Update Position
	        client.getClientSendHandler().updateClients(MessageTag.PLAYERPOSITION, new MapPosition(player.getX()+64, player.getY()+64));
	        
	        // Update Health
	        int health = client.getPlayerAndHealth().get(player.getName());
	        //Gdx.app.log("DEBUG", health+"");
	        if(player.getHealthPoints() != health){
	        	player.setHealthPoints(health);
	        	hp.changeHP(health);
	        }
        }

        map.render(camera);

        // Items
        for(int i = 0; i < items.size(); i++){
            if(collidedItemName.equals(items.get(i).getName())) {
                //Gdx.app.log("DEBUG",items.get(i).getName() + " touched");
            	if(player != null){
            		player.giveItemBehaviour(items, items.get(i));
            		
            		client.getClientSendHandler().updateClients(MessageTag.ITEMTAKEN, items.get(i).getName());
            		// Waiting until message is send
            		try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		if(player.isGodMode()){
            			itemMode = true;
            			hp.setStyle(Color.LIGHT_GRAY);
            			client.getClientSendHandler().updateClients(MessageTag.PLAYERGODMODE, true);
            		}
            	}
                items.remove(i);
            }else{
            	
            	if(!player.isGodMode() && itemMode){
            		hp.setStyle(Color.RED);
            		client.getClientSendHandler().updateClients(MessageTag.PLAYERGODMODE, false);
            		itemMode = false;
            	}
            	
            	// The player needs to remove the item a enemy has taken
            	boolean taken = false;
            	if(client.getItemAndTaken().containsKey(items.get(i).getName())){
            		taken = client.getItemAndTaken().get(items.get(i).getName());
            		Gdx.app.log("DEBUG", "taken: " + items.get(i).getName());
            	}
            		
            	if(taken){
            		items.remove(i);
            	}else{
	                items.get(i).render(batch, camera);
	                items.get(i).setRender(true);
            	}
            }
        }
        
        // Enemy Bullets
        
        HashMap<String,MapPosition> bulletPositionHashMap = client.getBulletAndPosition();
        HashMap<String,Boolean> bulletExistHashMap = client.getBulletAndExist();
    	for (Map.Entry<String, MapPosition> entry : bulletPositionHashMap.entrySet()) {
    	    String key = entry.getKey();
    	    MapPosition value = entry.getValue();
    	    
    	    // Check if the key is in playerBullets
    	    boolean containsKey = false;
    	    loopContainsKey:
    	    for(int i = 0; i < playerBullets.size(); i++){
    	    	if(playerBullets.get(i).getName().equals(key)){
    	    		containsKey = true;
    	    		break loopContainsKey;
    	    	}
    	    }
    	    
    	    if(!containsKey){
    	    	// Check if the bullet of the enemy exist
    	    	if(bulletExistHashMap.containsKey(key))
    	    	{
	    	    	if(bulletExistHashMap.get(key) == true){
	    	    		// Check if bullet exists in the array
	    	    		boolean containsBullet = false;
	    	    		loopContainsBullet:
	    	    		for(int i = 0; i < enemyBullets.size(); i++){
	    	    			if(enemyBullets.get(i).getName().equals(key)){
	    	    				containsBullet = true;
	    	    				break loopContainsBullet;
	    	    			}
	    	    		}
	    	    		
	    	    		// If not create it
	    	    		if(!containsBullet){
	    	    			enemyBullets.add(
	    	    					new Bullet(
	    	    	                        "data/bullet.png",
	    	    	                        value.getX()+64-16,
	    	    	                        value.getY()+64-16,
	    	    	                        key
	    	    	                        )
	    	    					);
	    	    		}
	    	    	}
	    	    	
	    	    	if(bulletExistHashMap.get(key) == false){
	    	    		loopRemoveBullet:
	    	    		for(int i = 0; i < enemyBullets.size(); i++){
	    	    			if(enemyBullets.get(i).getName().equals(key)){
	    	    				enemyBullets.remove(i);
	    	    				break loopRemoveBullet;
	    	    			}
	    	    		}
	    	    	}
    	    	}
    	    }
    	}
        
    	for(int i = 0; i < enemyBullets.size(); i++){
    		enemyBullets.get(i).setX(bulletPositionHashMap.get(enemyBullets.get(i).getName()).getX());
    		enemyBullets.get(i).setY(bulletPositionHashMap.get(enemyBullets.get(i).getName()).getY());
    		enemyBullets.get(i).render(batch, camera);
			enemyBullets.get(i).setRender(true);
    	}

        // Enemies
        if(enemies.size() == 0){
            //respawn();
            collidedEnemyName = "";
            collidedItemName = "";
        } else {
        	 for(int i = 0; i < enemies.size(); i++){
                 if(collidedEnemyName.equals(enemies.get(i).getName())){
                	 // Get health of player and only delete him if his health is 0
                	 int health = (Integer)client.getPlayerAndHealth().get(enemies.get(i).getName());
                	 // Check if the enemy has godMode
                	 boolean godMode = false;
                	 if(client.getPlayerAndGodMode().containsKey(enemies.get(i).getName())){
                		 godMode = client.getPlayerAndGodMode().get(enemies.get(i).getName());
                	 }
                	 
                	 if(!godMode){
                		 health = health-25;
                	 } 
                	 
            		 client.getClientSendHandler().updateClients(MessageTag.PLAYERHEALTH, enemies.get(i).getName()+":"+health);
            		 try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		 
            		 //Gdx.app.log("DEBUG", enemies.get(i).getName()+":"+health);
            		 enemies.get(i).render(batch, camera);
                     enemies.get(i).setRender(true);
                     
                     if(health <= 0){
                    	 player.setScore(player.getScore()+10);
                    	 score = player.getScore();
                		 enemies.remove(i);
                	 }else{
                		 collidedEnemyName = "";
                	 }
                 }else {
                	 // Update Enemy Status
                	 int health = client.getPlayerAndHealth().get(enemies.get(i).getName());
                	 
                	 // The player needs to remove the enemy that is killed by another enemy
                	 if(health <= 0){
                		 enemies.remove(i);
                	 }else{
	                	 enemies.get(i).render(batch, camera);
	                     enemies.get(i).setRender(true);
	                     
	                     if(client.getPlayerAndPosition() != null && player != null){
	    	                 // Update Position of other Players
	    	                 MapPosition mapposition = (MapPosition) client.getPlayerAndPosition().get(enemies.get(i).getName());
	    	                 
	    	                 if (mapposition != null) {
	    	                	 //Gdx.app.log("DEBUG", mapposition.getX() + ":" + mapposition.getY());
	    	                	 enemies.get(i).setX(mapposition.getX()-64);
	    		                 enemies.get(i).setY(mapposition.getY()-64);
	    	                 }
	                     }
                	 }
                 }
             }
        }

    	// Player Bullets
    	
        for(int i = 0; i < playerBullets.size(); i++){
            playerBullets.get(i).collideWithMap(map.getCollisionMap());

            collidedEnemyName = playerBullets.get(i).collideWithObject(enemies);
            //Gdx.app.log("DEBUG","Enemy: " + collidedEnemyName);
            playerBullets.get(i).setEnemyName(collidedEnemyName);

            if(!playerBullets.get(i).getEnemyName().equals("")){
            	
            	client.getClientSendHandler().updateClients(
	        			MessageTag.PLAYERBULLETEXIST, 
	        			playerBullets.get(i).getName()+"!"+"false"
	        			);
	        	// Waiting until message is send
	        	try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
                playerBullets.remove(i);
            } else if(playerBullets.get(i).isCollision()){
            	
            	client.getClientSendHandler().updateClients(
	        			MessageTag.PLAYERBULLETEXIST, 
	        			playerBullets.get(i).getName()+"!"+"false"
	        			);
	        	// Waiting until message is send
	        	try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
                playerBullets.remove(i);
            } else {
            	
                playerBullets.get(i).setX(playerBullets.get(i).getX() + playerBullets.get(i).getDirectionX() * 20);
                playerBullets.get(i).setY(playerBullets.get(i).getY() + playerBullets.get(i).getDirectionY() * 20);
                
                client.getClientSendHandler().updateClients(
	        			MessageTag.PLAYERBULLETPOSITION, 
	        			playerBullets.get(i).getName()+"!"+
	        			playerBullets.get(i).getX()+":"+
	        			playerBullets.get(i).getY()
	        			);
                
                playerBullets.get(i).render(batch, camera);
                playerBullets.get(i).setRender(true);
            }
        }

        // Labels
        labelScore.setText("Score: "+score);
        labelRound.setText("Round: "+round);

        if(player != null) {
            player.render(batch, camera);
            player.showBounds(false, camera);
        }

        // Draw stage for touchpad
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    private void spawn(){
    	for(int i = 0; i < client.getPlayerAndPosition().size(); i++){
    		if(client.getId() == i){
	        	player = new Player(
	                    "data/playerExample.png",
	                    ((MapPosition) client.getPlayerAndPosition().get(i+"")).getX()-64,
	                    ((MapPosition) client.getPlayerAndPosition().get(i+"")).getY()-64,
	                    i+""
	            );
	        	// Render player
	        	client.getClientSendHandler().updateClients(MessageTag.PLAYERGODMODE, false);
	        	try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	player.setRender(true);
	        	
	        	// Camera set to player position
	            camera.position.x = player.getX()+64;
	            camera.position.y = player.getY()+64;
    		} else {
                enemies.add(
                        new GameObject(
                                "data/playerExample.png",
                                ((MapPosition) client.getPlayerAndPosition().get(i+"")).getX()-64,
    		                    ((MapPosition) client.getPlayerAndPosition().get(i+"")).getY()-64,
    		                    i+""
                        )
                );
                client.getClientSendHandler().updateClients(MessageTag.PLAYERHEALTH, enemies.get(enemies.size()-1).getName()+":"+100);
                try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    	
    	items = new ArrayList<GameObject>();
    	HashMap<String,MapPosition> hashmap = client.getItemAndPosition();
    	for (Map.Entry<String, MapPosition> entry : hashmap.entrySet()) {
    	    String key = entry.getKey();
    	    MapPosition value = entry.getValue();
    	    
    	    String[] stringArray = key.split(":");
    	    
    	    if(stringArray[1].equals(ItemType.GODMODE)){
    	    	items.add(new ItemInvulnerability(
                        "data/item.png",
                        value.getX()-64,
                        value.getY()-64,
                        stringArray[0]+":"+stringArray[1]
                ));
    	    }
    	    
    	    // If another type of cheat is needed then add it hear with an if clause
    	}
    }
    
    
    private void respawn(){
        
        int alive = 0;
        for (Map.Entry<String, Integer> entry : client.getPlayerAndHealth().entrySet()) {
    	    String key = entry.getKey();
    	    int value = entry.getValue();
    	    
    	    if(value > 0){
    	    	alive++;
    	    }
        }
        
        if(alive == 1 && respawn == false){
        	respawn = true;
        }
        
        if(respawn){
        	round++;
        	
        	if(host){
        		server.getItemAndTaken().clear();
    			server.getPlayerAndGodMode().clear();
    			server.getPlayerAndHealth().clear();
    			
    			try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    			
        		for(int i = 0; i < server.getThreadSize(); i++){
        			
	        			server.getPlayerAndPosition().put(
		        				i+"", 
		        				new MapPosition(
		        						map.getSpawnMap().getSpawnPoint(i).getX(),
		        						map.getSpawnMap().getSpawnPoint(i).getY()
		        						)
		        				);
	        			
	        			server.getPlayerAndHealth().put(i+"", 100);
        		}
        		
        		try{
        			server.updateClients(MessageTag.PLAYERPOSITION);
        			// Wait until the Clients are updated
        			Thread.sleep(1000);
			
        			server.updateClients(MessageTag.PLAYERHEALTH);
        			Thread.sleep(1000);
				
        			server.updateClients(MessageTag.ITEMTAKEN);
        			Thread.sleep(1000);
        		} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        		
        		spawn();
        	}else {
        		try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		spawn();
        	}
        	respawn = false;
        }
        
    }

}