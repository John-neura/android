package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // 1. Declaración de las Vistas (Elementos de la UI)
    // Estas variables representarán los elementos que definiste en tu XML.
    private EditText productoEditText;
    private EditText cantidadEditText;
    private Button guardarButton;
    private TextView inventarioTextView;

    // 2. Declaración de variables para la persistencia de datos
    // SharedPreferences es la forma de Android para guardar datos simples de forma permanente.
    // Gson es una librería para convertir objetos Java a texto JSON y viceversa.
    private SharedPreferences sharedPreferences;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // 3. Enlace de la Lógica (Java) con el Diseño (XML)
        // Esta línea es crucial. Le dice a la actividad qué archivo de diseño XML debe mostrar.
        // Aquí se usa R.layout.activityPrincipal para enlazarlo con tu archivo XML.
        setContentView(R.layout.activityPrincipal);

        // Se mantiene el padding para las barras del sistema (parte del código original).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 4. Inicialización de las Vistas
        // Enlazamos las variables declaradas arriba con los elementos reales de la interfaz
        // usando sus IDs definidos en el archivo XML (ej. @+id/producto_edittext).
        productoEditText = findViewById(R.id.producto_edittext);
        cantidadEditText = findViewById(R.id.cantidad_edittext);
        guardarButton = findViewById(R.id.guardar_button);
        inventarioTextView = findViewById(R.id.inventario_textview);

        // 5. Inicialización de herramientas de persistencia
        // Se inicializa SharedPreferences para guardar los datos. El primer parámetro es el nombre del archivo.
        sharedPreferences = getSharedPreferences("inventario_pref", MODE_PRIVATE);
        // Se inicializa Gson.
        gson = new Gson();

        // 6. Configuración de un 'Listener' para el Botón
        // El setOnClickListener "escucha" los toques en el botón. Cuando se detecta un toque,
        // se llama a la función guardarProducto().
        guardarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarProducto();
            }
        });

        // 7. Cargar y mostrar el inventario al iniciar la aplicación
        // Llama a esta función para que, cuando la app se inicie, se muestren los datos guardados.
        mostrarInventario();
    }

    // --- Métodos de Lógica de la Aplicación ---

    private void guardarProducto() {
        // Obtenemos el texto de los campos de entrada.
        String producto = productoEditText.getText().toString().trim();
        String cantidadStr = cantidadEditText.getText().toString().trim();

        // Verificamos que los campos no estén vacíos. Si lo están, mostramos un mensaje.
        if (producto.isEmpty() || cantidadStr.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa un nombre y una cantidad.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convertimos la cantidad de texto a un número entero.
        int cantidad = Integer.parseInt(cantidadStr);
        // Obtenemos el inventario guardado. Si no existe, crea un nuevo mapa vacío.
        Map<String, Integer> inventario = obtenerInventarioGuardado();

        // Actualizamos o añadimos el producto al inventario.
        if (inventario.containsKey(producto)) {
            // Si el producto ya existe, se suma la nueva cantidad.
            inventario.put(producto, inventario.get(producto) + cantidad);
        } else {
            // Si no existe, se añade con la cantidad ingresada.
            inventario.put(producto, cantidad);
        }

        // Guardamos el mapa de inventario actualizado.
        guardarInventario(inventario);

        // Mostramos un mensaje temporal y limpiamos los campos de entrada.
        Toast.makeText(this, "Producto añadido: " + producto, Toast.LENGTH_SHORT).show();
        productoEditText.setText("");
        cantidadEditText.setText("");

        // Actualizamos la vista para mostrar los nuevos datos.
        mostrarInventario();
    }

    private Map<String, Integer> obtenerInventarioGuardado() {
        // Obtenemos la cadena JSON de SharedPreferences. Si no existe, el valor por defecto es "{}".
        String json = sharedPreferences.getString("inventario_json", "{}");
        // Usamos Gson para convertir la cadena JSON de vuelta a un mapa de String e Integer.
        Type type = new TypeToken<Map<String, Integer>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void guardarInventario(Map<String, Integer> inventario) {
        // Convertimos el mapa de Java a una cadena JSON.
        String json = gson.toJson(inventario);
        // Obtenemos un editor para modificar los datos de SharedPreferences.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Guardamos la cadena JSON en SharedPreferences con la clave "inventario_json".
        editor.putString("inventario_json", json);
        // Aplicamos los cambios.
        editor.apply();
    }

    private void mostrarInventario() {
        // Obtenemos el inventario actualizado.
        Map<String, Integer> inventario = obtenerInventarioGuardado();
        // Usamos un StringBuilder para construir el texto que se mostrará.
        StringBuilder sb = new StringBuilder();

        if (inventario.isEmpty()) {
            sb.append("No hay productos en el inventario.");
        } else {
            // Iteramos sobre cada par clave-valor del mapa.
            for (Map.Entry<String, Integer> entry : inventario.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        // Asignamos el texto final al TextView.
        inventarioTextView.setText(sb.toString());
    }
}