package com.example.arweld.feature.arview.ui.arview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.example.arweld.feature.arview.R
import com.example.arweld.feature.arview.arcore.ARViewController
import com.example.arweld.feature.arview.arcore.ARViewLifecycleHost

@Composable
fun ARViewScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    infoOverlay: @Composable () -> Unit = {},
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val controller = remember { ARViewController(context) }

    DisposableEffect(lifecycleOwner, controller) {
        val lifecycleHost = ARViewLifecycleHost(lifecycleOwner.lifecycle, controller)
        lifecycleHost.start()
        onDispose { lifecycleHost.stop() }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.ar_view_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AndroidView(
                factory = { controller.getView() },
                modifier = Modifier.fillMaxSize()
            )
            infoOverlay()
        }
    }
}
