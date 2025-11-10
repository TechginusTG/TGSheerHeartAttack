package com.github.TechGinus.TGSheerHeartAttack

import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import android.view.View


class SerialBluetooth {
    public fun handleConnect(view: View) {
        Snackbar.make(view, "Conectado", Snackbar.LENGTH_SHORT).show()
    }
}