package com.example.stockportfoliomanager.app;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.stockportfoliomanager.app.data.PortContract;

public class CreatePortFragment extends Fragment {
    private View mRootView;
    Activity mActivity;

    public CreatePortFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_create_port, container, false);

        mActivity = getActivity();

        final Button btnCreatePort = (Button) mRootView.findViewById(R.id.btn_create_new_port);
        final EditText etPortName = (EditText) mRootView.findViewById(R.id.edit_text_port_name);
        final Context context = getContext();

        btnCreatePort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Utilities.hideSoftKeyboard(mActivity);
                if(Utilities.getPortCountByPortName(context, etPortName.getText().toString()) == 0) {
                    Uri portUri = PortContract.PortEntry.buildPortUri();

                    ContentValues portValues = new ContentValues();
                    portValues.put(PortContract.PortEntry.COLUMN_PORT_NAME, etPortName.getText().toString());

                    Uri insertedUri;
                    insertedUri = context.getContentResolver().insert(portUri, portValues);

                    Snackbar.make(mRootView, "Portfolio name " + etPortName.getText().toString() + " successfully created.",
                            Snackbar.LENGTH_LONG).show();

                    etPortName.setText("");
                }
                else{
                    Snackbar.make(mRootView, "Portfolio name " + etPortName.getText().toString() + " already exists. ",
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });

        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getActivity().setTitle(R.string.title_create_port);
    }

    @Override
    public void onResume(){
        super.onResume();
        getActivity().setTitle(R.string.title_create_port);
    }
}
