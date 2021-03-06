package com.github.htw.mod.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.github.htw.mod.gameobject.Player;


/**
 * Created by Philip on 05.05.2017.
 */

public class ScoreBoard {

    private final MasterOfDisaster screenManager;
    private BitmapFont font;
    private GameScreen gameScreen;

    private Label labelScore, labelPlayer, labelPlayerName, labelPlayerScore;


    public ScoreBoard(Stage stage, MasterOfDisaster sm, final GameScreen gameScreen, int score, Player player){
        this.screenManager = sm;

        this.gameScreen=gameScreen;

        final Preferences settings = Gdx.app.getPreferences("ProjectY_settings");

        font = new BitmapFont();
        font.getData().setScale(5.0f);

        Label.LabelStyle labelStyle = new Label.LabelStyle( font, Color.WHITE);
        labelPlayer = new Label("Player: ", labelStyle);
        labelPlayer.setWidth(400);
        labelPlayer.setPosition((Gdx.graphics.getWidth() / 2)- labelPlayer.getWidth()+50, Gdx.graphics.getHeight() - labelPlayer.getHeight()-200);

        labelScore = new Label("Score: ", labelStyle);
        labelScore.setWidth(400);
        labelScore.setPosition((Gdx.graphics.getWidth() / 2)- labelScore.getWidth() +500, Gdx.graphics.getHeight() - labelScore.getHeight()-200);

        labelPlayerName = new Label(player.getName(), labelStyle);
        labelPlayerName.setWidth(400);
        labelPlayerName.setPosition((Gdx.graphics.getWidth() / 2)- labelPlayerName.getWidth() +50, Gdx.graphics.getHeight() - labelScore.getHeight()-300);

        labelPlayerScore = new Label(""+score, labelStyle);
        labelPlayerScore.setWidth(400);
        labelPlayerScore.setPosition((Gdx.graphics.getWidth() / 2)- labelPlayerScore.getWidth() +500, Gdx.graphics.getHeight() - labelScore.getHeight()-300);

        stage.addActor(labelPlayer);
        stage.addActor(labelScore);
        stage.addActor(labelPlayerName);
        stage.addActor(labelPlayerScore);

    }

    public Label[] getLabelPlayerLabelScore() {
        Label[] arr = new Label[4];
        arr[0] = labelPlayer;
        arr[1] = labelScore;
        arr[2] = labelPlayerName;
        arr[3] = labelPlayerScore;
        return arr;
    }
}
