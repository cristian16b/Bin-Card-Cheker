package com.example.bincardchecker;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        botonBuscarTarjeta = (ImageButton) findViewById(R.id.botonBuscar);
        nroTarjetaEdit = (EditText) findViewById(R.id.nroTarjetaText);

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
                postHttpResponse(nroTarjetaTexto);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void postHttpResponse(String nroIngresado) throws IOException {

        String urlBase = "https://lookup.binlist.net/";
        String urlSolicitud = urlBase + nroIngresado;
        Log.i("urlsolicitud",urlSolicitud);
        Request request = new Request.Builder()
                .url(urlSolicitud)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {


            // tener en cuenta
            // https://stackoverflow.com/questions/36786132/okhttp-response-status-code-in-onfailure-method
            // https://github.com/emiliano-sangoi/app-turnos/blob/master/app/src/main/java/com/example/emiliano/appturnos/backend/APITurnosManager.java

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
                    // Si la api retorna un 200
                    if(response.isSuccessful()){
                        String responseData = Objects.requireNonNull(response.body()).string();

                        JSONObject jsonObject = new JSONObject(responseData);
                        Log.i("ONRESPONSE",jsonObject.toString());

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
}
