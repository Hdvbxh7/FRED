import { QuartzTransformerPlugin } from "../types"
import { Root } from "hast"
import { visit } from "unist-util-visit"

export interface Options {
  height: string
  frameborder: string
}

const defaultOptions: Options = {
  height: "600px",
  frameborder: "0",
}

export const Excalidraw: QuartzTransformerPlugin<Partial<Options>> = (opts) => {
  const options: Options = { ...defaultOptions, ...opts }

  return {
    name: "Excalidraw",
    htmlPlugins() {
      return [
        () => (tree: Root) => {
          visit(tree, "element", (node, _index, parent) => {
            // Look for transclude blockquotes with excalidraw URLs
            if (node.tagName === "blockquote" && node.properties?.className?.includes("transclude")) {
              const dataUrl = node.properties?.["data-url"] as string
              if (dataUrl && dataUrl.includes(".excalidraw")) {
                // Convert URL: "Architecture.excalidraw" -> "Architecture.excalidraw.html"
                const iframeUrl = dataUrl.startsWith("/") ? dataUrl : `/${dataUrl}`
                const htmlUrl = iframeUrl.endsWith(".html") ? iframeUrl : `${iframeUrl}.html`

                // Replace the blockquote with an iframe
                Object.assign(node, {
                  type: "element",
                  tagName: "iframe",
                  properties: {
                    src: htmlUrl,
                    width: "100%",
                    height: options.height,
                    frameborder: options.frameborder,
                    style: "border: 1px solid var(--gray); border-radius: 4px;",
                  },
                  children: [],
                })
              }
            }
          })
        },
      ]
    },
  }
}
