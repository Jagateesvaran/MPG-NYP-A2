package com.example.mgpa1;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.CookieHandler;
import java.util.Random;

import static androidx.core.content.ContextCompat.startActivity;


public class LevelThreeGame extends SurfaceView implements SurfaceHolder.Callback  {

    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(currentFirebaseUser.getUid()).child("GameOver");



    private Rect r = new Rect();
    // It is a class from the thread
    private LevelThreeThread thread;

    private RectPlayer player;
    private Point playerPoint;

    private ObstacleManager obstacleManager;
    private EnemyManager enemyManager;

    private boolean movingPlayer = false;
    public boolean gameOver = false;
    private long gameOverTime;

    private Background bg;
    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    private CoinManager coin;


    public LevelThreeGame(Context context){
        super(context);
        getHolder().addCallback(this);
        Constants.CURRENT_CONTEXT = context;
        thread = new LevelThreeThread(getHolder(), this);
        player = new RectPlayer(new Rect(100, 100, 200, 200), Color.rgb(255, 0 ,0));
        playerPoint = new Point(Constants.SCREEN_WIDTH / 2 , 3 * Constants.SCREEN_HEIGHT / 4); // where the player spawn on the screen
        player.update(playerPoint);
        obstacleManager = new ObstacleManager(480 , 800 , 100, Color.rgb(150, 75,0));
        enemyManager = new EnemyManager(200 , 900 , 100, Color.GREEN);
        coin = new CoinManager(200 , 1000 , 100, Color.GREEN);
        setFocusable(true);
    }




    // to restart the game when the player die or press the restart button
    public void reset(){
        playerPoint = new Point(Constants.SCREEN_WIDTH/ 2 , 3*Constants.SCREEN_HEIGHT/4); // where the player spawn on the screen
        player.update(playerPoint);
        obstacleManager = new ObstacleManager(550 , 800 , 100, Color.rgb(150, 75,0));
        enemyManager = new EnemyManager(200 , 900 , 100, Color.GREEN);
        coin = new CoinManager(200 , 1000 , 100, Color.GREEN);
        movingPlayer = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.water));
        bg.setVector(-5);

        thread = new LevelThreeThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        boolean retry = true;

        while (retry){

            try {
                thread.setRunning(false);
                thread.join();
            }catch (Exception e) {e.printStackTrace();}
            retry = false;

        }

    }


    // this is where the player can move the player with the touch input
    @Override
    public boolean onTouchEvent(MotionEvent event){

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if ( !gameOver && player.getRectangle().contains((int)event.getX(), (int)event.getY()))
                    movingPlayer = true;
                // if the game is over then if timer is more then timer then set the game back to false;
                if ( gameOver && System.currentTimeMillis() - gameOverTime >= 2000 ){
                    reset();
                    gameOver = false;
                    myRef.child("GameOver").setValue("false");
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if ( !gameOver && movingPlayer )
                    playerPoint.set((int)event.getX(), (int)event.getY());
                break;
            case MotionEvent.ACTION_UP:
                movingPlayer = false;
                break;
        }
        return true;
    }


    // this is where u update the whole game like player , obstaclcer and enemy
    public void update(){
        if ( !gameOver ){
            player.update(playerPoint);
            obstacleManager.update();
            bg.update();
            enemyManager.update();
            coin.update();

            if ( obstacleManager.playerCollide(player)){
                myRef.child("GameOver").setValue("true");
                gameOver = true;
                gameOverTime = System.currentTimeMillis();
            }


            if ( enemyManager.playerCollide(player)){
                myRef.child("GameOver").setValue("true");
                gameOver = true;
            }

            if (coin.playerCollide(player)){
                gameOver = false;
            }



        }
    }


    // used to draw the player , obstacles onto the game screen
    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);

        final float scaleFactorX = getWidth()/WIDTH;
        final float scaleFactorY = getHeight()/HEIGHT;
        final int savedState = canvas.save();
        canvas.scale(scaleFactorY, scaleFactorX);
        bg.draw(canvas);
        canvas.restoreToCount(savedState);
        player.draw(canvas);
        obstacleManager.draw(canvas);
        enemyManager.draw(canvas);
        coin.draw(canvas);

        if ( gameOver ){
            Paint paint = new Paint();
            paint.setTextSize(100);
            paint.setColor(Color.RED);
            drawCenterText(canvas, paint, "Game Over");
        }
    }


    // andreas1724 (white color):
    private void drawCenterText(Canvas canvas, Paint paint, String text) {
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.getClipBounds(r);
        int cHeight = r.height();
        int cWidth = r.width();
        paint.getTextBounds(text, 0, text.length(), r);
        float x = cWidth / 2f - r.width() / 2f - r.left;
        float y = cHeight / 2f + r.height() / 2f - r.bottom;
        canvas.drawText(text, x, y, paint);


    }




}