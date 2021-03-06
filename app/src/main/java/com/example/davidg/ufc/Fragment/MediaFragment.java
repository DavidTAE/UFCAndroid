package com.example.davidg.ufc.Fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.davidg.ufc.Model.Fighters;
import com.example.davidg.ufc.Model.Media;
import com.example.davidg.ufc.R;
import com.example.davidg.ufc.adapter.FightersAdapter;
import com.example.davidg.ufc.adapter.MediaAdapter;
import com.example.davidg.ufc.api.ObservableEventsApiInterface;
import com.example.davidg.ufc.api.ObservableFightersApiInterface;
import com.example.davidg.ufc.api.ObservableMediaApiInterface;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by DavidG on 22/03/2018.
 */

public class MediaFragment extends Fragment {

    @BindView(R.id.swipeRefreshMedia)
    SwipeRefreshLayout strl;
    @BindView(R.id.rv_media)
    RecyclerView recyclerView;

    private MediaAdapter mediaAdapter;


    public static MediaFragment newInstance() {

        Bundle args = new Bundle();
        MediaFragment fragment = new MediaFragment();
        fragment.setArguments( args );
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.activity_mediafragment, container, false );

        ButterKnife.bind( this, view );

        recyclerView.setAdapter( mediaAdapter = new MediaAdapter() );
        recyclerView.setLayoutManager( new LinearLayoutManager( getActivity(), LinearLayoutManager.VERTICAL, false ) );

        strl.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mediaAdapter.emptyMedia();

                strl.postDelayed( new Runnable() {
                    @Override
                    public void run() {
                        createMedia();
                    }
                }, 100L );

                dotheUpdate();
            }
        } );
        createMedia();


        return view;


    }

    private void dotheUpdate() {

        strl.setRefreshing( false );
    }

    private void createMedia() {
        int cacheSize = 10 * 1024 * 1024;                                  // stores the data with the size of 10MB
        Cache cache = new Cache( getActivity().getCacheDir(), cacheSize );   //Caching the data for offline use.
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .cache( cache )
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl( "http://ufc-data-api.ufc.com/" )
                .client( okHttpClient )
                .addCallAdapterFactory( RxJava2CallAdapterFactory.create() )
                .addConverterFactory( GsonConverterFactory.create() )
                .build();

        final ObservableEventsApiInterface eventsApiInterface = retrofit.create( ObservableEventsApiInterface.class );
        eventsApiInterface.getMedia()
                .subscribeOn( Schedulers.io() )
                .observeOn( AndroidSchedulers.mainThread() )
                .subscribe( new Consumer<List<Media>>() {

                    @Override
                    public void accept(List<Media> media) throws Exception {

                        mediaAdapter.addMedia( media );
                        Toast.makeText( getActivity(), media.get( 0 ).getMediaDate(), Toast.LENGTH_SHORT ).show();
                        strl.setRefreshing( false );

                    }

                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText( getActivity(), throwable.getMessage(), Toast.LENGTH_LONG ).show();
                        strl.setRefreshing( false );
                    }
                } );


    }
}
