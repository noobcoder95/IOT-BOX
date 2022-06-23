package com.smartiotdevices.iotbox;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.smartiotdevices.iotbox.sshutils.SessionController;

public class FrgCctv_add2 extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public FrgCctv_add2()
    {

    }

    public static FrgCctv_add2 newInstance(String param1, String param2)
    {
        FrgCctv_add2 fragment = new FrgCctv_add2();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_cctv_add2, container, false);
        SwipeRefreshLayout swipeLayout = rootView.findViewById(R.id.swipe_cctv_add2);
        swipeLayout.setOnRefreshListener(this);
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector);
        PlayerView playerView = rootView.findViewById(R.id.cctv_player_add2);

        playerView.setPlayer(player);
        playerView.setVisibility(View.INVISIBLE);

        playerView.setVisibility(View.VISIBLE);
        RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
        MediaSource videoSource = new ExtractorMediaSource.Factory(rtmpDataSourceFactory).createMediaSource(Uri.parse("rtmp://"+SessionController.getSessionController().getSessionUserInfo().getHost()+"/live/technodynamite_add2"));

        player.prepare(videoSource);
        player.setPlayWhenReady(true);

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_cctv,menu);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onRefresh()
    {
        if (getFragmentManager() != null)
        {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
        if (!SessionController.isConnected())
        {
            Toast.makeText(getActivity(), getString(R.string.message_cctvfail), Toast.LENGTH_SHORT).show();
        }

        else
        {
            Toast.makeText(getActivity(), getString(R.string.message_loading), Toast.LENGTH_SHORT).show();
        }
    }

    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }

    public void onButtonPressed(Uri uri)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }
}
