package com.apptec.camello.mainactivity.fhome;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.apptec.camello.R;
import com.apptec.camello.databinding.FragmentHomeBinding;
import com.apptec.camello.mainactivity.BaseFragment;
import com.apptec.camello.mainactivity.fhome.geofence.VerifyLocation;
import com.apptec.camello.mainactivity.fhome.ui.DayViewContainer;
import com.apptec.camello.mainactivity.fhome.ui.MonthHeaderViewContainer;
import com.apptec.camello.models.WorkZoneModel;
import com.apptec.camello.util.Constants;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.TextStyle;
import org.threeten.bp.temporal.WeekFields;

import java.util.Locale;

import timber.log.Timber;

/**
 * Home fragment
 */
public class HomeFragment extends BaseFragment {


    // Using data binding
    private FragmentHomeBinding binding;

    // Constant for the calendar view
    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-419");


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel.setActiveFragmentName(getString(R.string.home_fragment_title));


    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false); // Inflate the view

        // Set the main viewmodel instance to the layout
        binding.setMainViewModel(mainViewModel);


        this.setupCalendar();                        // Setting up the calendar view

        this.setupCentralButtonManager();                       // Setting up the button


        // Creating a lister for the button
        binding.fragmentHomeStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("Button clicked");
                /**
                 * Verify first if the location service is available and the if
                 * the user is in a work zone
                 */
                new Thread(new VerifyLocation(new VerifyLocation.Listener() {

                    /**
                     * Show a SnackBar
                     */
                    @Override
                    public void onPermissionDenied() {
                        Timber.e("Location permission are denied");
                        requestLocationPermissions();
                    }

                    /**
                     * Show a message
                     */
                    @Override
                    public void onNotAvailableLocation() {
                        Timber.e("The user is not in a work zone");
                        HomeFragment.super.onErrorOccurred(R.string.not_correct_location_title, R.string.not_correct_location_message);

                    }


                    /**
                     * If all is right, the permission has granted and the user is inside a work zone
                     *
                     * @param workZoneModel is if of the work zone where the user is
                     */
                    @Override
                    public void onAvailableLotion(WorkZoneModel workZoneModel) {
                        Timber.i("The user is correctly inside a work zone");
                        mainViewModel.changeLastWorkingState(workZoneModel);

                    }


                })).start();


            }
        });


        return binding.getRoot();
    }



    /**
     * Setting up an observer to the mLastWorkingPeriod
     */
    private void setupCentralButtonManager() {

        mainViewModel.getLastWorkingPeriod().observe(getViewLifecycleOwner(), workingPeriod -> {
            try {
                Timber.d("Working period status: %s", workingPeriod.getStatus());
                if (workingPeriod.getStatus() == Constants.INT_NOT_INIT_STATUS) {
                    // The user is working
                    Timber.d("Update the UI to not working");
                    binding.fragmentHomeStartButton.setText(getString(R.string.home_button_start_message));
                    binding.fragmentHomeButtonMessage.setText(getString(R.string.home_text_view_start_message));

                } else {
                    // It is not
                    Timber.d("Update the UI to working");
                    binding.fragmentHomeStartButton.setText(getString(R.string.home_button_finish_message));
                    binding.fragmentHomeButtonMessage.setText(getString(R.string.home_text_view_finish_message));

                }
            } catch (NullPointerException npe) {
                // It is not working
                Timber.d("Update the UI to not working");
                binding.fragmentHomeStartButton.setText(getString(R.string.home_button_start_message));
                binding.fragmentHomeButtonMessage.setText(getString(R.string.home_text_view_start_message));

            }

        });

    }


    /**
     * Here is the logic for setup the calendar
     */
    private void setupCalendar() {

        // Calendar
        AndroidThreeTen.init(getActivity());
        binding.calendarView.setDayBinder(new DayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay calendarDay) {
                container.getDayText().setText(calendarDay.getDate().getDayOfMonth() + "");
                String dayName =
                        calendarDay.getDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, LOCALE_ES);
                container.getDayNameText().setText(dayName);
            }
        });
        binding.calendarView.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthHeaderViewContainer>() {
            @NonNull
            @Override
            public MonthHeaderViewContainer create(@NonNull View view) {
                return new MonthHeaderViewContainer(view);
            }

            @Override
            public void bind(@NonNull MonthHeaderViewContainer container, @NonNull CalendarMonth calendarMonth) {
                String month =
                        calendarMonth.getYearMonth().getMonth().getDisplayName(TextStyle.FULL, LOCALE_ES);
                String monthYearTxt = String.format("%s de %d", month, calendarMonth.getYear());
                container.getMonthText().setText(monthYearTxt);
                // Week scroll buttons
                container.getLeftMonthButton().setOnClickListener(view -> {
                    Toast.makeText(getActivity(), "<-", Toast.LENGTH_SHORT).show();
                });
            }
        });
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(10);
        YearMonth lastMonth = currentMonth.plusMonths(10);
        DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
        binding.calendarView.setup(firstMonth, lastMonth, firstDayOfWeek);
        binding.calendarView.scrollToMonth(currentMonth);
    }


    /**
     * Method to create a dialog and request the permission about location
     */
    private void requestLocationPermissions() {
        requestPermissions(
                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION},
                1);
    }


}