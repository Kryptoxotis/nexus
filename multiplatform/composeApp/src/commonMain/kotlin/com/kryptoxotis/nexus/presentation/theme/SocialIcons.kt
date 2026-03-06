package com.kryptoxotis.nexus.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object SocialIcons {

    val X: ImageVector by lazy {
        ImageVector.Builder("SocialX", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(18.244f, 2.25f)
                lineTo(21.552f, 2.25f)
                lineTo(14.325f, 10.51f)
                lineTo(22.827f, 21.75f)
                lineTo(16.17f, 21.75f)
                lineTo(10.956f, 14.933f)
                lineTo(4.99f, 21.75f)
                lineTo(1.68f, 21.75f)
                lineTo(9.41f, 12.915f)
                lineTo(1.254f, 2.25f)
                lineTo(8.08f, 2.25f)
                lineTo(12.793f, 8.481f)
                close()
                moveTo(17.083f, 19.77f)
                lineTo(18.916f, 19.77f)
                lineTo(7.084f, 4.126f)
                lineTo(5.117f, 4.126f)
                close()
            }
        }.build()
    }

    val GitHub: ImageVector by lazy {
        ImageVector.Builder("SocialGitHub", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 0f)
                curveToRelative(-6.626f, 0f, -12f, 5.373f, -12f, 12f)
                curveToRelative(0f, 5.302f, 3.438f, 9.8f, 8.207f, 11.387f)
                curveToRelative(0.599f, 0.111f, 0.793f, -0.261f, 0.793f, -0.577f)
                verticalLineToRelative(-2.234f)
                curveToRelative(-3.338f, 0.726f, -4.033f, -1.416f, -4.033f, -1.416f)
                curveToRelative(-0.546f, -1.387f, -1.333f, -1.756f, -1.333f, -1.756f)
                curveToRelative(-1.089f, -0.745f, 0.083f, -0.729f, 0.083f, -0.729f)
                curveToRelative(1.205f, 0.084f, 1.839f, 1.237f, 1.839f, 1.237f)
                curveToRelative(1.07f, 1.834f, 2.807f, 1.304f, 3.492f, 0.997f)
                curveToRelative(0.107f, -0.775f, 0.418f, -1.305f, 0.762f, -1.604f)
                curveToRelative(-2.665f, -0.305f, -5.467f, -1.334f, -5.467f, -5.931f)
                curveToRelative(0f, -1.311f, 0.469f, -2.381f, 1.236f, -3.221f)
                curveToRelative(-0.124f, -0.303f, -0.535f, -1.524f, 0.117f, -3.176f)
                curveToRelative(0f, 0f, 1.008f, -0.322f, 3.301f, 1.23f)
                curveToRelative(0.957f, -0.266f, 1.983f, -0.399f, 3.003f, -0.404f)
                curveToRelative(1.02f, 0.005f, 2.047f, 0.138f, 3.006f, 0.404f)
                curveToRelative(2.291f, -1.552f, 3.297f, -1.23f, 3.297f, -1.23f)
                curveToRelative(0.653f, 1.653f, 0.242f, 2.874f, 0.118f, 3.176f)
                curveToRelative(0.77f, 0.84f, 1.235f, 1.911f, 1.235f, 3.221f)
                curveToRelative(0f, 4.609f, -2.807f, 5.624f, -5.479f, 5.921f)
                curveToRelative(0.43f, 0.372f, 0.823f, 1.102f, 0.823f, 2.222f)
                verticalLineToRelative(3.293f)
                curveToRelative(0f, 0.319f, 0.192f, 0.694f, 0.801f, 0.576f)
                curveToRelative(4.765f, -1.589f, 8.199f, -6.086f, 8.199f, -11.386f)
                curveToRelative(0f, -6.627f, -5.373f, -12f, -12f, -12f)
                close()
            }
        }.build()
    }

    val LinkedIn: ImageVector by lazy {
        ImageVector.Builder("SocialLinkedIn", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.White)) {
                // Person icon (head)
                moveTo(8.44f, 8.56f)
                arcToRelative(1.53f, 1.53f, 0f, true, true, 0f, -3.06f)
                arcToRelative(1.53f, 1.53f, 0f, true, true, 0f, 3.06f)
                close()
                // Left bar
                moveTo(7.12f, 9.77f)
                horizontalLineToRelative(2.63f)
                verticalLineToRelative(8.46f)
                lineTo(7.12f, 18.23f)
                close()
                // Right section
                moveTo(17.88f, 18.23f)
                horizontalLineToRelative(-2.63f)
                verticalLineToRelative(-4.12f)
                curveToRelative(0f, -0.98f, -0.02f, -2.24f, -1.37f, -2.24f)
                curveToRelative(-1.37f, 0f, -1.58f, 1.07f, -1.58f, 2.17f)
                verticalLineToRelative(4.19f)
                horizontalLineToRelative(-2.63f)
                lineTo(9.67f, 9.77f)
                horizontalLineToRelative(2.52f)
                verticalLineToRelative(1.15f)
                horizontalLineToRelative(0.04f)
                arcToRelative(2.77f, 2.77f, 0f, false, true, 2.49f, -1.37f)
                curveToRelative(2.66f, 0f, 3.16f, 1.76f, 3.16f, 4.04f)
                close()
            }
        }.build()
    }

    val Instagram: ImageVector by lazy {
        ImageVector.Builder("SocialInstagram", 24.dp, 24.dp, 24f, 24f).apply {
            // Rounded rect (stroke only — use solid color since ImageVector doesn't support gradients)
            path(
                fill = null,
                stroke = SolidColor(Color(0xFFD62976)),
                strokeLineWidth = 1.8f
            ) {
                moveTo(7f, 2f)
                lineTo(17f, 2f)
                arcTo(5f, 5f, 0f, false, true, 22f, 7f)
                lineTo(22f, 17f)
                arcTo(5f, 5f, 0f, false, true, 17f, 22f)
                lineTo(7f, 22f)
                arcTo(5f, 5f, 0f, false, true, 2f, 17f)
                lineTo(2f, 7f)
                arcTo(5f, 5f, 0f, false, true, 7f, 2f)
                close()
            }
            // Circle
            path(
                fill = null,
                stroke = SolidColor(Color(0xFFD62976)),
                strokeLineWidth = 1.8f
            ) {
                moveTo(7f, 12f)
                arcToRelative(5f, 5f, 0f, true, false, 10f, 0f)
                arcToRelative(5f, 5f, 0f, true, false, -10f, 0f)
            }
            // Dot
            path(fill = SolidColor(Color(0xFFD62976))) {
                moveTo(16f, 6.5f)
                arcToRelative(1.5f, 1.5f, 0f, true, false, 3f, 0f)
                arcToRelative(1.5f, 1.5f, 0f, true, false, -3f, 0f)
            }
        }.build()
    }

    val Facebook: ImageVector by lazy {
        ImageVector.Builder("SocialFacebook", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(18f, 2f)
                horizontalLineToRelative(-3f)
                arcToRelative(5f, 5f, 0f, false, false, -5f, 5f)
                verticalLineToRelative(3f)
                lineTo(7f, 10f)
                verticalLineToRelative(4f)
                horizontalLineToRelative(3f)
                verticalLineToRelative(8f)
                horizontalLineToRelative(4f)
                verticalLineToRelative(-8f)
                horizontalLineToRelative(3f)
                lineToRelative(1f, -4f)
                horizontalLineToRelative(-4f)
                lineTo(14f, 7f)
                arcToRelative(1f, 1f, 0f, false, true, 1f, -1f)
                horizontalLineToRelative(3f)
                close()
            }
        }.build()
    }

    val YouTube: ImageVector by lazy {
        ImageVector.Builder("SocialYouTube", 24.dp, 24.dp, 24f, 24f).apply {
            // Background
            path(fill = SolidColor(Color(0xFF282828))) {
                moveTo(23.498f, 6.186f)
                arcToRelative(3.016f, 3.016f, 0f, false, false, -2.122f, -2.136f)
                curveTo(19.505f, 3.545f, 12f, 3.545f, 12f, 3.545f)
                reflectiveCurveToRelative(-7.505f, 0f, -9.377f, 0.505f)
                arcTo(3.017f, 3.017f, 0f, false, false, 0.502f, 6.186f)
                curveTo(0f, 8.07f, 0f, 12f, 0f, 12f)
                reflectiveCurveToRelative(0f, 3.93f, 0.502f, 5.814f)
                arcToRelative(3.016f, 3.016f, 0f, false, false, 2.122f, 2.136f)
                curveToRelative(1.871f, 0.505f, 9.376f, 0.505f, 9.376f, 0.505f)
                reflectiveCurveToRelative(7.505f, 0f, 9.377f, -0.505f)
                arcToRelative(3.015f, 3.015f, 0f, false, false, 2.122f, -2.136f)
                curveTo(24f, 15.93f, 24f, 12f, 24f, 12f)
                reflectiveCurveToRelative(0f, -3.93f, -0.502f, -5.814f)
                close()
            }
            // Play button
            path(fill = SolidColor(Color(0xFFFF0000))) {
                moveTo(9.545f, 15.568f)
                lineTo(9.545f, 8.432f)
                lineTo(15.818f, 12f)
                close()
            }
        }.build()
    }

    val Discord: ImageVector by lazy {
        ImageVector.Builder("SocialDiscord", 24.dp, 24.dp, 24f, 24f).apply {
            // Controller shape (outline)
            path(
                fill = null,
                stroke = SolidColor(Color(0xFF5865F2)),
                strokeLineWidth = 0.8f
            ) {
                moveTo(20.317f, 4.37f)
                arcToRelative(19.791f, 19.791f, 0f, false, false, -4.885f, -1.515f)
                arcToRelative(0.074f, 0.074f, 0f, false, false, -0.079f, 0.037f)
                curveToRelative(-0.21f, 0.375f, -0.444f, 0.864f, -0.608f, 1.25f)
                arcToRelative(18.27f, 18.27f, 0f, false, false, -5.487f, 0f)
                arcToRelative(12.64f, 12.64f, 0f, false, false, -0.617f, -1.25f)
                arcToRelative(0.077f, 0.077f, 0f, false, false, -0.079f, -0.037f)
                arcTo(19.736f, 19.736f, 0f, false, false, 3.677f, 4.37f)
                arcToRelative(0.07f, 0.07f, 0f, false, false, -0.032f, 0.027f)
                curveTo(0.533f, 9.046f, -0.32f, 13.58f, 0.099f, 18.057f)
                arcToRelative(0.082f, 0.082f, 0f, false, false, 0.031f, 0.057f)
                arcToRelative(19.9f, 19.9f, 0f, false, false, 5.993f, 3.03f)
                arcToRelative(0.078f, 0.078f, 0f, false, false, 0.084f, -0.028f)
                curveToRelative(0.462f, -0.63f, 0.874f, -1.295f, 1.226f, -1.994f)
                arcToRelative(0.076f, 0.076f, 0f, false, false, -0.041f, -0.106f)
                arcToRelative(13.107f, 13.107f, 0f, false, true, -1.872f, -0.892f)
                arcToRelative(0.077f, 0.077f, 0f, false, true, -0.008f, -0.128f)
                curveToRelative(0.12f, -0.098f, 0.246f, -0.198f, 0.373f, -0.292f)
                arcToRelative(0.074f, 0.074f, 0f, false, true, 0.077f, -0.01f)
                curveToRelative(3.928f, 1.793f, 8.18f, 1.793f, 12.062f, 0f)
                arcToRelative(0.074f, 0.074f, 0f, false, true, 0.078f, 0.01f)
                curveToRelative(0.12f, 0.098f, 0.246f, 0.198f, 0.373f, 0.292f)
                arcToRelative(0.077f, 0.077f, 0f, false, true, -0.006f, 0.127f)
                arcToRelative(12.299f, 12.299f, 0f, false, true, -1.873f, 0.892f)
                arcToRelative(0.077f, 0.077f, 0f, false, false, -0.041f, 0.107f)
                curveToRelative(0.36f, 0.698f, 0.772f, 1.362f, 1.225f, 1.993f)
                arcToRelative(0.076f, 0.076f, 0f, false, false, 0.084f, 0.028f)
                arcToRelative(19.839f, 19.839f, 0f, false, false, 6.002f, -3.03f)
                arcToRelative(0.077f, 0.077f, 0f, false, false, 0.032f, -0.054f)
                curveToRelative(0.5f, -5.177f, -0.838f, -9.674f, -3.549f, -13.66f)
                arcToRelative(0.061f, 0.061f, 0f, false, false, -0.031f, -0.03f)
                close()
            }
            // Left eye
            path(fill = SolidColor(Color(0xFF5865F2))) {
                moveTo(8.35f, 11.4f)
                arcToRelative(1.6f, 1.6f, 0f, true, false, 0f, 3.2f)
                arcToRelative(1.6f, 1.6f, 0f, true, false, 0f, -3.2f)
                close()
            }
            // Right eye
            path(fill = SolidColor(Color(0xFF5865F2))) {
                moveTo(15.65f, 11.4f)
                arcToRelative(1.6f, 1.6f, 0f, true, false, 0f, 3.2f)
                arcToRelative(1.6f, 1.6f, 0f, true, false, 0f, -3.2f)
                close()
            }
        }.build()
    }

    val Twitch: ImageVector by lazy {
        ImageVector.Builder("SocialTwitch", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color(0xFF9146FF))) {
                moveTo(6.5f, 2f)
                lineTo(3.5f, 5.5f)
                verticalLineToRelative(13f)
                horizontalLineToRelative(4.5f)
                lineTo(8f, 22f)
                lineToRelative(3.5f, -3.5f)
                horizontalLineToRelative(2.5f)
                lineTo(20.5f, 12f)
                lineTo(20.5f, 2f)
                close()
                moveTo(18.5f, 11.5f)
                lineToRelative(-2.5f, 2.5f)
                horizontalLineToRelative(-3f)
                lineToRelative(-2.2f, 2.2f)
                verticalLineToRelative(-2.2f)
                lineTo(8f, 14f)
                lineTo(8f, 4f)
                horizontalLineToRelative(10.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFF9146FF))) {
                moveTo(15f, 6.5f)
                horizontalLineToRelative(1.5f)
                verticalLineToRelative(4.5f)
                lineTo(15f, 11f)
                close()
                moveTo(11.5f, 6.5f)
                lineTo(13f, 6.5f)
                verticalLineToRelative(4.5f)
                horizontalLineToRelative(-1.5f)
                close()
            }
        }.build()
    }

    val TikTok: ImageVector by lazy {
        ImageVector.Builder("SocialTikTok", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12.525f, 0.02f)
                curveToRelative(1.31f, -0.02f, 2.61f, -0.01f, 3.91f, -0.02f)
                curveToRelative(0.08f, 1.53f, 0.63f, 3.09f, 1.75f, 4.17f)
                curveToRelative(1.12f, 1.11f, 2.7f, 1.62f, 4.24f, 1.79f)
                verticalLineToRelative(4.03f)
                curveToRelative(-1.44f, -0.05f, -2.89f, -0.35f, -4.2f, -0.97f)
                curveToRelative(-0.57f, -0.26f, -1.1f, -0.59f, -1.62f, -0.93f)
                curveToRelative(-0.01f, 2.92f, 0.01f, 5.84f, -0.02f, 8.75f)
                curveToRelative(-0.08f, 1.4f, -0.54f, 2.79f, -1.35f, 3.94f)
                curveToRelative(-1.31f, 1.92f, -3.58f, 3.17f, -5.91f, 3.21f)
                curveToRelative(-1.43f, 0.08f, -2.86f, -0.31f, -4.08f, -1.03f)
                curveToRelative(-2.02f, -1.19f, -3.44f, -3.37f, -3.65f, -5.71f)
                curveToRelative(-0.02f, -0.5f, -0.03f, -1f, -0.01f, -1.49f)
                curveToRelative(0.18f, -1.9f, 1.12f, -3.72f, 2.58f, -4.96f)
                curveToRelative(1.66f, -1.44f, 3.98f, -2.13f, 6.15f, -1.72f)
                curveToRelative(0.02f, 1.48f, -0.04f, 2.96f, -0.04f, 4.44f)
                curveToRelative(-0.99f, -0.32f, -2.15f, -0.23f, -3.02f, 0.37f)
                curveToRelative(-0.63f, 0.41f, -1.11f, 1.04f, -1.36f, 1.75f)
                curveToRelative(-0.21f, 0.51f, -0.15f, 1.07f, -0.14f, 1.61f)
                curveToRelative(0.24f, 1.64f, 1.82f, 3.02f, 3.5f, 2.87f)
                curveToRelative(1.12f, -0.01f, 2.19f, -0.66f, 2.77f, -1.61f)
                curveToRelative(0.19f, -0.33f, 0.4f, -0.67f, 0.41f, -1.06f)
                curveToRelative(0.1f, -1.79f, 0.06f, -3.57f, 0.07f, -5.36f)
                curveToRelative(0.01f, -4.03f, -0.01f, -8.05f, 0.02f, -12.07f)
                close()
            }
        }.build()
    }

    val WhatsApp: ImageVector by lazy {
        ImageVector.Builder("SocialWhatsApp", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(17.472f, 14.382f)
                curveToRelative(-0.297f, -0.149f, -1.758f, -0.867f, -2.03f, -0.967f)
                curveToRelative(-0.273f, -0.099f, -0.471f, -0.148f, -0.67f, 0.15f)
                curveToRelative(-0.197f, 0.297f, -0.767f, 0.966f, -0.94f, 1.164f)
                curveToRelative(-0.173f, 0.199f, -0.347f, 0.223f, -0.644f, 0.075f)
                curveToRelative(-0.297f, -0.15f, -1.255f, -0.463f, -2.39f, -1.475f)
                curveToRelative(-0.883f, -0.788f, -1.48f, -1.761f, -1.653f, -2.059f)
                curveToRelative(-0.173f, -0.297f, -0.018f, -0.458f, 0.13f, -0.606f)
                curveToRelative(0.134f, -0.133f, 0.298f, -0.347f, 0.446f, -0.52f)
                curveToRelative(0.149f, -0.174f, 0.198f, -0.298f, 0.298f, -0.497f)
                curveToRelative(0.099f, -0.198f, 0.05f, -0.371f, -0.025f, -0.52f)
                curveToRelative(-0.075f, -0.149f, -0.669f, -1.612f, -0.916f, -2.207f)
                curveToRelative(-0.242f, -0.579f, -0.487f, -0.5f, -0.669f, -0.51f)
                curveToRelative(-0.173f, -0.008f, -0.371f, -0.01f, -0.57f, -0.01f)
                curveToRelative(-0.198f, 0f, -0.52f, 0.074f, -0.792f, 0.372f)
                curveToRelative(-0.272f, 0.297f, -1.04f, 1.016f, -1.04f, 2.479f)
                curveToRelative(0f, 1.462f, 1.065f, 2.875f, 1.213f, 3.074f)
                curveToRelative(0.149f, 0.198f, 2.096f, 3.2f, 5.077f, 4.487f)
                curveToRelative(0.709f, 0.306f, 1.262f, 0.489f, 1.694f, 0.625f)
                curveToRelative(0.712f, 0.227f, 1.36f, 0.195f, 1.871f, 0.118f)
                curveToRelative(0.571f, -0.085f, 1.758f, -0.719f, 2.006f, -1.413f)
                curveToRelative(0.248f, -0.694f, 0.248f, -1.289f, 0.173f, -1.413f)
                curveToRelative(-0.074f, -0.124f, -0.272f, -0.198f, -0.57f, -0.347f)
                close()
                moveTo(12.051f, 21.775f)
                horizontalLineToRelative(-0.004f)
                arcToRelative(9.87f, 9.87f, 0f, false, true, -5.031f, -1.378f)
                lineToRelative(-0.361f, -0.214f)
                lineToRelative(-3.741f, 0.982f)
                lineToRelative(0.998f, -3.648f)
                lineToRelative(-0.235f, -0.374f)
                arcToRelative(9.86f, 9.86f, 0f, false, true, -1.51f, -5.26f)
                curveToRelative(0.001f, -5.45f, 4.436f, -9.884f, 9.888f, -9.884f)
                curveToRelative(2.64f, 0f, 5.122f, 1.03f, 6.988f, 2.898f)
                arcToRelative(9.825f, 9.825f, 0f, false, true, 2.893f, 6.994f)
                curveToRelative(-0.003f, 5.45f, -4.437f, 9.884f, -9.885f, 9.884f)
                close()
                moveTo(20.464f, 3.478f)
                arcTo(11.815f, 11.815f, 0f, false, false, 12.05f, 0f)
                curveTo(5.495f, 0f, 0.16f, 5.335f, 0.157f, 11.892f)
                curveToRelative(0f, 2.096f, 0.547f, 4.142f, 1.588f, 5.945f)
                lineTo(0.057f, 24f)
                lineToRelative(6.305f, -1.654f)
                arcToRelative(11.882f, 11.882f, 0f, false, false, 5.683f, 1.448f)
                horizontalLineToRelative(0.005f)
                curveToRelative(6.554f, 0f, 11.89f, -5.335f, 11.893f, -11.893f)
                arcToRelative(11.821f, 11.821f, 0f, false, false, -3.48f, -8.413f)
                close()
            }
        }.build()
    }
}

/**
 * Maps a social field key to its custom social icon.
 * Returns null for non-social fields (phone, email, website, jobTitle, company).
 */
fun socialIconFor(key: String): ImageVector? = when (key) {
    "instagram" -> SocialIcons.Instagram
    "twitter" -> SocialIcons.X
    "github" -> SocialIcons.GitHub
    "linkedin" -> SocialIcons.LinkedIn
    "facebook" -> SocialIcons.Facebook
    "youtube" -> SocialIcons.YouTube
    "tiktok" -> SocialIcons.TikTok
    "discord" -> SocialIcons.Discord
    "twitch" -> SocialIcons.Twitch
    "whatsapp" -> SocialIcons.WhatsApp
    else -> null
}
