import { QuartzTransformerPlugin } from "../types"
import { Root } from "mdast"
import { visit } from "unist-util-visit"
import path from "path"
import fs from "fs"
import { BuildCtx } from "../../util/ctx"

export interface Options {
  /** Directory where Javadoc HTML files are located */
  javadocDir: string
  /** Whether to inline the full Javadoc HTML or just link to it */
  inlineHtml: boolean
  /** Base URL for Javadoc links (relative to site root) */
  javadocBaseUrl: string
}

const defaultOptions: Options = {
  javadocDir: "static/javadoc",
  inlineHtml: true,
  javadocBaseUrl: "/static/javadoc",
}

/**
 * Transformer plugin that processes {{javadoc:ClassName}} syntax in markdown
 * and replaces it with embedded Javadoc HTML content or links.
 *
 * Syntax examples:
 * - {{javadoc:Superviseur}} - Embeds the Javadoc for Superviseur class
 * - {{javadoc:configuration.Scenario}} - Embeds the Javadoc for Scenario class in configuration package
 * - {{javadoc:LibEvaluateur.Evaluateur}} - Embeds the Javadoc for Evaluateur class
 */
export const Javadoc: QuartzTransformerPlugin<Partial<Options>> = (userOpts) => {
  const opts = { ...defaultOptions, ...userOpts }

  return {
    name: "Javadoc",
    textTransform(ctx: BuildCtx, src: string | Buffer): string | Buffer {
      if (typeof src !== "string") {
        return src
      }

      // Pattern to match {{javadoc:ClassName}} or {{javadoc:package.ClassName}}
      const javadocPattern = /\{\{javadoc:([a-zA-Z0-9_.]+)\}\}/g

      return src.replace(javadocPattern, (match, className) => {
        // Try to find and read the Javadoc HTML file
        const javadocHtml = findJavadocHtml(className, opts.javadocDir, ctx)

        if (javadocHtml && opts.inlineHtml) {
          // Extract just the main content from the Javadoc HTML
          const content = extractJavadocContent(javadocHtml, className)
          return `\n\n<div class="javadoc-embedded">\n\n${content}\n\n</div>\n\n`
        } else if (javadocHtml) {
          // Create a link to the Javadoc
          const javadocPath = getJavadocPath(className)
          return `[📚 Documentation Javadoc pour ${className}](${opts.javadocBaseUrl}/${javadocPath})`
        } else {
          // Javadoc not found, return a placeholder
          return `<div class="javadoc-error">⚠️ Documentation Javadoc non trouvée pour <code>${className}</code></div>`
        }
      })
    },
  }
}

/**
 * Finds the Javadoc HTML file for a given class name
 */
function findJavadocHtml(className: string, javadocDir: string, ctx: BuildCtx): string | null {
  // Convert package.ClassName to package/ClassName.html
  const htmlPath = getJavadocPath(className)
  const fullPath = path.join(javadocDir, htmlPath)

  try {
    // Check if file exists
    if (fs.existsSync(fullPath)) {
      return fs.readFileSync(fullPath, "utf-8")
    }
  } catch (error) {
    console.warn(`Failed to read Javadoc for ${className}:`, error)
  }

  return null
}

/**
 * Gets the relative path to the Javadoc HTML file
 */
function getJavadocPath(className: string): string {
  return className.replace(/\./g, "/") + ".html"
}

/**
 * Extracts the main content from a Javadoc HTML file,
 * removing navigation, headers, and other wrapper elements while preserving tables
 */
function extractJavadocContent(html: string, className: string): string {
  let content = html

  // Try to find the main content block (common in modern Javadoc)
  const mainMatch = html.match(/<main[^>]*>([\s\S]*?)<\/main>/i)
  if (mainMatch) {
    content = mainMatch[1]
  }

  // Clean up Javadoc-specific elements while preserving structure
  content = content
    // Remove navigation elements
    .replace(/<nav[^>]*>[\s\S]*?<\/nav>/gi, "")
    // Remove script tags
    .replace(/<script[^>]*>[\s\S]*?<\/script>/gi, "")
    // Remove noscript tags
    .replace(/<noscript[^>]*>[\s\S]*?<\/noscript>/gi, "")
    // Remove header elements
    .replace(/<header[^>]*>[\s\S]*?<\/header>/gi, "")
    // Remove footer elements
    .replace(/<footer[^>]*>[\s\S]*?<\/footer>/gi, "")
    // Remove the skip navigation span
    .replace(/<span class="skip-nav"[^>]*>[\s\S]*?<\/span>/gi, "")
    // Remove search functionality
    .replace(/<div class="nav-list-search"[^>]*>[\s\S]*?<\/div>/gi, "")
    // Remove sub-nav (Summary/Detail navigation)
    .replace(/<div class="sub-nav"[^>]*>[\s\S]*?<\/div>/gi, "")
    // Remove top navigation bar
    .replace(/<div class="top-nav"[^>]*>[\s\S]*?<\/div>/gi, "")
    // Convert Javadoc table classes to simpler structure for better rendering
    .replace(/class="(summary-table|details-list|member-list)"/g, 'class="javadoc-table"')
    .replace(/class="(three-column-summary|two-column-summary)"/g, 'class="javadoc-grid"')
    // Simplify column classes
    .replace(/class="col-(first|second|last|constructor-name|method-name) (even|odd)-row-color[^"]*"/g, 'class="javadoc-cell"')
    .replace(/class="table-header col-(first|second|last)"/g, 'class="javadoc-header"')
    // Remove empty divs that might break layout
    .replace(/<div class="inheritance"[^>]*>[\s\S]*?<\/div>/gi, "")
    // Clean up button elements used for tabs
    .replace(/<button[^>]*>[\s\S]*?<\/button>/gi, "")
    // Remove role and aria attributes that aren't needed
    .replace(/\s*(role|aria-[a-z\-]+|tabindex|onkeydown|onclick)="[^"]*"/gi, "")
    // Clean up excessive whitespace
    .replace(/\n\s*\n\s*\n/g, "\n\n")
    .trim()

  return content
}
