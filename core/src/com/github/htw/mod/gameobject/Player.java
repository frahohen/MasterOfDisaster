package com.github.htw.mod.gameobject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Timer;
import com.github.htw.mod.networking.Client;

import java.util.ArrayList;

public class Player extends GameObject {

    private float x;
    private float y;

    private boolean godMode;
    private int healthPoints;
    private int score;

    public Player(String avatar, float x, float y, String name){
        super(avatar,x,y,name);
        init(x,y);
    }

    @Override
    public void render(SpriteBatch batch, OrthographicCamera camera) {
        super.render(batch, camera);
    }

    @Override
    public void showBounds(boolean isBounds, OrthographicCamera camera) {
        super.showBounds(isBounds, camera);
    }

    public void giveItemBehaviour(ArrayList<GameObject> items, GameObject item){
        for(int i = 0; i < items.size(); i++) {
            if (item.equals(items.get(i))){
                if (items.get(i) instanceof ItemInvulnerability) {
                    godMode = true;
                    Gdx.app.log("DEBUG", "Godmode: " + godMode);
                    Timer.schedule(new Timer.Task() {

                        @Override
                        public void run() {
                            godMode = false;
                            Gdx.app.log("DEBUG", "Godmode: " + godMode);
                        }

                    }, 10);
                }
            }
        }
    }

    public void move(float knobPercentX, float knobPercentY){
        if(this.isCollision()){
            //Gdx.app.log("DEBUG", "AllowLeft: " + allowLeft + " AllowRight: " + allowRight + " AllowUp: " + allowUp + " AllowDown: " + allowDown);

        	/*
        	Gdx.app.log("DEBUG", 
        			" Right: " + this.isRight() +
        			" Left: " + this.isLeft() +
        			" Up: " + this.isUp() +
        			" Down: " + this.isDown()
        	);
        	*/
        	
            if(this.isRight()){
                if(knobPercentX < 0){
                    x += knobPercentX;
                }
            }
            if(this.isLeft()){
                if(knobPercentX > 0){
                    x += knobPercentX;
                }
            }
            if(this.isUp()){
                if(knobPercentY < 0){
                    y += knobPercentY;
                }
            }
            if(this.isDown()){
                if(knobPercentY > 0){
                    y += knobPercentY;
                }
            }

            this.setCollision(false);
        }else {
            this.setLeft(false);
            this.setRight(false);
            this.setUp(false);
            this.setDown(false);

            x += knobPercentX;
            y += knobPercentY;
        }

        this.setX(x);
        this.setY(y);
    }
    
    public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public boolean isGodMode() {
		return godMode;
	}

	public void setGodMode(boolean godMode) {
		this.godMode = godMode;
	}

	public int getHealthPoints() {
		return healthPoints;
	}

	public void setHealthPoints(int healthPoints) {
		this.healthPoints = healthPoints;
	}

	private void init(float x, float y){

        this.godMode = false;
        this.x = x;
        this.y = y;
        this.healthPoints = 100;
        this.score = 0;
    }
}
