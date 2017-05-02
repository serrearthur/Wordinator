package com.eisti.android.project;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.lang.Thread;
import java.util.Random;

public class Wordinator extends ApplicationAdapter {
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private ShapeRenderer renderer;
	private BitmapFont mainFont, letterFont, scoreFont, menuFont;
    private Grille gameGrid;
    private boolean gameOver;
    private TextureAtlas atlas;
    private Skin skin;
    private TextButton buttonPlay, buttonWordinate, buttonReset;
    private Stage stage;
    private Table table;
    private Thread mainGame;
    private Color bgColor;
    private int currentTime, oldBgColor, highscore;
    private Music music;
    private Sound popSound, winSound, loseSound, correctSound, wrongSound;
    private Dico dico;

    int sizeX=5;
    int sizeY=6;
    int cellSize=80;
    int offset=8;
    int maxTime=120;

	@Override
	public void create () {
        dico=new Dico();
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(true,480,800);
		renderer=new ShapeRenderer();
        gameGrid=null;
        mainGame=new Thread();
        gameOver=true;
        oldBgColor=-1;

        letterFont=new BitmapFont(Gdx.files.internal("fonts/gridfont.fnt"), true);
        letterFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);

        mainFont=new BitmapFont(Gdx.files.internal("fonts/mainfont.fnt"),true);
        mainFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);

        menuFont=new BitmapFont(Gdx.files.internal("fonts/menufont.fnt"),true);
        menuFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);

        scoreFont=new BitmapFont(true);
        scoreFont.setColor(0.3f,0.3f,0.3f,1);
        scoreFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);

        popSound = Gdx.audio.newSound(Gdx.files.internal("sounds/bubble.mp3"));
        winSound = Gdx.audio.newSound(Gdx.files.internal("sounds/highscore.mp3"));
        loseSound = Gdx.audio.newSound(Gdx.files.internal("sounds/gameover.mp3"));
        correctSound= Gdx.audio.newSound(Gdx.files.internal("sounds/correct.mp3"));
        wrongSound= Gdx.audio.newSound(Gdx.files.internal("sounds/wrong.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("sounds/background_music.mp3"));
        music.setLooping(true);
        music.play();

        stage=new Stage();
        Gdx.input.setInputProcessor(stage);
        atlas = new TextureAtlas("ui/button.pack");
        skin=new Skin(atlas);
        table=new Table(skin);
        table.setBounds(40,0,Gdx.graphics.getWidth()-80, Gdx.graphics.getHeight());
        table.top().right();

        BitmapFont buttonFont=new BitmapFont(Gdx.files.internal("fonts/buttonfont.fnt"), false);
        TextButton.TextButtonStyle textButtonStyle=new TextButton.TextButtonStyle();
        textButtonStyle.up=skin.getDrawable("button.up");
        textButtonStyle.down=skin.getDrawable("button.down");
        textButtonStyle.pressedOffsetX=1;
        textButtonStyle.pressedOffsetY=-1;
        textButtonStyle.font=buttonFont;

        buttonReset=new TextButton("Effacer", textButtonStyle);
        buttonReset.pad(20);
        buttonReset.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                popSound.play();
                FileHandle handle = Gdx.files.local("highscore.txt");
                handle.writeString("0",false);
                highscore=0;
            }
        });

        buttonPlay=new TextButton("Nouvelle Partie", textButtonStyle);
        buttonPlay.pad(20);
        buttonPlay.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                popSound.play();
                mainGame.interrupt();
                mainGame=null;
                mainGame=NewGame();
            }
        });

        buttonWordinate=new TextButton("Wordinater", textButtonStyle);
        buttonWordinate.pad(20);
        buttonWordinate.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                popSound.play();
                bgColor=GetColor();
                if (!gameOver) {
                    int score = gameGrid.gridScore;
                    gameGrid=new Grille(sizeX,sizeY,cellSize,new Vector2((480-(sizeX*cellSize+(sizeX-1)*offset))/2,200),offset);
                    gameGrid.gridScore=Math.max(score-15,0);
                }
            }
        });
        table.add().pad(buttonReset.getHeight());
        table.row();
        table.add().expandX();
        table.add().expandX();
        table.add(buttonReset).right();
        table.row();
        table.add().expandY();
        table.row();
        table.add(buttonWordinate).padBottom(30).left();
        table.add().expandX();
        table.add(buttonPlay).padBottom(30);
        stage.addActor(table);

        FileHandle handle = Gdx.files.local("highscore.txt");
        if (!handle.exists()) {
            handle.writeString("0",false);
        }
        try {
            highscore=Integer.parseInt(handle.readString());
        } catch (NumberFormatException nfe) {
            Gdx.app.log("my app", nfe.getMessage());
        }

        InputProcessor gridProcessor = new InputAdapter() {
            @Override
            public boolean touchDown(int x, int y,int pointer, int button) {
                if (!gameOver) {
                    if (pointer ==0) {
                        Vector3 v=camera.unproject(new Vector3(x,y,0));
                        Vector2 pos=gameGrid.IsInside(v.x, v.y);
                        if (pos !=null) {
                            popSound.play();
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean touchDragged(int x, int y, int pointer){
                if (!gameOver) {
                    if (pointer ==0) {
                        Vector3 v=camera.unproject(new Vector3(x,y,0));
                        Vector2 pos=gameGrid.IsInside(v.x, v.y);
                        if (pos !=null) {
                            gameGrid.addTouched(pos);
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean touchUp (int x, int y, int pointer, int button) {
                if (!gameOver) {
                    String touchedWord="";
                    for (Cellule c : gameGrid.touched) {
                        touchedWord+=c.value.toLowerCase();
                    }
                    boolean valid=!gameGrid.found.contains(touchedWord) && dico.dictionnary.contains(touchedWord);
                    for (Cellule c : gameGrid.touched) {
                        if (valid) {
                            gameGrid.found.add(touchedWord);
                            c.cellColor=gameGrid.green;
                            correctSound.play();
                        }
                        else {
                            c.cellColor=gameGrid.red;
                            wrongSound.play();
                        }

                    }
                    if (valid)
                        gameGrid.gridScore+=gameGrid.ComputeScore();
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                sleep(200);
                                for (Cellule c : gameGrid.touched) {
                                    c.cellColor=gameGrid.gray;
                                }
                                gameGrid.touched.clear();
                            } catch (InterruptedException e) {

                                Gdx.app.log("Main", e.getMessage());
                            }
                        }
                    };
                    thread.start();
                }
                return true; // return true to indicate the event was handled
            }
        };
        bgColor=GetColor();

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(gridProcessor);
        Gdx.input.setInputProcessor(inputMultiplexer);
        Gdx.app.setLogLevel(Application.LOG_INFO);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
        batch.setProjectionMatrix(camera.combined);
        renderer.setProjectionMatrix(camera.combined);

		renderer.begin(ShapeRenderer.ShapeType.Filled);
        //current letter box
        renderer.setColor(0,0,0,1);
		renderer.rect(20,110,440,80);

        //letter background
        renderer.setColor(0,0,0,0.7f);
        renderer.rect(20,195, 440,sizeY*cellSize+(sizeY-1)*offset+10);

        if (gameGrid!=null) {
            //cell
            for (int i=0; i< sizeX; i++) {
                for (int j=0; j<sizeY;j++) {
                    renderer.setColor(gameGrid.grille[i][j].cellColor);
                    renderer.rect(gameGrid.grille[i][j].root.x,gameGrid.grille[i][j].root.y,cellSize,cellSize);
                }
            }

            //lines
            Cellule prev=null;
            for (Cellule c : gameGrid.touched) {
                if (prev != null) {
                    renderer.setColor(c.cellColor);
                    renderer.rectLine(c.root.x+cellSize/2.0f,c.root.y+cellSize/2.0f,prev.root.x+cellSize/2.0f,prev.root.y+cellSize/2.0f,5);
                }
                prev=c;
            }
        }
		renderer.end();

		batch.begin();
        if (gameGrid!=null) {
            //cell letters
            for (int i=0; i< sizeX; i++) {
                for (int j=0; j<sizeY;j++) {
                    letterFont.draw(batch, gameGrid.grille[i][j].value,gameGrid.grille[i][j].letterRoot.x,gameGrid.grille[i][j].letterRoot.y);
                    scoreFont.draw(batch, String.valueOf(gameGrid.grille[i][j].score),gameGrid.grille[i][j].scoreRoot.x,gameGrid.grille[i][j].scoreRoot.y);
                }
            }

            //found letters
            String foundLetters="";
            for(Cellule c : gameGrid.touched) {
                foundLetters+=c.value;
            }
            letterFont.draw(batch,foundLetters,Math.max(280-(foundLetters.length()-1)*30,30), 130);

            //UI
            if (!gameOver) {
                mainFont.draw(batch,"Score: "+String.valueOf(gameGrid.gridScore),20, 10);
                mainFont.draw(batch,"Temps: "+String.valueOf(currentTime),320, 10);
            }
            else {
                mainFont.draw(batch, "Game Over! Score final: "+String.valueOf(gameGrid.gridScore),80,10);
            }
        }
        else {
            int posX=50;
            int posY=250;
            int space=45;
            int off=0;
            menuFont.setColor(new Color(97/255f,189/255f,79/255f,1));
            menuFont.draw(batch,"W",posX+off, posY);
            off+=space;
            menuFont.setColor(new Color(242/255f, 214/255f,0,1));
            menuFont.draw(batch,"o",posX+off, posY);
            off+=space;
            menuFont.setColor(new Color(1,171/255f,74/255f,1));
            menuFont.draw(batch,"r",posX+off, posY);
            off+=space-7;
            menuFont.setColor(new Color(235/255f, 90/255f,70/255f,1));
            menuFont.draw(batch,"d",posX+off, posY);
            off+=space-10;
            menuFont.setColor(new Color(195/255f,119/255f,224/255f,1));
            menuFont.draw(batch,"i",posX+off, posY);
            off+=space/2;
            menuFont.setColor(new Color(0,121/255f,191/255f,1));
            menuFont.draw(batch,"n",posX+off, posY);
            off+=space-5;
            menuFont.setColor(new Color(0,194/255f,224/255f,1));
            menuFont.draw(batch,"a",posX+off, posY);
            off+=space-7;
            menuFont.setColor(new Color(81/255f,232/255f,152/255f,1));
            menuFont.draw(batch,"t",posX+off, posY);
            off+=space-10;
            menuFont.setColor(new Color(1,128/255f,206/255f,1));
            menuFont.draw(batch,"o",posX+off, posY);
            off+=space;
            menuFont.setColor(new Color(77/255f,77/255f,77/255f,1));
            menuFont.draw(batch,"r",posX+off, posY);

            posX=40;
            posY+=80;
            off=60;
            mainFont.draw(batch, "Reliez les lettres pour", posX, posY);
            posY+=off/2;
            mainFont.draw(batch, "former des mots", posX, posY);;
            posY+=off;
            mainFont.draw(batch, "Appuyez sur \"Nouvelle Partie\"", posX, posY);
            posY+=off/2;
            mainFont.draw(batch, "pour commencer", posX, posY);
            posY+=off;
            mainFont.draw(batch, "En jeu, appuyez sur \"Wordinater\"", posX, posY);
            posY+=off/2;
            mainFont.draw(batch, "pour wordinater votre grille", posX, posY);
            posY+=off/2;
            mainFont.draw(batch, "et en générer une nouvelle", posX, posY);
            posY+=off;
            mainFont.draw(batch, "Attention, chaque wordination", posX, posY);
            posY+=off/2;
            mainFont.draw(batch, "coute 15pts!", posX, posY);
        }
        //highscore
        mainFont.draw(batch,"Record: "+String.valueOf(highscore),20, 60);

		batch.end();
        stage.act();
        stage.draw();
	}

	@Override
	public void dispose () {
		batch.dispose();
		renderer.dispose();
        mainFont.dispose();
        scoreFont.dispose();
        atlas.dispose();
        skin.dispose();
        stage.dispose();
        music.dispose();
	}

    Thread NewGame() {
        gameOver=false;
        gameGrid=new Grille(sizeX,sizeY,cellSize,new Vector2((480-(sizeX*cellSize+(sizeX-1)*offset))/2,200),offset);
        currentTime=maxTime;
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(300);
                    while (currentTime>0) {
                        sleep(1000);
                        currentTime--;
                    }
                    LostGame();
                } catch (InterruptedException e) {

                }
            }
        };
        thread.start();
        return thread;
    }

    void LostGame() {
        gameOver=true;
        if (gameGrid.gridScore > highscore) {
            winSound.play();
            highscore=gameGrid.gridScore;
            FileHandle handle = Gdx.files.local("highscore.txt");
            handle.writeString(String.valueOf(highscore),false);
        }
        else
            loseSound.play();
    }

    Color GetColor() {
        Color[] arrayColor={
                new Color(97/255f,189/255f,79/255f,1),
                new Color(242/255f, 214/255f,0,1),
                new Color(1,171/255f,74/255f,1),
                new Color(235/255f, 90/255f,70/255f,1),
                new Color(195/255f,119/255f,224/255f,1),
                new Color(0,121/255f,191/255f,1),
                new Color(0,194/255f,224/255f,1),
                new Color(81/255f,232/255f,152/255f,1),
                new Color(1,128/255f,206/255f,1),
                new Color(77/255f,77/255f,77/255f,1)
        };
        Random r = new Random();
        int roll;
        do {
            roll=r.nextInt(10);
        }while(oldBgColor>0 && roll==oldBgColor);
        oldBgColor=roll;
        return arrayColor[roll];
    }
}