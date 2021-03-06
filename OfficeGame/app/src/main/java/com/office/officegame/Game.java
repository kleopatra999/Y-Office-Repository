package com.office.officegame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Class that defines game as entity.
 *
 * @author Gavlovych Maksym (reverff@gmail.com)
 * @author Yakubenko Andrii (ayakubenko92@gmail.com)
 *         2014(c)
 */
public class Game extends Activity {

    /**
     * DB Helper
     */
    class DBHelper extends SQLiteOpenHelper {
        private DBHelper(Context context) {
            // super class constructor
            super(context, "OfficeGameDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Creating the highscore table:
            db.execSQL("create table highScore ("
                    + "id integer primary key autoincrement,"
                    + "game_id integer,"
                    + "score integer,"
                    + "games integer,"
                    + "summary integer);");
            db.execSQL("insert into highScore ('game_id','score','games','summary') values (1,0,0,0);");
            db.execSQL("insert into highScore ('game_id','score','games','summary') values (2,0,0,0);");
            db.execSQL("insert into highScore ('game_id','score','games','summary') values (3,0,0,0);");

            db.execSQL("create table signing ("
                    + "signed integer primary key autoincrement);");
            db.execSQL("insert into signing ('signed') values (0);");

            // Creating the modeDescription table:
            db.execSQL("create table modeDescription ("
                    + "id integer primary key autoincrement,"
                    + "mode_name varchar(255),"
                    + "description varchar(1000));");
            db.execSQL("insert into modeDescription ('mode_name','description') values ('ARCADE','Arcade mode. In this game you should hit as much black tiles as you can. But you can miss only 20 times. Every time when you will shout tiles will change faster. Good luck!');");
            db.execSQL("insert into modeDescription ('mode_name','description') values ('TIME SPRINT','Time sprint mode. In this game you should hit as much black tiles as you can. But you can miss only 20 times and you have only 30 seconds. Good luck!');");
            db.execSQL("insert into modeDescription ('mode_name','description') values ('TIME ATTACK','Time attack mode. In this game you should hit as much black tiles as you can. But you have only 5 seconds at begin. Every time when you hit time will increase +1 second, if you will miss then -1 second. Good luck!');");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    private int gameId;                     //the game id
    private String gameMode;                //the game mode
    private String gameRules;               //the game rules
    private Context context;                //the class context
    private int highScore;                  //the game high score

    private static DBHelper dbHelper;
    private static SQLiteDatabase db;
    private static Cursor DatabaseCursor;

    public Game(int gameId, String gameMode, Context context) {
        this.gameId = gameId;
        this.gameMode = gameMode;
        this.context = context;
    }

    public Game(Context context) {
        this.gameId = 0;
        this.gameMode = "";
        this.context = context;
    }

    public MediaPlayer player;

    public void createPlayer() {
        player = MediaPlayer.create(context, R.raw.game_back);
        player.setLooping(true);
        player.setVolume(0.4f ,0.4f);
        if(Main.soundOn) player.start();
    }

    public void startMusic() {
        if(Main.soundOn) player.start();
    }

    public void pauseMusic() {
        if(player.isPlaying()) player.pause();
    }

    public void stopMusic() {
        if(player.isPlaying()) player.stop();
    }

    public void onShow(final Button startButton) {
        AlertDialog.Builder looseAlert = new AlertDialog.Builder(context);
        looseAlert.setTitle(gameMode)
                .setMessage(getGameRules())
                .setCancelable(false)
                .setNegativeButton("I am ready",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                startButton.setText("START");
                                startButton.setVisibility(View.VISIBLE);
                            }
                        }
                );
        AlertDialog alert = looseAlert.create();
        alert.show();
    }

    public void whiteArray(TextView[] tileArray) {                              //setColor all tiles to White
        for (int i = 0; i < 16; i++) {
            tileArray[i].setBackgroundColor(Color.WHITE);
        }
    }

    public void changeColor(TextView[] tileArray) {            //setColor all tiles to white which not black
        for (int i = 0; i < 16; i++) {
            tileArray[i].setBackgroundColor(Color.WHITE);
        }

        for (int i = 0; i < 5; i++) {
            int k = (int) (Math.random() * 16);            //setColor tiles to black for clicked and up-gameScore
            tileArray[k].setBackgroundColor(Color.BLACK);
        }
    }

    public void connectDb() {
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public String getGameRules() {
        DatabaseCursor = db.rawQuery("Select description from modeDescription where mode_name='" + gameMode + "';", null);
        DatabaseCursor.moveToFirst();
        gameRules = DatabaseCursor.getString(DatabaseCursor.getColumnIndex("description"));
        return gameRules;
    }

    public int getHighScore() {
        DatabaseCursor = db.rawQuery("Select score from highScore where game_id=" + gameId + ";", null);
        DatabaseCursor.moveToFirst();
        highScore = DatabaseCursor.getInt(DatabaseCursor.getColumnIndexOrThrow("score"));
        return highScore;
    }

    public void updateHighScore(int highScoreInGame) {
        db.execSQL("Update highScore set score=" + highScoreInGame + " where game_id=" + gameId + ";");
    }

    public void updateGamesAndSummary(int score) {
        db.execSQL("Update highScore set games = (games+1) where game_id=" + gameId + ";");
        db.execSQL("Update highScore set summary = (summary+" + score + ") where game_id=" + gameId + ";");
    }

    public void showScore(int score, int highScoreInGame) {
        AlertDialog.Builder looseAlert = new AlertDialog.Builder(context);
        looseAlert.setTitle("GAME OVER")
                .setMessage("You finished with score: " + score)
                .setCancelable(false)
                .setNegativeButton("Okay!",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );
        if (score == highScoreInGame) {
            looseAlert.setMessage("You finished with score: " + score + "\nThis is your new high score!");
        }

        AlertDialog alert = looseAlert.create();
        alert.show();

    }

    public int getHighScore(int gameid) {
        DatabaseCursor = db.rawQuery("Select score from highScore where game_id=" + gameid + ";", null);
        DatabaseCursor.moveToFirst();
        return DatabaseCursor.getInt(DatabaseCursor.getColumnIndexOrThrow("score"));
    }

    public int getSumScore(int gameid) {
        DatabaseCursor = db.rawQuery("Select summary from highScore where game_id=" + gameid + ";", null);
        DatabaseCursor.moveToFirst();
        return DatabaseCursor.getInt(DatabaseCursor.getColumnIndexOrThrow("summary"));
    }

    public int getHighestScore() {
        DatabaseCursor = db.rawQuery("Select max(score) as high from highScore;", null);
        DatabaseCursor.moveToFirst();
        return DatabaseCursor.getInt(DatabaseCursor.getColumnIndexOrThrow("high"));
    }

    public int getGamesCount(int gameid) {
        DatabaseCursor = db.rawQuery("Select games from highScore where game_id=" + gameid + ";", null);
        DatabaseCursor.moveToFirst();
        return DatabaseCursor.getInt(DatabaseCursor.getColumnIndexOrThrow("games"));
    }

    public int getSignStatus() {
        DatabaseCursor = db.rawQuery("Select signed from signing;", null);
        DatabaseCursor.moveToFirst();
        return DatabaseCursor.getInt(DatabaseCursor.getColumnIndexOrThrow("signed"));
    }

    public void setSignStatus(int p) {
        db.execSQL("update signing set 'signed' = " + p + ";");
    }
}
