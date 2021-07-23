package com.dantsu.thermalprinter;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Login extends AppCompatActivity {

    private Button entrar;
    private EditText email;
    private EditText senha;
    private RelativeLayout loading;
    private String emails;
    private String senhas;
    private String status;
    private String result;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        entrar = findViewById(R.id.entrar);
        email = findViewById(R.id.email);
        senha = findViewById(R.id.senha);
        loading = findViewById(R.id.loading);

        email.setText("teste");
        emails = "teste";
        senhas = "102030";
        senha.setText("102030");

        isReadStoragePermissionGranted();

        entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.setVisibility(View.VISIBLE);
                checkCampos();
            }
        });

    }

    private void checkCampos() {
        if (TextUtils.isEmpty(email.getText().toString())) {
            email.setError("Digite seu email para continuar");
            loading.setVisibility(View.GONE);
            return;
        }

        if (TextUtils.isEmpty(senha.getText().toString())) {
            senha.setError("Digite seu email para continuar");
            loading.setVisibility(View.GONE);
            return;
        }

        emails = email.getText().toString();
        senhas = senha.getText().toString();

        //new LoginV2(Login.this).execute();

        new loginV3().execute();
        //Intent it = new Intent(Login.this, MenuPro.class);
        //startActivity(it);
    }

    public class loginV3 extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            //Your server URL
            String urlParameters = "id=" + emails + "&si=" + senhas + "&rr=" + "102";
            String url = RotasAPI.dominio + urlParameters;
            URL obj = null;
            try {
                obj = new URL(url);
                HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

                //add reuqest header
                con.setRequestMethod("GET");
                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                //Request Parameters you want to send


                // Send post request
                con.setDoOutput(true);// Should be part of code only for .Net web-services else no need for PHP
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'POST' request to URL : " + url);
                System.out.println("Post parameters : " + urlParameters);
                System.out.println("Response Code : " + responseCode);


                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                //print result
                System.out.println(response.toString());
                result = response.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            String[] status2 = result.split("[|]");
            System.out.println(status2[0]);

            if (status2[8].equals("0")) {
                RotasAPI.casasdecimais = "00";
            } else if (status2[8].equals("1")) {
                RotasAPI.casasdecimais = "000";
            } else if (status2[8].equals("2")) {
                RotasAPI.casasdecimais = "";
            }


            if (status2[0].trim().equals("\uFEFFOKSI")) {
                loading.setVisibility(View.GONE);
                Toast.makeText(Login.this, "Logado com sucesso", Toast.LENGTH_SHORT).show();
                Intent it = new Intent(Login.this, Principal.class);
                startActivity(it);
                finish();
            } else {
                loading.setVisibility(View.GONE);
                AlertDialog.Builder dlg = new AlertDialog.Builder(Login.this);
                dlg.setMessage("Login ou senha incorretos");
                dlg.setNeutralButton("Ok", null);
                dlg.show();
            }


        }
    }


    public boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 3);
                return false;
            }
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2:
                if (grantResults[0] == 0) {
                    Toast.makeText(this, "Permissão concedida!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Toast.makeText(this, "Permissão negada!", Toast.LENGTH_SHORT).show();
                    return;
                }
            case 3:
                if (grantResults[0] != 0) {
                    Toast.makeText(this, "Permissão concedida!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Toast.makeText(this, "Permissão negada!", Toast.LENGTH_SHORT).show();
                    return;
                }
            default:
                return;
        }
    }
}