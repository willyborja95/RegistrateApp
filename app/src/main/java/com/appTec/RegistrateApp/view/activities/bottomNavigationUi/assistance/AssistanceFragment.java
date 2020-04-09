package com.appTec.RegistrateApp.view.activities.bottomNavigationUi.assistance;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.appTec.RegistrateApp.R;
import com.appTec.RegistrateApp.models.Assistance;
import com.appTec.RegistrateApp.repository.webServices.ApiClient;
import com.appTec.RegistrateApp.repository.webServices.interfaces.AssistanceRetrofitInterface;
import com.appTec.RegistrateApp.util.Constants;
import com.appTec.RegistrateApp.view.adapters.AssistanceListAdapter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AssistanceFragment extends Fragment {

    ArrayList<Assistance> lstAssistances;
    ListView lvAssistance;
    EditText txtAssistanceSelectedDate;
    TextView lblNotAssistanceFound;
    ImageButton imgBtnSearchAssistance;
    SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
    Calendar selectedDate = Calendar.getInstance();
    String strSelectedDate;
    ProgressDialog progressDialog;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assistance, container, false);
        progressDialog = new ProgressDialog(getContext());
        lstAssistances = new ArrayList<Assistance>();
        lvAssistance = (ListView) view.findViewById(R.id.lvAssistance);
        txtAssistanceSelectedDate = (EditText) view.findViewById(R.id.txtAssistanceSelectedDate);
        imgBtnSearchAssistance = (ImageButton) view.findViewById(R.id.imgBtnSearchAssistance);
        lblNotAssistanceFound = (TextView) view.findViewById(R.id.lblNotAssistanceFound);
        lblNotAssistanceFound.setVisibility(view.GONE);
        txtAssistanceSelectedDate.setOnClickListener(new View.OnClickListener() {
            Calendar selectedDate = Calendar.getInstance();
            @Override
            public void onClick(View v) {
                DatePickerDialog dpDate = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        selectedDate.set(year, monthOfYear, dayOfMonth);
                        strSelectedDate = dateformat.format(selectedDate.getTime());
                        txtAssistanceSelectedDate.setText(strSelectedDate);
                    }
                }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
                dpDate.show();
            }
        });

        imgBtnSearchAssistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssistanceRetrofitInterface assistanceRetrofitInterface = ApiClient.getClient().create(AssistanceRetrofitInterface.class);
                Call<JsonObject> assistanceCall = assistanceRetrofitInterface.get(getUserToken(), strSelectedDate);
                showAssistanceProgressDialog(Constants.UPDATING_CHANGES);
                assistanceCall.enqueue(new Callback<JsonObject>() {

                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        hideAssistanceProgressDialog();
                        lstAssistances.clear();
                        JsonArray assistanceListJson = response.body().getAsJsonArray("data");

                        if (assistanceListJson.size() == 0) {
                            lblNotAssistanceFound.setVisibility(view.VISIBLE);
                        }else{
                            lblNotAssistanceFound.setVisibility(view.GONE);
                            for (int i = 0; i < assistanceListJson.size(); i++) {
                                Calendar assistanceDate = Calendar.getInstance();
                                JsonObject assistanceJson = assistanceListJson.get(i).getAsJsonObject();
                                String strDateTime = assistanceJson.get("formateddate").getAsString();
                                String strDate = strDateTime.split(" ")[0];
                                String strTime = strDateTime.split(" ")[1];
                                SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                try {
                                    assistanceDate.setTime(dateformat.parse(strDateTime));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                String event = assistanceJson.get("evento").getAsString();
                                Assistance assistance = new Assistance(event, assistanceDate);
                                lstAssistances.add(assistance);
                            }
                            updateListView();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        hideAssistanceProgressDialog();
                        showConectionErrorMessage();
                    }
                });
            }
        });
        return view;
    }

    private void updateListView() {
        lvAssistance.setAdapter(new AssistanceListAdapter(getContext(), lstAssistances));
    }

    //Shared preferences methods
    public String getUserToken(){
        SharedPreferences sharedPref = getContext().getSharedPreferences(
                Constants.SHARED_PREFERENCES_GLOBAL, Context.MODE_PRIVATE);
        return sharedPref.getString(Constants.USER_TOKEN,
                "");
    }

    //Dialogs
    public void showAssistanceProgressDialog(String message) {
        progressDialog.setMessage(message);
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
    }

    public void hideAssistanceProgressDialog() {
        progressDialog.dismiss();
    }

    public void showConectionErrorMessage() {
        showDialog("Error de conexión", "Al parecer no hay conexión a Internet.");
    }

    public void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext() )
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
        alertDialog.show();
    }
}