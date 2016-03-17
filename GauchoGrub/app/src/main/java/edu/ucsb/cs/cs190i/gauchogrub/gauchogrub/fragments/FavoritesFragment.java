package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.Bind;
import butterknife.ButterKnife;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.activities.MainActivity;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.R;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.adapters.FavoriteRecyclerAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FavoritesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FavoritesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoritesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    @Bind(R.id.FavoritesFragment_recyclerView)
    RecyclerView recyclerView;

    private String diningCommon;
    private FavoriteRecyclerAdapter favoriteRecyclerAdapter;
    ExecutorService executorService;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavoritesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavoritesFragment newInstance(String param1, String param2) {
        FavoritesFragment fragment = new FavoritesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.MainActivity_dining_common_shared_prefs), Context.MODE_PRIVATE);
        diningCommon = sharedPreferences.getString(MainActivity.STATE_CURRENT_DINING_COMMON,
                sharedPreferences.getString(getString(R.string.pref_key_default_dining_common),
                        getString(R.string.DLG)));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        activity.fab.show();
        activity.updateAppBarTitle(getString(R.string.FavoritesFragment_app_bar_title), true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        ButterKnife.bind(this, view);
        setRecyclerAdapter(diningCommon);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void switchDiningCommon(String diningCommon) {
        this.diningCommon = diningCommon;
        setRecyclerAdapter(diningCommon);
        ((MainActivity) getActivity()).updateAppBarTitle(getString(R.string.FavoritesFragment_app_bar_title), true);
    }

    private void setRecyclerAdapter(String diningCommon) {
        if (favoriteRecyclerAdapter != null) {
            recyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
            favoriteRecyclerAdapter.close();
            executorService.shutdownNow();
        }
        // Create new adapter and executor
        favoriteRecyclerAdapter = new FavoriteRecyclerAdapter(diningCommon, getContext());
        executorService = Executors.newSingleThreadExecutor();
        // Set executor and adapter
        favoriteRecyclerAdapter.setExecutor(executorService);
        recyclerView.setAdapter(favoriteRecyclerAdapter);
        // Set layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Start async query
        favoriteRecyclerAdapter.queryAsync();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}