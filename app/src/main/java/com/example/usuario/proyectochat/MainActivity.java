package com.example.usuario.proyectochat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import com.example.usuario.proyectochat.chatterbotapi.ChatterBot;
import com.example.usuario.proyectochat.chatterbotapi.ChatterBotFactory;
import com.example.usuario.proyectochat.chatterbotapi.ChatterBotSession;
import com.example.usuario.proyectochat.chatterbotapi.ChatterBotThought;
import com.example.usuario.proyectochat.chatterbotapi.ChatterBotType;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private TextToSpeech tts;
    private EditText et;
    private TextView tv;
    private boolean success;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        inicio();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leer();
            }
        });
    }
    public void inicio(){
        et = (EditText) findViewById(R.id.editText);
        tv = (TextView) findViewById(R.id.textView);
        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, 0);

    }
    public void pasaTexto(View v){
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-ES");
            i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Hablaahora");
            i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,3000);
            startActivityForResult(i, 2);
    }
    public void voz(String frase){
        if(success) {
            tts.setLanguage(new Locale("es", "ES"));
            tts.setPitch((float) 1);
            tts.speak(frase, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    public void leer(){
        if(success) {
            String frase=et.getText().toString();
            voz(frase);

            tv.append("\nYO: "+frase );
            et.setText("");
            Tarea t=new Tarea();
            t.execute(frase);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== 0) {
            if(resultCode== TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts= new TextToSpeech(this, this);
                tts.setLanguage(Locale.getDefault());
            } else{
                Intent intent= new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
            }
        }
        if(requestCode== 1) {
            ArrayList<String> textos = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            tv.setText(textos.get(0));
            for (String s : textos) {
                tv.append(s+"\n");
            }
        }
        if(requestCode==2){
            ArrayList<String> textos = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            tv.setText("YO: "+textos.get(0));

            Tarea t=new Tarea();
            t.execute(textos.get(0));
        }
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            success = true;
        } else{
            success = false;
        }
    }
    /**/
    public class Tarea extends AsyncTask<String,Integer,String> {

        @Override
        protected String doInBackground(String... params) {
            ChatterBotFactory factory = new ChatterBotFactory();

            ChatterBot bot1 = null;
            try {
                bot1 = factory.create(ChatterBotType.CLEVERBOT);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ChatterBotSession bot1session = bot1.createSession();
            String s = params[0]+"";
            String respuesta="";
            try {
                respuesta = bot1session.think(s);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return respuesta;
        }

        @Override
        protected void onPostExecute(String respuesta) {
            tv.append("\nBOT: "+respuesta);
            voz(respuesta);
        }
    }
}
