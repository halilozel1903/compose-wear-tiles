package io.github.halilozel1903.weartiles.sample

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.wear.tiles.renderer.TileRenderer
import io.github.halilozel1903.weartiles.TileRendering

/**
 * The tile gallery. Tiles live in the system's tile carousel, which CI cannot drive reliably, so
 * this activity inflates the same layouts the tile services build with the Wear OS tiles renderer
 * (`androidx.wear.tiles:tiles-renderer`'s `TileRenderer`) on the watch screen.
 *
 * - Without extras it shows all four tiles one below the other; scroll with the crown or a swipe.
 * - With `--es scene progress|stats|shortcuts|countdown` it shows one tile full screen, as used by
 *   `scripts/screenshots.sh`.
 */
class MainActivity : Activity() {

    private lateinit var root: FrameLayout

    /** Inflates ProtoLayout layouts into views; a tapped Refresh action rebuilds the gallery. */
    private val renderer by lazy {
        TileRenderer(this, mainExecutor) { _ -> show(GalleryScene.from(intent.getStringExtra(EXTRA_SCENE))) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keeps the watch from dimming into ambient mode while the gallery is open.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        root = FrameLayout(this).apply { setBackgroundColor(BACKGROUND) }
        setContentView(root)
        show(GalleryScene.from(intent.getStringExtra(EXTRA_SCENE)))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        show(GalleryScene.from(intent.getStringExtra(EXTRA_SCENE)))
    }

    private fun show(scene: GalleryScene?) {
        root.removeAllViews()
        root.contentDescription = null
        if (scene != null) {
            val frame = tileFrame()
            root.addView(frame, FrameLayout.LayoutParams(MATCH, MATCH))
            inflate(scene, frame) { root.contentDescription = "${scene.title} tile" }
            return
        }
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val size = resources.displayMetrics.heightPixels
        GalleryScene.entries.forEach { scene ->
            val frame = tileFrame()
            list.addView(frame, LinearLayout.LayoutParams(MATCH, size))
            inflate(scene, frame) {}
        }
        val scroll = ScrollView(this).apply {
            isVerticalScrollBarEnabled = false
            addView(list, ViewGroup.LayoutParams(MATCH, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        root.addView(scroll, FrameLayout.LayoutParams(MATCH, MATCH))
        scroll.requestFocus() // the crown scrolls the focused view
    }

    private fun tileFrame(): FrameLayout = FrameLayout(this).apply { setBackgroundColor(BACKGROUND) }

    /** Builds [scene]'s layout for this screen and inflates it into [parent]. */
    private fun inflate(scene: GalleryScene, parent: FrameLayout, onShown: () -> Unit) {
        val rendered = TileRendering.render(
            context = this,
            deviceConfiguration = TileRendering.deviceParameters(this),
            layout = scene.layout(),
            theme = TrailmateTiles.theme,
            nowMillis = SampleData.NOW,
        )
        val inflated = renderer.inflateAsync(rendered.layout, rendered.resources, parent)
        inflated.addListener(
            {
                try {
                    val view = inflated.get()
                    (view.layoutParams as? FrameLayout.LayoutParams)?.gravity = Gravity.CENTER
                    onShown()
                } catch (error: Exception) {
                    Log.e(TAG, "Could not inflate the ${scene.id} tile", error)
                    parent.addView(
                        TextView(this).apply {
                            text = "Could not render ${scene.id}: ${error.message}"
                            setTextColor(Color.WHITE)
                            gravity = Gravity.CENTER
                        },
                        FrameLayout.LayoutParams(MATCH, MATCH),
                    )
                }
            },
            mainExecutor,
        )
    }

    companion object {
        const val EXTRA_SCENE = "scene"
        private const val TAG = "TileGallery"
        private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT

        /** A dark navy instead of pure black, so a blank capture stands out from a tile. */
        private val BACKGROUND = Color.rgb(0x0E, 0x15, 0x20)
    }
}
