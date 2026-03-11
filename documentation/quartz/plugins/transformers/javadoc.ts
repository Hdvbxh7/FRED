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

      // Find all code block regions first to avoid replacing inside them
      const codeBlockRanges = findCodeBlockRanges(src)

      // Pattern to match {{javadoc:ClassName}} or {{javadoc:package.ClassName}}
      const javadocPattern = /\{\{javadoc:([a-zA-Z0-9_.]+)\}\}/g

      return src.replace(javadocPattern, (match, className, offset) => {
        // Check if this match is inside a code block
        if (isInCodeBlock(offset, codeBlockRanges)) {
          return match // Return unchanged if inside code block
        }

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
 * Finds all code block ranges in markdown to avoid replacing content inside them
 * Returns array of {start, end} positions
 */
function findCodeBlockRanges(markdown: string): Array<{start: number, end: number}> {
  const ranges: Array<{start: number, end: number}> = []
  
  // Find fenced code blocks (``` or ~~~)
  const lines = markdown.split('\n')
  let currentPos = 0
  let inCodeBlock = false
  let codeBlockStart = 0
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    const lineStart = currentPos
    const lineEnd = currentPos + line.length
    
    // Check for fenced code block markers
    if (line.match(/^(```|~~~)/)) {
      if (!inCodeBlock) {
        inCodeBlock = true
        codeBlockStart = lineStart
      } else {
        inCodeBlock = false
        ranges.push({ start: codeBlockStart, end: lineEnd })
      }
    }
    
    currentPos = lineEnd + 1 // +1 for newline
  }
  
  // If still in code block at end, close it
  if (inCodeBlock) {
    ranges.push({ start: codeBlockStart, end: markdown.length })
  }
  
  // Find inline code spans - handle multiple backticks
  // Matches: `code`, ``code with ` backtick``, ```code````, etc.
  const inlineCodeRegex = /(`+)([^`]|[^`][\s\S]*?[^`])\1(?!`)/g
  let match: RegExpExecArray | null
  
  while ((match = inlineCodeRegex.exec(markdown)) !== null) {
    ranges.push({ start: match.index, end: match.index + match[0].length })
  }
  
  // Also handle simple single backtick pairs (edge case for empty or single char)
  const simpleInlineRegex = /`[^`\n]*`/g
  let simpleMatch: RegExpExecArray | null
  while ((simpleMatch = simpleInlineRegex.exec(markdown)) !== null) {
    // Check if not already covered by previous regex
    const alreadyCovered = ranges.some(r => 
      simpleMatch!.index >= r.start && simpleMatch!.index < r.end
    )
    if (!alreadyCovered) {
      ranges.push({ start: simpleMatch.index, end: simpleMatch.index + simpleMatch[0].length })
    }
  }
  
  return ranges
}

/**
 * Checks if a position is inside any of the given code block ranges
 */
function isInCodeBlock(position: number, ranges: Array<{start: number, end: number}>): boolean {
  return ranges.some(range => position >= range.start && position < range.end)
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
    // Remove empty divs that might break layout
    .replace(/<div class="inheritance"[^>]*>[\s\S]*?<\/div>/gi, "")
    // Clean up button elements used for tabs
    .replace(/<button[^>]*>[\s\S]*?<\/button>/gi, "")
    // Remove role and aria attributes that aren't needed
    .replace(/\s*(role|aria-[a-z\-]+|tabindex|onkeydown|onclick)="[^"]*"/gi, "")
    // Clean up excessive whitespace
    .replace(/\n\s*\n\s*\n/g, "\n\n")
    .trim()

  // Convert Javadoc div-based tables to proper HTML tables
  content = convertDivTablesToHtmlTables(content)

  return content
}

/**
 * Converts Javadoc's div-based grid tables to proper HTML table elements
 */
function convertDivTablesToHtmlTables(content: string): string {
  // Strategy: Find table divs and parse their direct children
  // Use a more robust matching that handles nested content
  
  // First, let's match table containers more carefully
  // Note: class attribute contains both classes separated by space: class="summary-table three-column-summary"
  
  let result = content
  const tableRegex = /<div class="(summary-table|details-list|member-list)(\s+(three-column-summary|two-column-summary|four-column-summary))?"/g
  
  let match
  const replacements: Array<{start: number, end: number, replacement: string}> = []
  
  while ((match = tableRegex.exec(content)) !== null) {
    const startPos = match.index
    const tableType = match[1]
    const columnClass = match[3] || "" // Group 3 because group 2 includes the space
    
    // Determine number of columns
    let numColumns = 2
    if (columnClass.includes("three-column")) numColumns = 3
    else if (columnClass.includes("four-column")) numColumns = 4
    
    // Find the matching closing div by counting nesting depth
    let depth = 1
    let pos = match.index + match[0].length
    
    // Skip the > after the opening tag
    while (pos < content.length && content[pos] !== '>') pos++
    pos++ // Skip the >
    
    const contentStart = pos
    
    // Count div depth to find matching close
    while (pos < content.length && depth > 0) {
      if (content.substring(pos, pos + 5) === '<div ') {
        depth++
        pos += 5
      } else if (content.substring(pos, pos + 6) === '</div>') {
        depth--
        if (depth === 0) break
        pos += 6
      } else {
        pos++
      }
    }
    
    if (depth !== 0) continue // Malformed HTML, skip
    
    const contentEnd = pos
    const innerHtml = content.substring(contentStart, contentEnd)
    
    // Parse cells from innerHtml
    const cells: Array<{content: string, isHeader: boolean}> = []
    const cellRegex = /<div class="([^"]*)">(.*?)<\/div>/gs
    let cellMatch
    
    while ((cellMatch = cellRegex.exec(innerHtml)) !== null) {
      const classes = cellMatch[1]
      const cellContent = cellMatch[2]
      
      // Only process immediate child cells
      if (classes && (classes.includes("col-") || classes.includes("table-header"))) {
        const isHeader = classes.includes("table-header")
        cells.push({ content: cellContent.trim(), isHeader })
      }
    }
    
    if (cells.length === 0) continue
    
    // Build table HTML
    let tableHtml = '<table class="javadoc-table">\n'
    
    // Headers
    const headerCells = cells.filter(c => c.isHeader)
    if (headerCells.length > 0) {
      tableHtml += '  <thead>\n    <tr>\n'
      for (const cell of headerCells) {
        tableHtml += `      <th>${cell.content}</th>\n`
      }
      tableHtml += '    </tr>\n  </thead>\n'
    }
    
    // Data rows
    const dataCells = cells.filter(c => !c.isHeader)
    if (dataCells.length > 0) {
      tableHtml += '  <tbody>\n'
      for (let i = 0; i < dataCells.length; i += numColumns) {
        tableHtml += '    <tr>\n'
        for (let j = 0; j < numColumns && i + j < dataCells.length; j++) {
          tableHtml += `      <td>${dataCells[i + j].content}</td>\n`
        }
        tableHtml += '    </tr>\n'
      }
      tableHtml += '  </tbody>\n'
    }
    
    tableHtml += '</table>'
    
    // Store replacement (we'll apply them in reverse order to preserve positions)
    replacements.push({
      start: startPos,
      end: contentEnd + 6, // +6 for </div>
      replacement: tableHtml
    })
  }
  
  // Apply replacements in reverse order
  for (let i = replacements.length - 1; i >= 0; i--) {
    const r = replacements[i]
    result = result.substring(0, r.start) + r.replacement + result.substring(r.end)
  }
  
  return result
}
