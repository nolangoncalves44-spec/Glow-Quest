# Glow Quest

App de bien-être en mode jeu, installable sur téléphone (PWA), hors ligne, sans store.

## Mise en ligne (une seule fois)
1. Déposer tous les fichiers de ce dossier à la racine du dépôt (Add file > Upload files > Commit).
2. Settings > Pages > Source : "Deploy from a branch", branche `main`, dossier `/ (root)` > Save.
3. Attendre 1 à 2 min, puis ouvrir https://nolangoncalves44-spec.github.io/Glow-Quest/
4. Sur le téléphone (Chrome) : menu > "Installer l'application" ou "Ajouter à l'écran d'accueil".

## Mise à jour
1. Modifier `index.html` (et `APP_VERSION` dedans).
2. Changer `VERSION` dans `sw.js` (ex. 1.0.0 vers 1.0.1). Obligatoire, sinon le téléphone garde l'ancienne version.
3. Déposer les fichiers modifiés sur GitHub.
4. À la prochaine ouverture, l'app affiche "Nouvelle version disponible". Un toucher recharge l'app.

Les données restent sur le téléphone (stockage du site) et ne sont pas touchées par les mises à jour.
Une copie de secours est faite automatiquement avant chaque mise à jour. Export/Import dans Réglages.
