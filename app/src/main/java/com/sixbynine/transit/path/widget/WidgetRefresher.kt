package com.sixbynine.transit.path.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import com.sixbynine.transit.path.logging.Logging
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

interface WidgetRefresher {
    suspend fun refreshWidget(id: GlanceId)
}

class DefaultWidgetRefresher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val widget: DepartureBoardWidget,
) : WidgetRefresher {
    override suspend fun refreshWidget(id: GlanceId) {
        widget.update(context, id)
    }
}

@InstallIn(SingletonComponent::class)
@Module
interface WidgetRefresherModule {
    @Binds
    fun bindWidgetRefresher(refresher: DefaultWidgetRefresher): WidgetRefresher
}
