package com.github.htw.mom.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.htw.mom.gameobject.Bullet;
import com.github.htw.mom.gameobject.GameObject;
import com.github.htw.mom.gameobject.Healthbar;
import com.github.htw.mom.gameobject.ItemInvulnerability;
import com.github.htw.mom.gameobject.Player;
import com.github.htw.mom.map.GameMap;

import java.util.ArrayList;
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

    public GameScreen(MasterOfDisaster screenManager) {
        this.screenManager = screenManager;
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

        player = new Player(
                "data/playerExample.png",
                map.getSpawnMap().getSpawnPoint(0).getX()-64,
                map.getSpawnMap().getSpawnPoint(0).getY()-64,
                "Player0"
        );

        player.setRender(true);
        hp= new Healthbar();
        enemies = new ArrayList<GameObject>();
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

        // Camera set to player position
        camera.position.x = player.getX();
        camera.position.y = player.getY();
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
        labelScore.setWidth(400);
        labelScore.setPosition(Gdx.graphics.getWidth()- labelScore.getWidth()-40, Gdx.graphics.getHeight() - labelScore.getHeight()-20);

        round = 0;
        labelRound = new Label("Round: "+round, labelStyle);
        labelRound.setWidth(400);
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
        	player.move(0 * playerSpeed, 1 * playerSpeed);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)){
        	player.move(0 * playerSpeed, -1 * playerSpeed);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A)){
        	player.move(-1 * playerSpeed, 0 * playerSpeed);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D)){
        	player.move(1 * playerSpeed, 0 * playerSpeed);
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
	        	
	        	/*
	        	Vector3 vec = new Vector3(Gdx.input.getX(),Gdx.input.getY(),0);
	        	camera.unproject(vec);
	        	*/
	        	
	        	float pathX = Gdx.input.getX() - ((Gdx.graphics.getWidth()/2)+64);
	        	float pathY = Gdx.input.getY() - ((Gdx.graphics.getHeight()/2)-64);
	        	
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
        
        player.collideWithMap(map.getCollisionMap());

        camera.position.x = player.getX();
        camera.position.y = player.getY();

        map.render(camera);

        // Items
        collidedItemName = player.collideWithObject(items);
        for(int i = 0; i < items.size(); i++){
            if(collidedItemName.equals(items.get(i).getName())) {
                //Gdx.app.log("DEBUG",items.get(i).getName() + " touched");
                player.giveItemBehaviour(items, items.get(i));
                items.remove(i);
            }else{
                items.get(i).render(batch, camera);
                items.get(i).setRender(true);
            }
        }

        // Enemies
        for(int i = 0; i < enemies.size(); i++){
            if(collidedEnemyName.equals(enemies.get(i).getName())){
                enemies.remove(i);
            }else {
                enemies.get(i).render(batch, camera);
                enemies.get(i).setRender(true);
            }
        }

        if(enemies.size() == 0){
            respawn();
            collidedEnemyName = "";
            collidedItemName = "";
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


}
