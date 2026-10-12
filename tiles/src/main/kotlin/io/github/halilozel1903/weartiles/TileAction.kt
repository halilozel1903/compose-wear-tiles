package io.github.halilozel1903.weartiles

import android.app.Activity
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ModifiersBuilders

/**
 * What happens when a part of a tile is tapped.
 *
 * ```kotlin
 * TileAction.launch<WorkoutActivity>("type" to "run")   // open an activity with string extras
 * TileAction.Refresh                                     // ask for a new tile (lastClickId tells you what was tapped)
 * ```
 */
public sealed interface TileAction {

    /**
     * Starts the activity [className] of [packageName] (your app when null) with string [extras].
     * The activity must be exported so the tile host can start it.
     */
    public class LaunchActivity(
        public val className: String,
        public val packageName: String? = null,
        public val extras: Map<String, String> = emptyMap(),
    ) : TileAction {
        override fun equals(other: Any?): Boolean =
            other is LaunchActivity && other.className == className && other.packageName == packageName && other.extras == extras

        override fun hashCode(): Int = (className.hashCode() * 31 + packageName.hashCode()) * 31 + extras.hashCode()

        override fun toString(): String = "LaunchActivity($className, $packageName, $extras)"
    }

    /**
     * Requests a new tile from your service. The id of the tapped element arrives as
     * [TileStateRequest.lastClickId], so a refresh can also mean "next page" or "mark done".
     */
    public data object Refresh : TileAction

    public companion object {
        /** Starts [activity] of this app with string [extras]. */
        public fun launch(activity: Class<out Activity>, extras: Map<String, String> = emptyMap()): TileAction =
            LaunchActivity(activity.name, packageName = null, extras = extras)

        /** Starts the activity [A] of this app with string [extras]. */
        public inline fun <reified A : Activity> launch(vararg extras: Pair<String, String>): TileAction =
            launch(A::class.java, extras.toMap())
    }
}

/** This action as a ProtoLayout [ModifiersBuilders.Clickable] with the element id [id]. */
public fun TileAction.toClickable(context: Context, id: String): ModifiersBuilders.Clickable {
    val builder = ModifiersBuilders.Clickable.Builder().setId(id)
    when (this) {
        is TileAction.LaunchActivity -> {
            val activity = ActionBuilders.AndroidActivity.Builder()
                .setPackageName(packageName ?: context.packageName)
                .setClassName(className)
            extras.forEach { (key, value) ->
                activity.addKeyToExtraMapping(key, ActionBuilders.AndroidStringExtra.Builder().setValue(value).build())
            }
            builder.setOnClick(ActionBuilders.LaunchAction.Builder().setAndroidActivity(activity.build()).build())
        }
        TileAction.Refresh -> builder.setOnClick(ActionBuilders.LoadAction.Builder().build())
    }
    return builder.build()
}

/**
 * The main action button of a tile, drawn as an edge button that follows the bottom of the screen.
 * With an [icon] it shows the icon, otherwise the [label] (which is also the content description).
 */
public class TileButton(
    public val label: String,
    public val action: TileAction,
    @param:DrawableRes public val icon: Int? = null,
)
