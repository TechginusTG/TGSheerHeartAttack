package com.github.TechGinus.TGSheerHeartAttack

import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar


class SerialBluetooth(private val activity: AppCompatActivity) {

    fun set_listener() {
        val goBtn: Button = activity.findViewById(R.id.go_btn)
        goBtn.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.isPressed = true
                    pressingGo(view)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.isPressed = false
                    releasedGo(view)
                    true
                }
                else -> false
            }
        }

        val backBtn: Button = activity.findViewById(R.id.back_btn)
        backBtn.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.isPressed = true
                    pressingBack(view)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.isPressed = false
                    releasedBack(view)
                    true
                }
                else -> false
            }
        }

        val turnLeftBtn: Button = activity.findViewById(R.id.turn_left_btn)
        turnLeftBtn.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.isPressed = true
                    pressingTurn_Left(view)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.isPressed = false
                    releasedTurn_Left(view)
                    true
                }
                else -> false
            }
        }

        val turnRightBtn: Button = activity.findViewById(R.id.turn_right_btn)
        turnRightBtn.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.isPressed = true
                    pressingTurn_Right(view)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.isPressed = false
                    releasedTurn_Right(view)
                    true
                }
                else -> false
            }
        }

        val connectBtn: Button = activity.findViewById(R.id.connect)
        connectBtn.setOnClickListener { handleConnect(it) }
    }


    fun handleConnect(view: View) {
        Snackbar.make(view, "try Connect", Snackbar.LENGTH_SHORT).show()
    }

    fun pressingGo(view: View) {
        Snackbar.make(view, "pressing GO", Snackbar.LENGTH_SHORT).show()
    }

    fun releasedGo(view: View) {
        Snackbar.make(view, "released GO", Snackbar.LENGTH_SHORT).show()
    }

    fun pressingBack(view: View) {
        Snackbar.make(view, "pressing back", Snackbar.LENGTH_SHORT).show()
    }

    fun releasedBack(view: View) {
        Snackbar.make(view, "released back", Snackbar.LENGTH_SHORT).show()
    }

    fun pressingTurn_Left(view: View) {
        Snackbar.make(view, "pressing LT", Snackbar.LENGTH_SHORT).show()
    }

    fun releasedTurn_Left(view: View) {
        Snackbar.make(view, "released LT", Snackbar.LENGTH_SHORT).show()
    }

    fun pressingTurn_Right(view: View) {
        Snackbar.make(view, "pressing RT", Snackbar.LENGTH_SHORT).show()
    }

    fun releasedTurn_Right(view: View) {
        Snackbar.make(view, "released RT", Snackbar.LENGTH_SHORT).show()
    }
}
