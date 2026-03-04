#!/bin/bash

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "Erreur: npm n'est pas installé. Pour mettre à jour la documentation sur votre machine, veuillez installer npm et Node.js."
    echo "Consultez https://nodejs.org/ pour plus d'informations."
    exit 1
fi

# Check if npx is installed
if ! command -v npx &> /dev/null; then
    echo "Error: npx n'est pas installé. Pour mettre à jour la documentation sur votre machine, veuillez installer npm et Node.js."
    echo "Consultez https://nodejs.org/ pour plus d'informations."
    exit 1
fi

echo "✓ npm et npx sont installés !"

# Check if node_modules exists, if not, install dependencies
if [ ! -d "node_modules" ]; then
    echo "Dépendences non-installées. Procédons à l'installation..."
    npm install
    if [ $? -ne 0 ]; then
        echo "Erreur: Echec de l'installation. Veuillez consulter les logs pour plus d'informations."
        exit 1
    fi
    echo "✓ Dépendences installées sans erreurs !"
else
    echo "✓ Dépendences déjà installées !"
fi

# Build Quartz to raw_html directory
echo "Mise à jour de la documentation dans raw_html..."
npx quartz build --output raw_html