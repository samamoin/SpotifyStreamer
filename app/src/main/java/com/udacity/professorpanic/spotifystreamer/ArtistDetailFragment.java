package com.udacity.professorpanic.spotifystreamer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Created by DoctorMondo on 6/20/2015.
 */
public class ArtistDetailFragment extends ListFragment {


private String artistString;
private String artistName;
private SpotifyApi api;
private SpotifyService spotifyService;
private ArrayList<Track> topTracks = new ArrayList<Track>(); //initializing this just to have something to inflate the view with at first, this will get overwritten by the Async Task I made
private static final String TAG = "ArtistDetailFragment";
private Callbacks mCallbacks;
public static final String TRACK_LIST = "Artist Top Ten Tracks";
public static final String CHOSEN_TRACK = "Chosen Track";
public static final String PASSED_ARTIST_NAME = "Artist Name";
public static final String ARTIST_ID = "Spotify Artist ID";


private ArtistDetailAdapter mAdapter;

    public interface Callbacks
    {
        void onArtistSelected(MediaPlayerFragment fragment);

    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mCallbacks = (Callbacks)activity;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mCallbacks=null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        artistString = getArguments().getString(getString(R.string.EXTRA_ARTIST));
        api = new SpotifyApi();
        spotifyService = api.getService();


    }



    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        MediaPlayerFragment mediaPlayerFragment = new MediaPlayerFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(TRACK_LIST, topTracks);
        args.putInt(CHOSEN_TRACK, position);
        args.putString(PASSED_ARTIST_NAME, artistName);
        args.putString(ARTIST_ID, artistString);

        mediaPlayerFragment.setArguments(args);

        mCallbacks.onArtistSelected(mediaPlayerFragment);

    }




    @Override
    public void onResume()
    {
        super.onResume();
        getActivity().setTitle(R.string.top_ten_tracks);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_detail,container, false);
        ListView listView = (ListView)rootView.findViewById(android.R.id.list);

        FetchArtistDetailTask fetchArtistDetailTask = new FetchArtistDetailTask();
        fetchArtistDetailTask.execute(artistString);


        mAdapter = new ArtistDetailAdapter(getActivity(), R.layout.artist_list_item, topTracks);
        listView.setAdapter(mAdapter);


        return  rootView;
    }



    public class FetchArtistDetailTask extends AsyncTask<String, Void, Void>
    {
        ArrayList<Artist> artistList;
        Tracks artistTopTracks;



        @Override
        protected Void doInBackground(String... params) {
            String artistId = params[0];
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            String country = prefs.getString(getString(R.string.pref_country_code_key), getString(R.string.default_country_key));
            try {
                artistTopTracks = spotifyService.getArtistTopTrack(artistId, country);
            }
            catch (Exception ex)
            {
                //putting this in here in case the country code is invalid, I'll just have it default to murrica.
                artistTopTracks = spotifyService.getArtistTopTrack(artistId, "US");
            }
            Log.i(TAG, "" + Locale.getDefault().getCountry());
            artistName = spotifyService.getArtist(artistId).name;




            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            //tracks returns something that uses the List interface so I should be able to cast it to an ArrayList without a big issue, to make it easier to handle with the adapter
            topTracks = (ArrayList)artistTopTracks.tracks;

            if (topTracks.size() < 1)
            {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.no_songs_found), Toast.LENGTH_SHORT).show();
            }
            else
            {
                mAdapter.addAll(topTracks);
            }

            mAdapter.notifyDataSetChanged();


        }
    }


    @Override
    public void onStop() {
        super.onStop();
        mAdapter.notifyDataSetChanged();
    }
}
