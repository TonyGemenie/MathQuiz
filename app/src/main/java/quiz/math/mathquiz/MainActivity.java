package quiz.math.mathquiz;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.startButton) Button startButton;
    @BindViews({R.id.button, R.id.button1, R.id.button2, R.id.button3}) List<Button> buttons;
    @BindView(R.id.playAgain) Button playAgainButton;
    @BindView(R.id.score) TextView mScore;
    @BindView(R.id.equation) TextView mEquation;
    @BindView(R.id.timer_text) TextView mTimer;
    @BindView(R.id.score_card) TextView mScoreCard;
    @BindView(R.id.high_score_text_view) TextView highScoreTextView;
    @BindView(R.id.result) TextView mResult;
    RelativeLayout gameRelativeLayout;

    private String[] operations = {"+", "-", "*", "/"};
    ArrayList<Integer> mHighScores = new ArrayList<>();
    ArrayList<String> dateList = new ArrayList<>();
    ArrayList<String> dateListHighScores = new ArrayList<>();
    private int mScoreOfGame = 0;
    private int mQuestions = 0;
    private int mFirst;
    private int mSecond;
    private String mOperator;
    private int answerIndex;
    private boolean active;
    private Random mRnd;
    private SharedPreferences sharedPreferences;
    public static final String HIGH_SCORE = "high_score";
    public static final String DATE = "date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        gameRelativeLayout = findViewById(R.id.gameRelativeLayout);
        mRnd = new Random();
        sharedPreferences = this.getSharedPreferences("ice_nine.cj.math", MODE_PRIVATE);
        for (int i = 0; i < 5; i++) {
            mHighScores.add(sharedPreferences.getInt(HIGH_SCORE + i , 0));
            dateList.add(sharedPreferences.getString(DATE + i, ""));
        }
    }

    public void start(View v) {
        startButton.setVisibility(View.GONE);
        playGame();
    }

    public void displayHighScores(String highscores){
        playAgainButton.setVisibility(View.VISIBLE);
        gameRelativeLayout.setVisibility(RelativeLayout.GONE);
        String highscoreString = getString(R.string.highscores) + "\n" + highscores;
        highScoreTextView.setText(highscoreString);
        highScoreTextView.setAlpha(1f);
        ObjectAnimator animateResult = ObjectAnimator.ofFloat(mScoreCard, "TranslationY", 0f);
        ObjectAnimator animatePlayAgain = ObjectAnimator.ofFloat(playAgainButton, "TranslationY", 0f);
        ObjectAnimator animateHighScore = ObjectAnimator.ofFloat(highScoreTextView, "TranslationY", 0f);
        mScoreCard.setY(-800f);
        highScoreTextView.setY(-800f);
        playAgainButton.setY(5000f);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(1500);
        set.playTogether(animateResult, animatePlayAgain, animateHighScore);
        set.start();
    }

    public void playAgain(View v){
        mResult.setVisibility(View.INVISIBLE);
        playGame();
    }

    public void resetGameElements(){
        playAgainButton.setVisibility(View.GONE);
        gameRelativeLayout.setVisibility(RelativeLayout.VISIBLE);
        highScoreTextView.setAlpha(0f);
        mScoreOfGame = 0;
        mQuestions = 0;
        mTimer.setText(R.string.timer_string);
        mScore.setText(R.string.score_string);
    }

    public void playGame() {
        resetGameElements();
        active = true;
        gameRelativeLayout.setY(-1000);
        ObjectAnimator animateGameLayout = ObjectAnimator.ofFloat(gameRelativeLayout, "TranslationY", 0f);
        animateGameLayout.setDuration(500);
        animateGameLayout.start();

        new CountDownTimer(30100, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String timerString = String.valueOf(millisUntilFinished / 1000) + "s";
                mTimer.setText(timerString);
            }

            @Override
            public void onFinish() {
                active = false;
                mTimer.setText(R.string.timer_finish);
                mResult.setVisibility(View.VISIBLE);
                mResult.setText(mScore.getText());
                mScoreCard.setAlpha(1f);
                String scoreCardString = getString(R.string.score_card_string) + mScore.getText();
                mScoreCard.setText(scoreCardString);
                displayHighScores(configureHighScores());
            }
        }.start();
        getEquation();
    }

    public String configureHighScores(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d, ''yy  h:mm a", Locale.US);
        String date = simpleDateFormat.format(new Date());
        mHighScores.add(mScoreOfGame);
        Collections.sort(mHighScores, Collections.reverseOrder());
        String highscores = "";
        dateListHighScores.clear();
        dateList.add(date);
        dateList.add(date);
        dateListHighScores.addAll(dateList);
        for (int i = 0; i < 5; i++) {
            if (mHighScores.get(i) == mScoreOfGame && mHighScores.get(i+1) != mScoreOfGame) {
                for (int j = 0; j < 5 - i; j++) {
                    dateListHighScores.set(i+1+j, dateList.get(i+j));
                }
                dateListHighScores.set(i , dateList.get(dateList.size() - 1));
            }
            highscores += mHighScores.get(i) + "   " + dateListHighScores.get(i) + "\n";
        }
        dateList.clear();
        dateList.addAll(dateListHighScores);
        return highscores;
    }

    @Override
    protected void onStop() {
        Collections.sort(mHighScores, Collections.reverseOrder());
        for (int i = 0; i < 5; i++) {
            sharedPreferences.edit().putInt("HIGH_SCORE" + i, mHighScores.get(i)).commit();
            sharedPreferences.edit().putString("DATE" + i, dateList.get(i)).commit();
        }
        super.onStop();
    }

    public void getEquation() {
        int[] mAnswers = {0, 0, 0, 0};
        int operatorIndex = mRnd.nextInt(4);
        int answer = operatorEquation(operatorIndex);
        answerIndex = mRnd.nextInt(4);
        setEquationText();

        int incorrect;
        for (int i = 0; i < 4; i++) {
            if (i == answerIndex) {
                mAnswers[i] = answer;
            } else {
                incorrect = operatorEquation(operatorIndex);
                while (incorrect == mAnswers[0] || incorrect == mAnswers[1] ||
                        incorrect == mAnswers[2] || incorrect == mAnswers[3] ||
                        incorrect == answer) {
                    incorrect = operatorEquation(operatorIndex);
                }
                mAnswers[i] = incorrect;
            }
        }
        for (int i = 0; i < mAnswers.length; i++) {
            buttons.get(i).setText(String.valueOf(mAnswers[i]));
        }
    }

    public int operatorEquation (int operator) {
        mOperator = operations[operator];
        int bounds = 0;
        switch (operator){
            case(0):
            case(1):
                bounds = 200;
                break;
            case(2):
            case(3):
                bounds = 20;
                break;
        }
        mFirst = mRnd.nextInt(bounds);
        mSecond = mRnd.nextInt(bounds);
        while (mFirst < mSecond || mFirst == 0 || mSecond == 0 || mFirst == mSecond || mFirst == 1 || mSecond == 1) {
            mSecond = mRnd.nextInt(bounds);
            mFirst = mRnd.nextInt(bounds);
        }
        return computation(operator);
    }

    public int computation(int operator) {

        switch (operator) {
            case (0):
                return mFirst + mSecond;
            case (1):
                return mFirst - mSecond;
            case (2):
                return mFirst * mSecond;
            case (3):
                return mSecond;
        }
        return 0;
    }

    public void setEquationText() {
        String equationString;
        if (mOperator.equals("/")) {
            String firstTerm = String.valueOf(mFirst * mSecond);
            equationString = firstTerm + mOperator + mFirst;
        } else {
            equationString = mFirst + mOperator + mSecond + "";
        }
        mEquation.setText(equationString);
    }

    public void chooseAnswer(View v) {
        if (active) {
            if (v.getTag().toString().equals(Integer.toString(answerIndex))) {
                mScoreOfGame++;
                mResult.setText(R.string.right_string);

            } else {
                mResult.setText(R.string.wrong_string);
            }
            mQuestions++;
            mResult.setAlpha(1f);
            ObjectAnimator resultFade = ObjectAnimator.ofFloat(mResult, "alpha", 0f);
            resultFade.setDuration(1000);
            resultFade.start();
            String gameScoreString = String.valueOf(mScoreOfGame) + "/" + String.valueOf(mQuestions);
            mScore.setText(gameScoreString);
            getEquation();
        }
    }
}
