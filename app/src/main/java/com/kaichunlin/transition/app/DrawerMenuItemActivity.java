package com.kaichunlin.transition.app;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import com.kaichunlin.transition.Animation.Animation;
import com.kaichunlin.transition.MenuItemTransition;
import com.kaichunlin.transition.MenuItemTransitionBuilder;
import com.kaichunlin.transition.adapter.DrawerListenerAdapter;
import com.kaichunlin.transition.adapter.ITransitionAdapter;
import com.kaichunlin.transition.adapter.MenuOptionConfiguration;

import kaichunlin.transition.app.R;


public class DrawerMenuItemActivity extends AppCompatActivity implements View.OnClickListener {
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private DrawerListenerAdapter mDrawerListenerAdapter;
    private MenuItemTransition mFlipOpen;
    private MenuItemTransition mFlipClose;
    private MenuItemTransition mShrinkOpen;
    private MenuItemTransition mShrinkClose;
    private MenuItemTransition mRotateOpen;
    private MenuItemTransition mRotateClose;
    private Animation mStartAnimation;
    private boolean mFirstTimeAnimation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_drawer_menuitem);
        findViewById(R.id.flip_fade).setOnClickListener(this);
        findViewById(R.id.shrink_fade).setOnClickListener(this);
        findViewById(R.id.rotate).setOnClickListener(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,              /* host Activity */
                mDrawerLayout,    /* DrawerLayout object */
                toolbar,
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        );

        //set up the transition
        //Creates a shared configuration that: applies alpha, the transition effect is applied in a cascading manner
        // (v.s. simultaneously), MenuItem will reset to enabled when transiting, and invalidates menu on transition
        // completion
        MenuItemTransitionBuilder sharedBuilder = MenuItemTransitionBuilder.transit(toolbar).visibleOnStartAnimation(true).invalidateOptionOnStopTransition(this, true);
        MenuItemTransitionBuilder builder = sharedBuilder.clone().id("Flip").alpha(1f, 0.5f).translationX(0, 30).cascade(0.3f);
        mFlipOpen = builder.scaleX(1f, 0f).build();
        mFlipClose = builder.reverse().translationX(0, -30).build();
        mShrinkClose = builder.scale(1f, 0f).id("Shrink").build();
        mShrinkOpen = builder.reverse().translationX(0, 30).build();
        builder = sharedBuilder.id("Rotate").rotation(0f, 180f).cascade(0.15f);
        mRotateOpen = builder.scale(1f, 0f).build();
        mRotateClose = builder.reverse().build();

        //set up the adapter
        mDrawerListenerAdapter = new DrawerListenerAdapter(mDrawerToggle, R.id.drawerList);
        mDrawerListenerAdapter.setDrawerLayout(mDrawerLayout);
        mDrawerListenerAdapter.setDrawerListener(new DialogDrawerListener(this));

        mStartAnimation = new Animation(mFlipOpen.clone().reverse());

        //this is to prevent conflict when the drawer is being opened while the above mStartAnimation is still in progress
        //unfortunately there's no way to reconcile the two, so the transiting/animating View will "jump" to a new state
        //TODO evaluate if it's possible to reconcile the two states automatically, maybe if they share the same ITransition instance?
        mDrawerListenerAdapter.addTransitionListener(new ITransitionAdapter.TransitionListener() {
            @Override
            public void onStartTransition(ITransitionAdapter adapter) {
                mStartAnimation.cancelAnimation();
            }

            @Override
            public void onStopTransition(ITransitionAdapter adapter) {
            }
        });

        //set the initial options
        onClick(findViewById(R.id.flip_fade));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mDrawerListenerAdapter.onCreateOptionsMenu(this, menu);

        //TODO make it a build-in function for AnimationAdapter
        if (mFirstTimeAnimation) {
            //wait after onCreateOptionsMenu is called to start the animation
            //startAnimationDelayed is used with a delay of 0 to allow menu to be properly added to the view before executing mStartAnimation
            mStartAnimation.startAnimationDelayed(800, 0);
            mFirstTimeAnimation = false;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.flip_fade:
                mDrawerListenerAdapter.setupOptions(this, new MenuOptionConfiguration(mFlipOpen, R.menu.drawer), new MenuOptionConfiguration(mFlipClose, R.menu.main));
                break;
            case R.id.shrink_fade:
                mDrawerListenerAdapter.setupOptions(this, new MenuOptionConfiguration(mShrinkOpen, R.menu.drawer), new MenuOptionConfiguration(mShrinkClose, R.menu.main));
                break;
            case R.id.rotate:
                mDrawerListenerAdapter.setupCloseOption(this, new MenuOptionConfiguration(mRotateClose, R.menu.main));
                break;
        }
    }
}
