import { QuartzConfig } from "./quartz/cfg"
import * as Plugin from "./quartz/plugins"

/**
 * Quartz 4.0 Configuration
 *
 * See https://quartz.jzhao.xyz/configuration for more information.
 */
const config: QuartzConfig = {
  configuration: {
    pageTitle: "F.R.E.D",
    pageTitleSuffix: " - F.R.E.D",
    enableSPA: true,
    enablePopovers: true,
    enableMobileSidebar: true,
    analytics: {
      provider: "plausible",
    },
    locale: "fr-FR",
    baseUrl: "https://github.com/Hdvbxh7/FRED",
    ignorePatterns: ["private", "templates", ".obsidian"],
    defaultDateType: "created",
    theme: {
      fontOrigin: "googleFonts",
      cdnCaching: true,
      typography: {
        header: "Schibsted Grotesk",
        body: "Source Sans Pro",
        code: "IBM Plex Mono",
      },
      colors: {
        lightMode: {
          light: "#faf8f8",
          lightgray: "#e5e5e5",
          gray: "#b8b8b8",
          darkgray: "#4e4e4e",
          dark: "#2b2b2b",
          secondary: "#4a0d75",
          tertiary: "#a84dc9",
          highlight: "rgba(163, 143, 169, 0.15)",
          textHighlight: "#fff23688",
        },
        darkMode: {
          light: "#181618",
          lightgray: "#393639",
          gray: "#655669",
          darkgray: "#cecece",
          dark: "#ffffff",
          secondary: "#9062a7",
          tertiary: "#aa85bc",
          highlight: "rgba(143, 159, 169, 0.15)",
          textHighlight: "#aa00ffc2",
        },
      },
    },
  },
  plugins: {
    transformers: [
      Plugin.FrontMatter(),
      Plugin.CreatedModifiedDate({
        priority: ["frontmatter", "filesystem"],
      }),
      Plugin.SyntaxHighlighting({
        theme: {
          light: "github-light",
          dark: "github-dark",
        },
        keepBackground: false,
      }),
      Plugin.ObsidianFlavoredMarkdown({ enableInHtmlEmbed: false }),
      Plugin.GitHubFlavoredMarkdown(),
      Plugin.TableOfContents(),
      Plugin.CrawlLinks({ markdownLinkResolution: "shortest" }),
      Plugin.Description(),
      Plugin.Latex({ renderEngine: "katex" }),
      Plugin.Javadoc({
        javadocDir: "static/javadoc",
        inlineHtml: true,
        javadocBaseUrl: "/static/javadoc",
      }),
    ],
    filters: [Plugin.RemoveDrafts()],
    emitters: [
      Plugin.AliasRedirects(),
      Plugin.ComponentResources(),
      Plugin.ContentPage(),
      Plugin.FolderPage(),
      Plugin.TagPage(),
      Plugin.ContentIndex({
        enableSiteMap: true,
        enableRSS: true,
      }),
      Plugin.Assets(),
      Plugin.Static(),
      Plugin.NotFoundPage(),
    ],
  },
}

export default config
