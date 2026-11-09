package com.worthour.data

data class PresetSubscription(
    val id: String,
    val name: String,
    val defaultPrice: Int,
    val cycle: PaymentCycle = PaymentCycle.MONTHLY,
    val analysisType: AnalysisType = AnalysisType.TIME_TRACKING,
    val candidatePackages: List<String>
) {
    companion object {
        val PRESETS = listOf(
            PresetSubscription(
                id = "netflix",
                name = "Netflix",
                defaultPrice = 17000,
                candidatePackages = listOf("com.netflix.mediaclient")
            ),
            PresetSubscription(
                id = "youtube",
                name = "YouTube Premium",
                defaultPrice = 14900,
                candidatePackages = listOf(
                    "com.google.android.youtube",
                    "com.google.android.apps.youtube.music"
                )
            ),
            PresetSubscription(
                id = "spotify",
                name = "Spotify",
                defaultPrice = 10900,
                candidatePackages = listOf("com.spotify.music")
            ),
            PresetSubscription(
                id = "disney",
                name = "Disney+",
                defaultPrice = 6100,
                candidatePackages = listOf("com.disney.disneyplus")
            )
        )
    }
}
