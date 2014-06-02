package nl.elements.animations;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {
    private FlipBookView flipBookView;

    private static final int IDLE_DURATION=30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flipBookView = (FlipBookView)findViewById(R.id.flipBook);
        flipBookView.setPattern("tmp_%d");
        flipBookView.setFlipDuration(IDLE_DURATION);
        flipBookView.flip();
    }



}
