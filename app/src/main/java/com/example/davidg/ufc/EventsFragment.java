package com.example.davidg.ufc;

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

import com.example.davidg.ufc.Model.Events;
import com.example.davidg.ufc.adapter.EventAdapter;
import com.example.davidg.ufc.api.ObservableEventsApiInterface;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by DavidG on 21/03/2018.
 */

public class EventsFragment extends Fragment {


    private SwipeRefreshLayout strl;
    private RecyclerView rv;
    private EventAdapter eventAdapter;

    public static EventsFragment newInstance() {

        Bundle args = new Bundle();
        EventsFragment fragment = new EventsFragment();
        fragment.setArguments( args );
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.activity_eventfragment, container, false );

        rv = view.findViewById( R.id.rv_events );
        rv.setAdapter( eventAdapter = new EventAdapter() );
        rv.setLayoutManager( new LinearLayoutManager( getActivity(), LinearLayoutManager.VERTICAL, false ) );


        strl = view.findViewById( R.id.swipeRefreshevent );
        strl.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                eventAdapter.emptyEvents();

                strl.postDelayed( new Runnable() {
                    @Override
                    public void run() {
                        doNetworkCall();
                    }
                }, 100L );

                dotheUpdate();
            }
        } );

        doNetworkCall();


        return view;
    }


    private void dotheUpdate() {
        strl.setRefreshing( false );

    }

    private void doNetworkCall() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl( "http://ufc-data-api.ufc.com/" )
                .addConverterFactory( GsonConverterFactory.create() )
                .addCallAdapterFactory( RxJava2CallAdapterFactory.create() )
                .build();

        ObservableEventsApiInterface apiService = retrofit.create( ObservableEventsApiInterface.class );
        apiService.getEvents()
                .subscribeOn( Schedulers.io() )
                .observeOn( AndroidSchedulers.mainThread() )
                .subscribe( new Consumer<List<Events>>() {
                    @Override
                    public void accept(List<Events> events) throws Exception {

                        eventAdapter.addEvents( events );
                        Toast.makeText( getActivity(), events.get( 0 ).getSubtitle(), Toast.LENGTH_LONG ).show();
                        strl.setRefreshing( false );

                    }
                } );


    }


}