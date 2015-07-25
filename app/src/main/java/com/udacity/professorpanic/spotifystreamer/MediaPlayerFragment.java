package com.udacity.professorpanic.spotifystreamer;

import android.app.Activity;
import android.app.DialogFragment;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by DoctorMondo on 7/15/2015.
 */
public class MediaPlayerFragment extends DialogFragment implements MediaController.MediaPlayerControl, MediaPlayer.OnPreparedListener, View.OnTouchListener{
    private final static String TAG = "MediaPlayerFragment";
    private MediaPlayer mPlayer = new MediaPlayer();
    private ArrayList<Track> topTracks;
    private Uri trackUri;
    ImageView trackImage;
    TextView artistNameTextView;
    TextView trackName;
    View rootView;
    TextView albumName;
    ImageButton playButton;
    ImageButton skipNextButton;
    ImageButton skipPreviousButton;
    private String artistName;
    private int chosenTrack=0;
    private static final String CHOSEN_TRACK = "Chosen Track";
    private static final String PASSED_ARTIST_NAME = "Artist Name";
    private static final String TRACK_LIST = "Artist Top Ten Tracks";
    private MediaController mController;
    private Handler mHandler = new Handler();
    

    public MediaPlayer getMediaPlayer() {
        return this.mPlayer;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //Intent intent = getActivity().getIntent();
        Bundle args = this.getArguments();
        topTracks = (ArrayList<Track>)args.get(TRACK_LIST);
        chosenTrack = args.getInt(CHOSEN_TRACK);
        artistName = args.getString(PASSED_ARTIST_NAME);
        Log.i(TAG, artistName);
        //mController = new MediaController(getActivity());
        mController = new MediaController(getActivity()) {
            @Override
            public void hide() {}      // This bit is to keep the controller from hiding after 3 seconds

            //this is to close out both the mediacontroller and player in one swoop
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    Activity a = (Activity)getContext();
                    a.finish();

                }
                return true;
            }
        };

       // Bundle intentBundle = intent.getExtras();
        mPlayer.reset();
        mPlayer.setOnPreparedListener(this);



        try {

            trackUri = Uri.parse(topTracks.get(chosenTrack).preview_url);
            mPlayer.setDataSource(getActivity(), trackUri);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "Could not open file for playback. trackUri is " + trackUri.toString(), e);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.media_player_layout,container, false);

        trackImage = (ImageView) rootView.findViewById(R.id.mediaplayer_image);

        artistNameTextView = (TextView) rootView.findViewById(R.id.player_artist_name);
        artistNameTextView.setText(artistName);

        albumName = (TextView) rootView.findViewById(R.id.player_artist_album);
        albumName.setText(topTracks.get(chosenTrack).album.name);

        trackName = (TextView) rootView.findViewById(R.id.player_artist_track);
        trackName.setText(topTracks.get(chosenTrack).name);

        SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        playButton = (ImageButton) rootView.findViewById(R.id.play_button);
        playButton.setImageResource(R.drawable.ic_action_pause);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer.isPlaying())
                {
                    playButton.setImageResource(R.drawable.ic_action_play_arrow);
                    mPlayer.pause();
                }
                else
                {
                    playButton.setImageResource(R.drawable.ic_action_pause);
                    mPlayer.start();
                }

            }
        });
        skipNextButton = (ImageButton) rootView.findViewById(R.id.skip_track_button);
        skipPreviousButton = (ImageButton) rootView.findViewById(R.id.previous_track_button);


        for (Track t : topTracks)
        {
            Log.i(TAG, t.name.toString());
        }

        if (!Utility.isNetworkAvailable(getActivity().getApplicationContext()))
        {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.no_network_error), Toast.LENGTH_SHORT).show();
            trackImage.setImageResource(R.drawable.ic_music_note);
        }
        else
        {
            Picasso.with(getActivity()).load(topTracks.get(chosenTrack).album.images.get(0).url).into(trackImage);
        }
        return rootView;
    }

    @Override
    public void onStop()
    {
        super.onStop();
        mController.hide();
        mPlayer.stop();
        mPlayer.release();

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mController.show();
        Log.i(TAG, "onTouch");
        return false;
    }

    public class FetchMusicTask extends AsyncTask<List<Artist>, Void, Void>
    {


        @Override
        protected Void doInBackground(List<Artist>... params)
        {
            //artistList = params[0];

            return null;
        }

        @Override
        protected void onPostExecute(Void v)
        {
            super.onPostExecute(v);



        }
    }




//These are for the MediaController


    @Override
    public void start() {
        mPlayer.start();
        playButton.setImageResource(R.drawable.ic_action_pause);
        Log.i(TAG, "onStart");
    }



    @Override
    public void pause() {

        mPlayer.pause();

    }

    @Override
    public int getDuration() {
        return mPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mPlayer.getCurrentPosition();

    }

    @Override
    public void seekTo(int pos) {
        mPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {

        return mPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {


        mController.setMediaPlayer(this);

        mController.setAnchorView(rootView);
        mController.requestFocus();

        mHandler.post(new Runnable() {
            public void run() {
                mController.setEnabled(true);
                mController.show(0);
            }
        });
        mController.show(0);
    }



}