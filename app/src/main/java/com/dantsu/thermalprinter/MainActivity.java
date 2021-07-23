package com.dantsu.thermalprinter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.tcp.TcpConnection;
import com.dantsu.escposprinter.connection.usb.UsbConnection;
import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.dantsu.thermalprinter.async.AsyncBluetoothEscPosPrint;
import com.dantsu.thermalprinter.async.AsyncEscPosPrinter;
import com.dantsu.thermalprinter.async.AsyncTcpEscPosPrint;
import com.dantsu.thermalprinter.async.AsyncUsbEscPosPrint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private String result;
    private String vendedor;
    private JSONArray jsonArray = null;
    private TextView quantidade;
    private TextView valor;
    private RelativeLayout loading;

    private String texto = "";
    private Button imprimir;
    private Button desistir;

    private ArrayList<String> linhas = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loading = findViewById(R.id.loading);

        quantidade = findViewById(R.id.quantidade);
        valor = findViewById(R.id.valor);

        int valor2 = (Integer.parseInt(RotasAPI.valor) * Integer.parseInt(RotasAPI.numerocartelas));

        quantidade.setText("SERÁ IMPRIMIDO " + RotasAPI.numerocartelas);
        valor.setText("Valor " + valor2);

        imprimir = (Button) this.findViewById(R.id.imprimir);
        desistir = (Button) this.findViewById(R.id.desistir);
        imprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.setVisibility(View.VISIBLE);
               new sale().execute();
            }
        });

        desistir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              finish();
            }
        });

    }


    /*==============================================================================================
    ======================================BLUETOOTH PART============================================
    ==============================================================================================*/

    public static final int PERMISSION_BLUETOOTH = 1;

    public void printBluetooth() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, MainActivity.PERMISSION_BLUETOOTH);
        } else {
            // this.printIt(BluetoothPrintersConnections.selectFirstPaired());
            new AsyncBluetoothEscPosPrint(this).execute(this.getAsyncEscPosPrinter(null));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case MainActivity.PERMISSION_BLUETOOTH:
                    this.printBluetooth();
                    break;
            }
        }
    }


    /*==============================================================================================
    ===========================================USB PART=============================================
    ==============================================================================================*/

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MainActivity.ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                    UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbManager != null && usbDevice != null) {
                            // printIt(new UsbConnection(usbManager, usbDevice));
                            new AsyncUsbEscPosPrint(context)
                                    .execute(getAsyncEscPosPrinter(new UsbConnection(usbManager, usbDevice)));
                        }
                    }
                }
            }
        }
    };

    public void printUsb() {
        UsbConnection usbConnection = UsbPrintersConnections.selectFirstConnected(this);
        UsbManager usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);

        if (usbConnection == null || usbManager == null) {
            new AlertDialog.Builder(this)
                    .setTitle("USB Connection")
                    .setMessage("No USB printer found.")
                    .show();
            return;
        }

        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(MainActivity.ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_USB_PERMISSION);
        registerReceiver(this.usbReceiver, filter);
        usbManager.requestPermission(usbConnection.getDevice(), permissionIntent);
    }

    /*==============================================================================================
    =========================================TCP PART===============================================
    ==============================================================================================*/

    public void printTcp() {
        final EditText ipAddress = (EditText) this.findViewById(R.id.edittext_tcp_ip);
        final EditText portAddress = (EditText) this.findViewById(R.id.edittext_tcp_port);

        try {
            // this.printIt(new TcpConnection(ipAddress.getText().toString(), Integer.parseInt(portAddress.getText().toString())));
            new AsyncTcpEscPosPrint(this)
                    .execute(this.getAsyncEscPosPrinter(new TcpConnection(ipAddress.getText().toString(), Integer.parseInt(portAddress.getText().toString()))));
        } catch (NumberFormatException e) {
            new AlertDialog.Builder(this)
                    .setTitle("Invalid TCP port address")
                    .setMessage("Port field must be a number.")
                    .show();
            e.printStackTrace();
        }
    }

    /*==============================================================================================
    ===================================ESC/POS PRINTER PART=========================================
    ==============================================================================================*/


    /**
     * Synchronous printing
     */
    @SuppressLint("SimpleDateFormat")
    public void printIt(DeviceConnection printerConnection) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("'on' yyyy-MM-dd 'at' HH:mm:ss");
            EscPosPrinter printer = new EscPosPrinter(printerConnection, 203, 48f, 32);
            printer
                    .printFormattedText(
                            "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.logo, DisplayMetrics.DENSITY_MEDIUM)) + "</img>\n" +
                                    "[L]\n" +
                                    "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
                                    "[C]<font size='small'>" + format.format(new Date()) + "</font>\n" +
                                    "[L]\n" +
                                    "[C]================================\n" +
                                    "[L]\n" +
                                    "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99e\n" +
                                    "[L]  + Size : S\n" +
                                    "[L]\n" +
                                    "[L]<b>AWESOME HAT</b>[R]24.99e\n" +
                                    "[L]  + Size : 57/58\n" +
                                    "[L]\n" +
                                    "[C]--------------------------------\n" +
                                    "[R]TOTAL PRICE :[R]34.98e\n" +
                                    "[R]TAX :[R]4.23e\n" +
                                    "[L]\n" +
                                    "[C]================================\n" +
                                    "[L]\n" +
                                    "[L]<font size='tall'>Customer :</font>\n" +
                                    "[L]Raymond DUPONT\n" +
                                    "[L]5 rue des girafes\n" +
                                    "[L]31547 PERPETES\n" +
                                    "[L]Tel : +33801201456\n" +
                                    "[L]\n" +
                                    "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
                                    "[C]<qrcode size='20'>http://www.developpeur-web.dantsu.com/</qrcode>"
                    );
        } catch (EscPosConnectionException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this)
                    .setTitle("Broken connection")
                    .setMessage(e.getMessage())
                    .show();
        } catch (EscPosParserException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this)
                    .setTitle("Invalid formatted text")
                    .setMessage(e.getMessage())
                    .show();
        } catch (EscPosEncodingException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this)
                    .setTitle("Bad selected encoding")
                    .setMessage(e.getMessage())
                    .show();
        } catch (EscPosBarcodeException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this)
                    .setTitle("Invalid barcode")
                    .setMessage(e.getMessage())
                    .show();
        }
    }

    /**
     * Asynchronous printing
     */
    @SuppressLint("SimpleDateFormat")
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        SimpleDateFormat format = new SimpleDateFormat("'on' yyyy-MM-dd 'at' HH:mm:ss");
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 16);

        return printer.setTextToPrint(
                "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.logo, DisplayMetrics.DENSITY_LOW)) + "</img>\n" +
                        "[L]\n" +
                        "[C]<u><font size='52'>NOVA VENDA DE CARTELAS</font></u>\n" +
                        "[C]<font size='small'>" + format.format(new Date()) + "</font>\n" +
                        "[C]<u><font size='36'>PATATI BINGO</font></u>\n" +
                        "[C]<u><font size='36'>VENDEDOR - LUAN</font></u>\n" +
                        "[L]\n" +
                        "[C]================================\n" +
                        "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
                        "[L]\n" +
                        "[C]<u><font size='36'>NOME DO JOGADOR: Luan Phellip</font></u>\n" +
                        "[C]<u><font size='36'>TELEFONE DO JOGADOR: 21983336614</font></u>\n" +
                        "[C]<u><font size='36'>RODADA: TESTE</font></u>\n" +
                        "[C]<u><font size='36'>NUMERO DE CARTELA: 01</font></u>\n" +
                        "[C]<u><font size='36'>CODIGO DA CARTELA: TESTE</font></u>\n" +
                        "[C]<u><font size='36'>"+texto+"</font></u>\n" +
                        "[C]<u><b><font size='36'>52  69  48  87  01</font></u>\n" +
                        "[C]<u><b><font size='36'>08  02  49  21  58</font></u>\n" +
                        "[C]<u><b><font size='36'>66  33  31  28  87</font></u>\n"
        );


    }


    public class sale extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            //Your server URL
            String parametros = "qtd=" + RotasAPI.numerocartelas + "&tk=pea!peep&rod=" + RotasAPI.codigo;
            String url = RotasAPI.sale + parametros;
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

            String Status = "";
            String Menssage = "";

            try {
                jsonObject = new JSONObject(result);
                //jsonArray = jsonObject.getJSONArray("cartelas");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (jsonObject != null) {
                try {
                    Status = jsonObject.getString("stOper");
                    vendedor = jsonObject.getString("codigo");
                    System.out.println(Status);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (Status.equals("0")) {
                new print().execute();
            } else {
                loading.setVisibility(View.GONE);
                androidx.appcompat.app.AlertDialog.Builder dlg = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
                try {
                    dlg.setMessage(jsonObject.getString("descricao"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dlg.setNeutralButton("Ok", null);
                dlg.show();
            }


        }
    }

    public class print extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            //Your server URL
            String parametros = "vd=" + vendedor + "&tk=pea!peep&rg=1";
            String url = RotasAPI.print + parametros;
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

            String Status = "";

            try {
                jsonObject = new JSONObject(result);
                jsonArray = jsonObject.getJSONArray("cartelas");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (jsonObject != null) {
                try {
                    Status = jsonObject.getString("stOper");
                    System.out.println(Status);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (Status.equals("0")) {
                loading.setVisibility(View.GONE);
                texto = read();
                System.out.println(texto);
                imprimir.setText("RE-IMPRIMIR");
                desistir.setText("SAIR");
                printBluetooth();
            }

        }
    }

    private String read() {
        String result = "";


        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                //result = "CARTELA: " + jsonObject.get("cupom") + "\n------------------\n";

                //System.out.println(jsonObject.get("cupomL1").toString().substring(1, jsonObject.get("cupomL1").toString().length() - 1));
                String[] splitL1 = jsonObject.get("cupomL1").toString().substring(1, jsonObject.get("cupomL1").toString().length() - 1).split(",");
                String[] splitL2 = jsonObject.get("cupomL2").toString().substring(1, jsonObject.get("cupomL2").toString().length() - 1).split(",");
                String[] splitL3 = jsonObject.get("cupomL3").toString().substring(1, jsonObject.get("cupomL3").toString().length() - 1).split(",");

                String linha1 = addzero(splitL1[0].substring(1, splitL1[0].length() - 1)) + " | "
                        + addzero(splitL1[1].substring(1, splitL1[1].length() - 1)) + " | "
                        + addzero(splitL1[2].substring(1, splitL1[2].length() - 1) )+ " | "
                        + addzero(splitL1[3].substring(1, splitL1[3].length() - 1)) + " | "
                        + addzero(splitL1[4].substring(1, splitL1[4].length() - 1));

                String linha2 = addzero(splitL2[0].substring(1, splitL2[0].length() - 1)) + " | "
                        + addzero(splitL2[1].substring(1, splitL2[1].length() - 1)) + " | "
                        + addzero(splitL2[2].substring(1, splitL2[2].length() - 1)) + " | "
                        + addzero(splitL2[3].substring(1, splitL2[3].length() - 1)) + " | "
                        + addzero(splitL2[4].substring(1, splitL2[4].length() - 1));

                String linha3 = addzero(splitL3[0].substring(1, splitL3[0].length() - 1)) + " | "
                        + addzero(splitL3[1].substring(1, splitL3[1].length() - 1)) + " | "
                        + addzero(splitL3[2].substring(1, splitL3[2].length() - 1)) + " | "
                        + addzero(splitL3[3].substring(1, splitL3[3].length() - 1)) + " | "
                        + addzero(splitL3[4].substring(1, splitL3[4].length() - 1));

                String cupon = "CUPOM: " +  jsonObject.getString("cupom");
                String linhatraco = "----------------\n";

                //
                result = result + cupon + "\n" + linhatraco + linha1 + "\n" + linha2 + "\n" + linha3 + "\n";

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }



        return result;
    }

    private String addzero(String charactrer){

        if (charactrer.length() == 1){
            charactrer = "0" + charactrer;
        }

        return charactrer;
    }
}
