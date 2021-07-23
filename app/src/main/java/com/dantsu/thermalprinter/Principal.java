package com.dantsu.thermalprinter;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Principal extends AppCompatActivity {

    private Button btn1;
    private Button btn2;
    private Button btn3;
    private Button btn5;
    private Button btn10;
    private Button btn15;
    private Button btn20;
    private Button btn25;
    private Button btn30;
    private Button btn40;
    private Button btn50;
    private Button btn60;
    private String result;
    private String horalocal = "";
    private RelativeLayout loading;
    private TextView kuadra;
    private TextView proximo;
    private TextView kina;
    private TextView keno;
    private TextView doacao;
    private TextView opcoes;
    private TextView dataini;
    private TextView sorteio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn5 = findViewById(R.id.btn5);
        btn10 = findViewById(R.id.btn10);
        btn15 = findViewById(R.id.btn15);
        btn20 = findViewById(R.id.btn20);
        btn25 = findViewById(R.id.btn25);
        btn30 = findViewById(R.id.btn30);
        btn40 = findViewById(R.id.btn40);
        btn50 = findViewById(R.id.btn50);
        btn60 = findViewById(R.id.btn60);

        kuadra = findViewById(R.id.kuadra);
        kina = findViewById(R.id.kina);
        keno = findViewById(R.id.keno);
        doacao = findViewById(R.id.doacao);
        proximo = findViewById(R.id.proximo);

        loading = findViewById(R.id.loading);
        dataini = findViewById(R.id.dataini);
        sorteio = findViewById(R.id.sorteio);

        opcoes = findViewById(R.id.opcoes);

        opcoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(Principal.this, MenuPri.class);
                startActivity(it);
            }
        });

        new getpeep().execute();

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RotasAPI.numerocartelas = "1";
                Intent it = new Intent(Principal.this, MainActivity.class);
                startActivity(it);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RotasAPI.numerocartelas = "2";
                Intent it = new Intent(Principal.this, MainActivity.class);
                startActivity(it);
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RotasAPI.numerocartelas = "3";
                Intent it = new Intent(Principal.this, MainActivity.class);
                startActivity(it);
            }
        });

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RotasAPI.numerocartelas = "5";
                Intent it = new Intent(Principal.this, MainActivity.class);
                startActivity(it);
            }
        });

        btn10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RotasAPI.numerocartelas = "10";
                Intent it = new Intent(Principal.this, MainActivity.class);
                startActivity(it);
            }
        });

        btn15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RotasAPI.numerocartelas = "15";
                Intent it = new Intent(Principal.this, MainActivity.class);
                startActivity(it);
            }
        });

        btn20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RotasAPI.numerocartelas = "20";
                Intent it = new Intent(Principal.this, MainActivity.class);
                startActivity(it);
            }
        });

        btn25.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RotasAPI.numerocartelas = "25";
                Intent it = new Intent(Principal.this, MainActivity.class);
                startActivity(it);
            }
        });

        btn30.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RotasAPI.numerocartelas = "30";
                Intent it = new Intent(Principal.this, MainActivity.class);
                startActivity(it);
            }
        });

        btn40.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RotasAPI.numerocartelas = "40";
                Intent it = new Intent(Principal.this, MainActivity.class);
                startActivity(it);
            }
        });

        btn50.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RotasAPI.numerocartelas = "50";
                Intent it = new Intent(Principal.this, MainActivity.class);
                startActivity(it);
            }
        });

        btn60.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RotasAPI.numerocartelas = "60";
                Intent it = new Intent(Principal.this, MainActivity.class);
                startActivity(it);
            }
        });
    }

    public class getpeep extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //Your server URL
            String url = RotasAPI.peep;
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
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'POST' request to URL : " + url);
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

            JSONObject jsonObject = null;
            String Kuadra = "";
            String Kina = "";
            String Keno = "";
            String Doacao = "";
            String Dataini = "";
            String Sorteio = "";

            try {
                jsonObject = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (jsonObject != null) {
                try {
                    Kuadra = jsonObject.getString("premio1");
                    Kina = jsonObject.getString("premio2");
                    Keno = jsonObject.getString("premio3");
                    Doacao = jsonObject.getString("precoCupom");
                    Dataini = jsonObject.getString("dtInicio");
                    Sorteio = jsonObject.getString("sorteio");
                    horalocal = jsonObject.getString("horaLocal");
                    RotasAPI.codigo = jsonObject.getString("sorteio");
                    RotasAPI.valor = jsonObject.getString("precoCupom");
                    System.out.println(Kuadra);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //2021-07-22 13:25:00

            String[] splithoralocal = horalocal.split(" ");
            String[] splithoralocal1 = splithoralocal[0].split("-");
            String[] splithoralocal2 = splithoralocal[1].split(":");

            int horalocalcalc = Integer.parseInt("" + splithoralocal2[0] + splithoralocal2[1]);



            String[] splitdate = Dataini.split(" ");
            String[] splitdate1 = splitdate[0].split("-");
            String[] splitdate2 = splitdate[1].split(":");

            int datecalc = Integer.parseInt("" + splithoralocal2[0] + splithoralocal2[1]);

            String printdate = splitdate1[2] + "/" + splitdate1[1] + "/" + splitdate1[0] + " " + splitdate2[0] + ":" + splitdate2[1];

            kuadra.setText(Kuadra + "." + RotasAPI.casasdecimais);
            kina.setText(Kina + "." + RotasAPI.casasdecimais);
            keno.setText(Keno + "." + RotasAPI.casasdecimais);
            doacao.setText(Doacao + "." + RotasAPI.casasdecimais);
            dataini.setText(printdate);
            sorteio.setText("SORTEIO:" + Sorteio);

            int total = horalocalcalc - datecalc;

            proximo.setText("proximo sorteio em:" + total);


            loading.setVisibility(View.GONE);

        }
    }


}