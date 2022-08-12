package com.test.sockettestclient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

public class LoginFragment extends Fragment {

    MainActivity mainActivity;

    Button btn_save;
    EditText edt_ip, edt_port;
    String ip_text, port_text;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        mainActivity = (MainActivity) getActivity();
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_login, container, false);
//
//        btn_save = rootView.findViewById(R.id.btn_save);
//        edt_ip = rootView.findViewById(R.id.edt_ip);
//        edt_port = rootView.findViewById(R.id.edt_port);
//        ip_text = PreferenceManager.getIpString(getActivity(), "ip");
//        port_text = PreferenceManager.getPortString(getActivity(), "port");
//
//
//        btn_save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.e("msg", "fragmentMsg");
//                Log.e("msg", "" + ip_text.length());
//                if(ip_text.length() == 0 || port_text.length() == 0) {
//                    PreferenceManager.setIpString(getActivity(), "ip", edt_ip.getText().toString());
//                    PreferenceManager.setPortString(getActivity(), "port", edt_port.getText().toString());
//                    Log.e("msg", "저장된 정보 : ip = " + ip_text + " port = " + port_text);
//                    ((MainActivity) getActivity()).destroyFragment();
//                }
//            }
//        });

        return rootView;
    }

}