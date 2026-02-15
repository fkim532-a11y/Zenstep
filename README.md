
# 🚶 ZenStep - Podomètre Mobile Web (PWA)

ZenStep est une application de podomètre minimaliste, élégante et privée, conçue pour fonctionner comme une **Progressive Web App (PWA)**.

## 🚀 Comment l'installer sur votre Smartphone

### Étape 1 : Hébergement avec GitHub Pages
1. Créez un dépôt GitHub et téléversez tous les fichiers du projet.
2. Allez dans **Settings** > **Pages**.
3. Choisissez la branche `main` et le dossier `/root`, puis cliquez sur **Save**.
4. Notez l'URL générée (ex: `https://votre-pseudo.github.io/votre-depot/`).

### Étape 2 : Installation sur Android / iOS
1. Ouvrez l'URL dans **Chrome** (Android) ou **Safari** (iOS).
2. **Sur Android** : Cliquez sur les 3 points en haut à droite > **"Installer l'application"**.
3. **Sur iOS** : Cliquez sur l'icône de partage (carré avec flèche) > **"Sur l'écran d'accueil"**.
4. L'application est maintenant sur votre écran d'accueil et fonctionne hors-ligne !

## ✨ Points Forts
- **Vie Privée** : Aucune donnée ne quitte votre téléphone (stockage local uniquement).
- **Batterie** : Utilise l'accéléromètre, beaucoup moins gourmand que le GPS.
- **Offline** : Fonctionne sans internet une fois installée.
- **Zéro Pub** : Une expérience pure et zen.

## 🛠️ Technique
- **Framework** : React 19 (via esm.sh pour un déploiement sans build complexe).
- **Style** : Tailwind CSS.
- **Capteurs** : API `DeviceMotionEvent` pour le comptage de pas.
