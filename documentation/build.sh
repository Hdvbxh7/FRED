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

# Generate Javadoc
echo "Génération de la documentation Javadoc..."

# Check if javadoc is available
if ! command -v javadoc &> /dev/null; then
    echo "Avertissement: javadoc n'est pas installé. La documentation Java ne sera pas générée."
    echo "Consultez https://www.oracle.com/java/technologies/downloads/ pour installer le JDK."
else
    # Create javadoc output directory
    mkdir -p static/javadoc
    
    # Navigate to project root and generate javadoc
    cd ..
    
    # Find all Java source files
    JAVA_FILES=$(find . -name "*.java" -not -path "./BacATest/*" -not -path "./Tests/*" -not -path "./documentation/*")
    
    if [ -n "$JAVA_FILES" ]; then
        # Generate Javadoc with source files, excluding CheckStyle.java which has incompatible dependencies
        javadoc -d documentation/static/javadoc \
                -sourcepath . \
                -subpackages configuration:LibEvaluateur:LibExplorateurs \
                Superviseur.java \
                -classpath "libs/*" \
                -encoding UTF-8 \
                -charset UTF-8 \
                -private \
                -Xdoclint:none \
                -noqualifier all \
                -quiet \
                --ignore-source-errors \
        
        # Check if any HTML files were generated
        if [ -d "documentation/static/javadoc" ] && [ "$(find documentation/static/javadoc -name "*.html" -type f | head -1)" ]; then
            GENERATED_COUNT=$(find documentation/static/javadoc -name "*.html" -type f | wc -l)
            echo "✓ Documentation Javadoc générée pour $GENERATED_COUNT classes dans static/javadoc/ !"
            echo "  Note: CheckStyle.java exclu (dépendances incompatibles)"
        else
            echo "⚠️  Avertissement: La génération Javadoc a échoué. Certaines classes peuvent ne pas être documentées."
        fi
    else
        echo "Avertissement: Aucun fichier Java trouvé."
    fi
    
    # Return to documentation directory
    cd documentation
fi

# Build Quartz to public directory
echo "Mise à jour de la documentation dans public..."
npx quartz build
echo "✓ Documentation mise à jour avec succès !"