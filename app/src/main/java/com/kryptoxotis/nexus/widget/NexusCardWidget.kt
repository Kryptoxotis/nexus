package com.kryptoxotis.nexus.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.appwidget.cornerRadius
import androidx.glance.unit.ColorProvider
import com.kryptoxotis.nexus.MainActivity
import com.kryptoxotis.nexus.data.local.NexusDatabase
import com.kryptoxotis.nexus.domain.model.BusinessCardData
import com.kryptoxotis.nexus.service.WidgetBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val TealColor = ColorProvider(Color(0xFF037A68))
private val WhiteText = ColorProvider(Color(0xFFEEEEEE))
private val DimText = ColorProvider(Color(0xFF888888))

class NexusCardWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val userId = WidgetBridge.readUserId(context)
        val dao = NexusDatabase.getDatabase(context).personalCardDao()

        val businessCard = if (userId != null) {
            withContext(Dispatchers.IO) {
                dao.getCardsByUser(userId).firstOrNull { it.cardType == "business_card" }
            }
        } else null

        val cardData = businessCard?.let { BusinessCardData.fromJson(it.content ?: "") }

        provideContent {
            NexusCard(
                name = businessCard?.title ?: "Nexus",
                subtitle = cardData?.subtitle() ?: "Tap to open"
            )
        }
    }
}

@Composable
private fun NexusCard(name: String, subtitle: String) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(TealColor)
            .cornerRadius(16.dp)
            .clickable(actionStartActivity(Intent(LocalContext.current, MainActivity::class.java)))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = name,
                style = TextStyle(
                    color = WhiteText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            if (subtitle.isNotBlank()) {
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = TextStyle(color = DimText, fontSize = 13.sp),
                    maxLines = 1
                )
            }
        }
    }
}

class NexusCardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NexusCardWidget()
}
