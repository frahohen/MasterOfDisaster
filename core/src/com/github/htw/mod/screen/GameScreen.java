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
import com.github.htw.mod.gameobject.Player;
import com.github.htw.mod.map.GameMap;
import com.github.htw.mod.networking.Client;
import com.github.htw.mod.networking.Server;
import com.github.htw.mod.networking.message.MessageTag;
import com.github.htw.mod.point.MapPosition;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

public class GameScreen implements Screen {

    private final MasterOfDisaster screenManager;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Stage stage;

    private GameMap map;
    private Player player;
    private ArrayList<GameObject> items;
    private ArrayList<Bullet> bullets;
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
        
        //** SERVER ** - START
        if(host){
        	// Set position of all Player and Items
        	for(int i = 0; i < server.getThreadSize(); i++){
        		server.getPlayerAndPosition().put(
        				i+"", 
        				new MapPosition(
        						map.getSpawnMap().getSpawnPoint(i).getX(),
        						map.getSpawnMap().getSpawnPoint(i).getY()
        						)
        				);
        	}
        	
        	server.updateClients(MessageTag.POSITION);
        	
        	// Wait until the Clients are updated
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	// Set on Server every position of a Player
        	spawn();
        }else{
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	// Set on Client every position of a Player
        	spawn();
        }
        //** SERVER ** - END
        hp= new Healthbar();
        
        items = new ArrayList<GameObject>();
        for(int i = 0; i < 10; i++){
            int position = new Random().nextInt(map.getFloorMap().getSize());
            items.add(new ItemInvulnerability(
                    "data/item.png",
                    map.getFloorMap().getFloorPoint(position).getX()-64,
                    map.getFloorMap().getFloorPoint(position).getY()-64,
                    "Item"+i
            ));
        }

        bullets = new ArrayList<Bullet>();
        //** GAME ** -END

        //** GUI ** - START
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
        
        
        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){	
        	if(isMouse){
	        	bullets.add(
	                    new Bullet(
	                        "data/bullet.png",
	                        player.getX()+64-16,
	                        player.getY()+64-16,
	                        "Bullet")
	            );
	        	
	        	float pathX = Gdx.input.getX() - ((Gdx.graphics.getWidth()/2));
	        	float pathY = Gdx.input.getY() - ((Gdx.graphics.getHeight()/2));
	        	
	        	float distance = (float) Math.sqrt(pathX*pathX+pathY*pathY);
	        	
	        	float directionX = pathX / distance;
	        	float directionY = pathY / distance;
	        	
        		bullets.get(bullets.size()-1).setDirectionX(directionX);
        		bullets.get(bullets.size()-1).setDirectionY(-directionY);
	        	
	            isMouse = false;
        	}
        }else{
        	isMouse = true;
        }
        
        if(player != null){
	        player.collideWithMap(map.getCollisionMap());
	        player.move(percentX * playerSpeed, percentY * playerSpeed);
	        percentX = 0;
	        percentY = 0;
	
	        camera.position.x = player.getX()+64;
	        camera.position.y = player.getY()+64;
	        
	        collidedItemName = player.collideWithObject(items);
	        
	        // Update Position
	        client.getClientSendHandler().updateClients(MessageTag.POSITION, new MapPosition(player.getX()+64, player.getY()+64));
        }

        map.render(camera);

        // Items
        for(int i = 0; i < items.size(); i++){
            if(collidedItemName.equals(items.get(i).getName())) {
                //Gdx.app.log("DEBUG",items.get(i).getName() + " touched");
            	if(player != null){
            		player.giveItemBehaviour(items, items.get(i));
            	}
                items.remove(i);
            }else{
                items.get(i).render(batch, camera);
                items.get(i).setRender(true);
            }
        }

        // Enemies
        if(enemies.size() == 0){
            //respawn();
            collidedEnemyName = "";
            collidedItemName = "";
        } else {
        	 for(int i = 0; i < enemies.size(); i++){
                 if(collidedEnemyName.equals(enemies.get(i).getName())){
                     enemies.remove(i);
                 }else {
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

        // Bullets
        for(int i = 0; i < bullets.size(); i++){
            bullets.get(i).collideWithMap(map.getCollisionMap());

            collidedEnemyName = bullets.get(i).collideWithObject(enemies);
            //Gdx.app.log("DEBUG","Enemy: " + collidedEnemyName);
            bullets.get(i).setEnemyName(collidedEnemyName);

            if(!bullets.get(i).getEnemyName().equals("")){
                //score+=10;
                score = bullets.get(i).checkScore(score);
                bullets.remove(i);
            } else if(bullets.get(i).isCollision()){
                bullets.remove(i);
            } else {
                bullets.get(i).setX(bullets.get(i).getX() + bullets.get(i).getDirectionX() * 20);
                bullets.get(i).setY(bullets.get(i).getY() + bullets.get(i).getDirectionY() * 20);
                bullets.get(i).render(batch, camera);
                bullets.get(i).setRender(true);
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
    		}
    	}
    }
    
    /*
    private void respawn(){
        round++;
        for(int i = 1; i < map.getSpawnMap().getSize(); i++){
            enemies.add(
                    new GameObject(
                            "data/playerExample.png",
                            map.getSpawnMap().getSpawnPoint(i).getX()-64,
                            map.getSpawnMap().getSpawnPoint(i).getY()-64,
                            "Enemy"+i
                    )
            );
        }

        player = new Player(
                "data/playerExample.png",
                map.getSpawnMap().getSpawnPoint(0).getX()-64,
                map.getSpawnMap().getSpawnPoint(0).getY()-64,
                "Player0"
        );
        player.setRender(true);
    }
    */


}