Cette documentation est générée avec [Quartz](https://quartz.jzhao.xyz/), un générateur de site statique qui transforme des fichiers Markdown en pages web. Les fichiers sources se trouvent dans le dossier `documentation/content/`.

## Prérequis

Pour contribuer à la documentation, vous aurez besoin de :
- [Node.js](https://nodejs.org/) et npm/npx (pour Quartz)
- [JDK](https://www.oracle.com/java/technologies/downloads/) avec javadoc (pour générer la Javadoc)
- (Optionnel) [Obsidian](https://obsidian.md/) ou [Logseq](https://logseq.com/) pour éditer confortablement

## Éditer la documentation

### Avec un éditeur Markdown simple

Vous pouvez éditer directement les fichiers `.md` dans `documentation/content/` avec n'importe quel éditeur de texte. La documentation utilise la syntaxe Markdown standard plus quelques extensions :

- Liens internes : `[[Nom de la page]]` ou `[[Nom de fichier|Texte affiché]]`
- Intégration Javadoc : `{{javadoc:package.ClassName}}`
- Front matter YAML pour les métadonnées (optionnel)

### Avec Obsidian ou Logseq

Ces éditeurs sont spécialement conçus pour gérer des notes avec des liens bidirectionnels :

1. Ouvrez le dossier `documentation/content/` comme vault/graph dans l'application
2. Profitez de l'autocomplétion des liens, de la vue graphe des relations entre pages
3. Les modifications sont instantanément sauvegardées

**Avantage** : Ces outils offrent une meilleure expérience pour naviguer et créer des liens entre les pages de documentation.

## Prévisualiser vos modifications

Pour voir le rendu de vos modifications avant de les publier :

```bash
cd documentation
./preview.sh
```

Ce script va :
1. Vérifier que npm/npx sont installés
2. Installer les dépendances si nécessaire
3. Lancer un serveur local avec rechargement automatique sur `http://localhost:8081`

Les modifications dans `content/` sont détectées et le site se recharge automatiquement.

> [!WARNING]
> Ce script ne génère pas la documentation Javadoc. Assurez-vous d'avoir exécuté `./build.sh` au moins une fois pour que les fichiers Javadoc soient disponibles dans `static/javadoc/`.

## Générer la version finale

Pour générer la documentation complète (Javadoc + Quartz) :

```bash
cd documentation
./build.sh
```

Ce script va :
1. Vérifier les dépendances npm
2. Générer la Javadoc depuis les sources Java dans `static/javadoc/`
3. Compiler le site Quartz dans le dossier `public/`

Le dossier `public/` contient alors la documentation au format HTML complète prête à être déployée. Vous pouvez même ouvrir `public/index.html` dans votre navigateur pour voir le résultat localement.

## Structure des fichiers

```
documentation/
├── content/              # Fichiers Markdown sources
│   ├── index.md         # Page d'accueil
│   ├── Architecture/    # Documentation des classes
│   ├── Comment utiliser FRED ?/
│   └── Languages/       # Guides par langage
├── static/
│   └── javadoc/        # Documentation Javadoc générée
├── public/             # Site compilé (généré par build.sh)
├── quartz.config.ts    # Configuration Quartz
└── quartz.layout.ts    # Layout de la page
```

## Ajouter une nouvelle page

1. Créez un fichier `.md` dans le bon sous-dossier de `content/`
2. Ajoutez un front matter si nécessaire :
   ```markdown
   ---
   title: Titre de la page
   ---
   ```
   Si vous n'ajoutez pas de front matter, le titre sera généré automatiquement à partir du nom du fichier.
3. Écrivez votre contenu en Markdown
4. Utilisez `[[Nom de page]]` pour créer des liens vers d'autres pages
5. Pour documenter une classe Java, ajoutez `{{javadoc:package.ClassName}}` en fin de fichier

## Paramètres des pages

La configuration de la documentation se trouve dans `quartz.config.ts`. Vous pouvez définir des paramètres globaux ou spécifiques à certaines pages, comme le titre, la description, les balises meta, etc.

Pour en savoir plus sur la configuration, consultez la section [Configuration de Quartz](https://quartz.jzhao.xyz/configuration).

## Bonnes pratiques

- **Liens internes** : Privilégiez les liens wiki-style `[[Page]]` plutôt que les liens Markdown classiques
- **Javadoc** : Documentez le code Java directement dans les fichiers `.java`, la documentation sera générée automatiquement
- **Concision** : Gardez les explications claires et concises
- **Exemples** : Illustrez avec des exemples de code quand c'est pertinent
- **Liens externes** : Ajoutez des liens vers la documentation des outils utilisés (JUnit, Checkstyle, etc.)