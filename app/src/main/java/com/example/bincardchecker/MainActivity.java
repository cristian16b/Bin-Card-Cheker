package com.example.bincardchecker;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Objects;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ImageButton botonBuscarTarjeta;
    private EditText nroTarjetaEdit;
    private TextView tipoTarjeta;
    private TextView nombreTarjeta;
    private TextView nombrePais;
    private TextView monedaPais;
    private TextView nombreBanco;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        botonBuscarTarjeta = (ImageButton) findViewById(R.id.botonBuscar);
        nroTarjetaEdit = (EditText) findViewById(R.id.nroTarjetaText);

        tipoTarjeta = (TextView) findViewById(R.id.tipoTarjeta);
        nombreTarjeta = (TextView) findViewById(R.id.sistemaPago);
        nombrePais = (TextView) findViewById(R.id.pais);
        monedaPais = (TextView) findViewById(R.id.nombreMoneda);
        nombreBanco = (TextView) findViewById(R.id.nombreBanco);
    }

    public void buscarDatosTarjeta(View view) throws IOException {
        String nroTarjetaTexto=this.nroTarjetaEdit.getText().toString();

        if(nroTarjetaTexto.length()== 0){
            Toast.makeText(this,R.string.msj_nro_vacio,Toast.LENGTH_LONG).show();
            return;
        }
        else
        {
            try
            {
                Toast.makeText(this,R.string.msj_consultando_api,Toast.LENGTH_SHORT).show();
                blanquearDatosTarjeta();
                postHttpResponse(nroTarjetaTexto);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void postHttpResponse(String nroIngresado) throws IOException {

        String urlBase = "https://lookup.binlist.net/";
        String urlSolicitud = urlBase + nroIngresado;
        Request request = new Request.Builder()
                .url(urlSolicitud)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                Log.i("ONFAILURE","");
                mostrarErrores(getString(R.string.msj_fallo_conex_defaul));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try
                {
                    //Log.i("codigoResp", String.valueOf(response.code()));

                    // Si la api retorna un 200
                    if(response.isSuccessful()){
                        String responseData = Objects.requireNonNull(response.body()).string();

                        JSONObject jsonObject = new JSONObject(responseData);
                        Log.i("ONRESPONSE",jsonObject.toString());

                        mostrarDatosTarjeta(jsonObject);
                    }
                    else
                    {
                        switch(response.code()){
                            case 401: //unauthorized
                                mostrarErrores(getString(R.string.msj_fallo_conex));
                                break;
                            case 403: //unauthorized
                                mostrarErrores(getString(R.string.msj_fallo_conex));
                                break;
                            default:
                                mostrarErrores(getString(R.string.msj_fallo_conex_defaul));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // muestro un mensaje de error que se le pasa como parametro.
    private void mostrarErrores(final String errorMensaje)
    {
        // Run view-related code back on the main thread

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),errorMensaje,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarDatosTarjeta(JSONObject jsonObject){
        try
        {
            Log.i("resultado",jsonObject.getString("scheme"));
            String nombreTarj = jsonObject.getString("scheme");
            String tipoTarj = jsonObject.getString("type");

            JSONObject pais = jsonObject.getJSONObject("country");
            String paisNombre = pais.getString("name");
            String paisMoneda = pais.getString("currency");

            /*
            Log.i("resultado",jsonObject.getString("type"));
            Log.i("resultado",pais.getString("name"));
            Log.i("resultado",pais.getString("currency"));
            */

            JSONObject banco = jsonObject.getJSONObject("bank");
            String bancoNombre = banco.getString("name");
            //Log.i("resultado",banco.getString("name"));


            nombreTarjeta.setText(nombreTarj);
            tipoTarjeta.setText(tipoTarj);
            nombreBanco.setText(bancoNombre);
            nombrePais.setText(paisNombre);
            monedaPais.setText(paisMoneda);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void blanquearDatosTarjeta(){

        nombreTarjeta.setText("");
        tipoTarjeta.setText("");
        nombreBanco.setText("");
        nombrePais.setText("");
        monedaPais.setText("");
    }
}
