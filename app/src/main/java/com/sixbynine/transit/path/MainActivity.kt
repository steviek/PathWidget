package com.sixbynine.transit.path

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.sixbynine.transit.path.ui.theme.PathTheme
import com.sixbynine.transit.path.widget.DepartureBoardWidgetReceiver
import com.sixbynine.transit.path.widget.WidgetRefreshWorkerScheduler
import com.sixbynine.transit.path.widget.configuration.DepartureBoardWidgetConfigurationActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var widgetRefreshScheduler: WidgetRefreshWorkerScheduler

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Content()
        }
    }

    @Composable
    fun Content() {
        PathTheme {
            Surface(color = MaterialTheme.colors.background) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val logsToDisplay by viewModel.logs.collectAsState(initial = emptyList())
                    if (logsToDisplay.isNotEmpty()) {
                        SelectionContainer(modifier = Modifier.weight(1f)) {
                            LazyColumn {
                                logsToDisplay.forEach { (_, timestamp, message, level) ->
                                    item {
                                        val levelDisplay = when (level) {
                                            Log.DEBUG -> "D"
                                            Log.WARN -> "W"
                                            else -> level.toString()
                                        }
                                        Text("${formatLogTimestamp(timestamp)}: $levelDisplay: $message")
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Text(
                        text = stringResource(R.string.welcome_message),
                        color = MaterialTheme.colors.onBackground,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp
                    )

                    if (showAddWidgetButton) {
                        Spacer(
                            modifier = Modifier
                                .height(8.dp)
                                .width(1.dp)
                        )
                        Button(onClick = { requestAddWidget() }) {
                            Text(stringResource(R.string.add_widget))
                        }
                    }

                    if (BuildConfig.DEBUG) {
                        Spacer(
                            modifier = Modifier
                                .height(8.dp)
                                .width(1.dp)
                        )
                        Button(
                            onClick = {
                                lifecycleScope.launch {
                                    widgetRefreshScheduler.performOneTimeRefresh()
                                }
                            }
                        ) {
                            Text("Update widgets")
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = { sendReportEmailIntent() }) {
                        Text(stringResource(R.string.report_problem), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }

    private val appWidgetManager: AppWidgetManager
        get() = AppWidgetManager.getInstance(this)

    private val showAddWidgetButton: Boolean
        get() = VERSION.SDK_INT >= 26 && appWidgetManager.isRequestPinAppWidgetSupported

    private fun requestAddWidget() {
        if (!showAddWidgetButton) return

        // Android doesn't call the configuration activity for you. You need to pass it
        // as a callback. Android will add the extra for the widget id.
        val configurationIntent =
            Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                .setClass(this, DepartureBoardWidgetConfigurationActivity::class.java)

        val configurationPendingIntent =
            PendingIntent.getActivity(
                this,
                /* requestCode = */ 0,
                configurationIntent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        appWidgetManager.requestPinAppWidget(
            ComponentName(this, DepartureBoardWidgetReceiver::class.java),
            /* extras = */ null,
            /* successCallback = */ configurationPendingIntent
        )
    }

    private fun openPathApiGithub() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/mrazza/path-data")))
    }

    private fun sendReportEmailIntent() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("sixbynineapps@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        }
        startActivity(intent)
    }

    private val logTimestampFormatter = DateTimeFormatter.ofPattern("dd HH:mm:ss")
    private fun formatLogTimestamp(timestamp: Instant): String {
        return logTimestampFormatter.format(
            timestamp.atZone(ZoneId.systemDefault()).toLocalDateTime()
        )
    }
}
