package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.Bind;
import butterknife.ButterKnife;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.adapters.ScheduleRecyclerAdapter;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.activities.MainActivity;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.R;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.adapters.FavoriteRecyclerAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScheduleFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private String diningCommon;

    @Bind(R.id.ScheduleFragment_Monday)
    RecyclerView recyclerViewMonday;

    private ScheduleRecyclerAdapter mondayRecyclerAdapter;
    private ExecutorService mondayExecutorService;

    @Bind(R.id.ScheduleFragment_Tuesday)
    RecyclerView recyclerViewTuesday;
    
    private ScheduleRecyclerAdapter tuesdayRecyclerAdapter;
    private ExecutorService tuesdayExecutorService;

    @Bind(R.id.ScheduleFragment_Wednesday)
    RecyclerView recyclerViewWednesday;
    
    private ScheduleRecyclerAdapter wednesdayRecyclerAdapter;
    private ExecutorService wednesdayExecutorService;

    @Bind(R.id.ScheduleFragment_Thursday)
    RecyclerView recyclerViewThursday;

    private ScheduleRecyclerAdapter thursdayRecyclerAdapter;
    private ExecutorService thursdayExecutorService;

    @Bind(R.id.ScheduleFragment_Friday)
    RecyclerView recyclerViewFriday;

    private ScheduleRecyclerAdapter fridayRecyclerAdapter;
    private ExecutorService fridayExecutorService;

    @Bind(R.id.ScheduleFragment_Saturday)
    RecyclerView recyclerViewSaturday;

    private ScheduleRecyclerAdapter saturdayRecyclerAdapter;
    private ExecutorService saturdayExecutorService;

    @Bind(R.id.ScheduleFragment_Sunday)
    RecyclerView recyclerViewSunday;

    private ScheduleRecyclerAdapter sundayRecyclerAdapter;
    private ExecutorService sundayExecutorService;


    public ScheduleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScheduleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScheduleFragment newInstance(String param1, String param2) {
        ScheduleFragment fragment = new ScheduleFragment();
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
        activity.updateAppBarTitle(getString(R.string.SchedulesFragment_app_bar_title), true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        ButterKnife.bind(this, view);
        setScheduleAdapters();
        return view;
    }

    public void updateDiningCommon(String diningCommon) {
        this.diningCommon = diningCommon;
        setScheduleAdapters();
        ((MainActivity) getActivity()).updateAppBarTitle(getString(R.string.SchedulesFragment_app_bar_title), true);
    }

    private void setScheduleAdapters() {
        setScheduleAdapter(recyclerViewMonday, mondayRecyclerAdapter, mondayExecutorService, DateTimeConstants.MONDAY);
        setScheduleAdapter(recyclerViewTuesday, tuesdayRecyclerAdapter, tuesdayExecutorService, DateTimeConstants.TUESDAY);
        setScheduleAdapter(recyclerViewWednesday, wednesdayRecyclerAdapter, wednesdayExecutorService, DateTimeConstants.WEDNESDAY);
        setScheduleAdapter(recyclerViewThursday, thursdayRecyclerAdapter, thursdayExecutorService, DateTimeConstants.THURSDAY);
        setScheduleAdapter(recyclerViewFriday, fridayRecyclerAdapter, fridayExecutorService, DateTimeConstants.FRIDAY);
        setScheduleAdapter(recyclerViewSaturday, saturdayRecyclerAdapter, saturdayExecutorService, DateTimeConstants.SATURDAY);
        setScheduleAdapter(recyclerViewSunday, sundayRecyclerAdapter, sundayExecutorService, DateTimeConstants.SUNDAY);
    }

    private void setScheduleAdapter(RecyclerView recyclerView, @Nullable ScheduleRecyclerAdapter adapter, @Nullable ExecutorService executorService, int dayOfWeek) {
        if(adapter != null && executorService != null) {
            recyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
            adapter.close();
            executorService.shutdownNow();
        }
        // Create new adapter and executor
        adapter = new ScheduleRecyclerAdapter(diningCommon, dayOfWeek, getContext());
        executorService = Executors.newSingleThreadExecutor();
        // Set executor and adapter
        adapter.setExecutor(executorService);
        recyclerView.setAdapter(adapter);
        // Set layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Start async query
        adapter.queryAsync();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
