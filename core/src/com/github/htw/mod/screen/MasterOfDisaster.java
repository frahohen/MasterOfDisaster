package com.github.htw.mod.screen;

import com.badlogic.gdx.Game;

public class MasterOfDisaster extends Game {

	public MasterOfDisaster() {
	}

	@Override
	public void create() {

		this.setScreen(new MainMenuScreen(this));
	}

	@Override
	public void render() {
		super.render(); //important!
	}

	@Override
	public void dispose() {
        super.dispose();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}
}
