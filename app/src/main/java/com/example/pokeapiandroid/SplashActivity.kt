package com.example.pokeapiandroid

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val splashDuration = 6000L // Incrementado a 6 segundos exactos según lo solicitado

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val webView3D = findViewById<WebView>(R.id.webView3D)
        
        // Configuración de alto rendimiento para garantizar el renderizado 3D en celulares reales
        webView3D.setBackgroundColor(0x00000000) // Fondo transparente para evitar flash blanco
        webView3D.setLayerType(View.LAYER_TYPE_HARDWARE, null) // FORZAR aceleración por hardware
        
        webView3D.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(false)
            // Permitir ejecución de gráficos complejos
            displayZoomControls = false
            builtInZoomControls = false
        }

        // WebChromeClient es necesario en algunos dispositivos para procesar animaciones JS/CSS pesadas
        webView3D.webChromeClient = WebChromeClient()
        webView3D.webViewClient = WebViewClient()

        // HTML embebido embebiendo un contenedor interactivo con CSS3D y rotación realista de la Pokebola en 3D
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
                <style>
                    body, html {
                        margin: 0; padding: 0; width: 100%; height: 100%;
                        background-color: #121212; display: flex; justify-content: center; align-items: center;
                        overflow: hidden; perspective: 800px;
                    }
                    /* Contenedor Esférico 3D de la Pokebola */
                    .pokeball-3d {
                        width: 180px; height: 180px;
                        position: relative; transform-style: preserve-3d;
                        animation: rotate3D 6s infinite linear;
                    }
                    /* Caras y capas tridimensionales */
                    .sphere-half {
                        position: absolute; width: 100%; height: 100%;
                        border-radius: 50%; transform-style: preserve-3d;
                    }
                    .top-red {
                        background: radial-gradient(circle at 50% 20%, #FF1C1C, #990000);
                        clip-path: inset(0 0 50% 0);
                    }
                    .bottom-white {
                        background: radial-gradient(circle at 50% 80%, #FFFFFF, #A0A0A0);
                        clip-path: inset(50% 0 0 0);
                    }
                    /* Banda central tridimensional */
                    .middle-belt {
                        position: absolute; width: 184px; height: 16px;
                        background: #232323; top: 82px; left: -2px;
                        border-radius: 4px; transform: translateZ(1px);
                    }
                    /* Botón 3D flotante frontal */
                    .center-button {
                        position: absolute; width: 44px; height: 44px;
                        background: #232323; border-radius: 50%;
                        top: 68px; left: 68px; transform: translateZ(92px);
                        display: flex; justify-content: center; align-items: center;
                        box-shadow: 0 4px 10px rgba(0,0,0,0.5);
                    }
                    .center-button::after {
                        content: ''; width: 24px; height: 24px;
                        background: #FFFFFF; border-radius: 50%;
                        box-shadow: inset 0 2px 5px rgba(0,0,0,0.3);
                        animation: pulseGlow 1.5s infinite ease-in-out;
                    }
                    /* Animación de rotación completa en 3 Ejes Espaciales */
                    @keyframes rotate3D {
                        0% { transform: rotateX(-15deg) rotateY(0deg) rotateZ(0deg); }
                        50% { transform: rotateX(15deg) rotateY(180deg) rotateZ(10deg); }
                        100% { transform: rotateX(-15deg) rotateY(360deg) rotateZ(0deg); }
                    }
                    @keyframes pulseGlow {
                        0%, 100% { background-color: #FFFFFF; filter: drop-shadow(0 0 2px #FFF); }
                        50% { background-color: #FFCB05; filter: drop-shadow(0 0 8px #FFCB05); }
                    }
                </style>
            </head>
            <body>
                <div class="pokeball-3d">
                    <div class="sphere-half top-red"></div>
                    <div class="sphere-half bottom-white"></div>
                    <div class="middle-belt"></div>
                    <div class="center-button"></div>
                </div>
            </body>
            </html>
        """.trimIndent()

        webView3D.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)

        // Manejador para dar el salto automático exacto a la pantalla de Pokémon
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }, splashDuration)
    }
}