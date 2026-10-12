package io.github.halilozel1903.weartiles.sample

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import io.github.halilozel1903.weartiles.requestTileUpdate
import io.github.halilozel1903.weartiles.sample.tiles.StepsTileService

/**
 * Opened from the Steps and Quick start tiles with the workout type as an extra. A real app would
 * start a workout here; Trailmate logs 500 steps for a walk and asks Wear OS to refresh the Steps
 * tile right away instead of waiting for its 15 minute refresh.
 */
class WorkoutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val type = intent.getStringExtra(EXTRA_TYPE) ?: "run"
        if (type == "walk") {
            TrailmateRepository.loggedSteps += 500
            requestTileUpdate<StepsTileService>()
        }
        setContentView(
            TextView(this).apply {
                text = "${type.replaceFirstChar { it.uppercase() }} started"
                textSize = 20f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.rgb(0x0E, 0x15, 0x20))
            },
        )
    }

    companion object {
        const val EXTRA_TYPE = "type"
    }
}
