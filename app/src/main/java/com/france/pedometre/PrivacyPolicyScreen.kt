package com.france.pedometre

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) {
                Text("‹ Retour", color = Color(0xFF00E676), fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("POLITIQUE DE CONFIDENTIALITÉ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Dernière mise à jour : avril 2026", color = Color.Gray, fontSize = 12.sp)
        Spacer(Modifier.height(24.dp))

        PrivacySection("1. Données collectées",
            "PedomètrePro collecte et stocke localement sur votre appareil :\n" +
            "• Votre nombre de pas quotidien\n" +
            "• Votre profil : taille, poids, sexe, longueur de foulée\n" +
            "• Votre objectif journalier de pas")

        PrivacySection("2. Utilisation des données",
            "Ces données sont utilisées exclusivement pour calculer votre distance parcourue, " +
            "les calories brûlées et votre progression vers votre objectif quotidien.")

        PrivacySection("3. Stockage et sécurité",
            "Toutes les données sont stockées uniquement sur votre appareil. " +
            "Aucune donnée n'est transmise à nos serveurs ni à des services tiers.")

        PrivacySection("4. Partage des données",
            "Nous ne partageons, ne vendons et ne transmettons aucune de vos données personnelles à des tiers.")

        PrivacySection("5. Suppression des données",
            "Vous pouvez supprimer l'intégralité de vos données à tout moment en désinstallant l'application.")

        PrivacySection("6. Contact",
            "Pour toute question concernant cette politique de confidentialité, contactez-nous à : franckim@orange.fr")
    }
}

@Composable
fun PrivacySection(title: String, body: String) {
    Text(title, color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 14.sp)
    Spacer(Modifier.height(6.dp))
    Text(body, color = Color.LightGray, fontSize = 13.sp, lineHeight = 20.sp)
    Spacer(Modifier.height(20.dp))
}
