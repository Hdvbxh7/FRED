import { QuartzEmitterPlugin } from "../types"
import path from "path"
import { write } from "./helpers"
import { FilePath } from "../../util/path"

export const ExcalidrawEmitter: QuartzEmitterPlugin = () => {
  return {
    name: "ExcalidrawEmitter",
    getQuartzComponents() {
      return []
    },
    async emit(ctx, content, _resources): Promise<FilePath[]> {
      const paths: FilePath[] = []

      // Process only .excalidraw.md files
      for (const [_tree, file] of content) {
        const filePath = file.data.filePath
        if (!filePath || !filePath.endsWith(".excalidraw.md")) {
          continue
        }

        const fileContent = file.value as string

        // Extract compressed JSON from the markdown
        const jsonMatch = fileContent.match(/```compressed-json\n([\s\S]*?)\n```/)
        if (!jsonMatch || !jsonMatch[1]) {
          console.warn(`No compressed JSON found in ${filePath}`)
          continue
        }

        const compressedData = jsonMatch[1].trim()

        // Generate HTML file with Excalidraw viewer
        const htmlContent = generateExcalidrawHTML(compressedData)
        
        const outputPath = filePath.replace(".excalidraw.md", ".excalidraw.html") as FilePath
        
        await write({
          ctx,
          fileResources: [],
          vfile: { data: { filePath: outputPath } },
          content: htmlContent,
        })

        paths.push(outputPath)
      }

      return paths
    },
  }
}

function generateExcalidrawHTML(compressedData: string): string {
  return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Excalidraw Diagram</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: system-ui, -apple-system, sans-serif;
            background: #faf8f8;
            overflow: hidden;
        }
        #app {
            width: 100%;
            height: 100vh;
        }
        .error {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 100%;
            height: 100vh;
            font-size: 18px;
            color: #c92a2a;
            background: #fff;
        }
    </style>
</head>
<body>
    <div id="app"></div>
    
    <script src="https://unpkg.com/pako@2.1.0/dist/pako.min.js"><\/script>
    <script src="https://unpkg.com/react@18/umd/react.production.min.js"><\/script>
    <script src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"><\/script>
    <script src="https://unpkg.com/@excalidraw/excalidraw@0.16.1/dist/excalidraw.production.min.js"><\/script>
    
    <script>
        (async function() {
            try {
                const container = document.getElementById('app');
                
                // Decompress the data
                const compressed = '${compressedData}';
                const binaryString = atob(compressed);
                const bytes = new Uint8Array(binaryString.length);
                for (let i = 0; i < binaryString.length; i++) {
                    bytes[i] = binaryString.charCodeAt(i);
                }
                
                const decompressed = pako.inflate(bytes, { to: 'string' });
                const data = JSON.parse(decompressed);
                
                // Render with Excalidraw
                const Excalidraw = window.ExcalidrawLib.Excalidraw;
                const React = window.React;
                const ReactDOM = window.ReactDOM;
                
                const excalidrawComponent = React.createElement(Excalidraw, {
                    onChange: () => {},
                    initialData: data,
                    viewModeEnabled: true,
                    zenModeEnabled: false,
                    gridModeEnabled: true,
                });
                
                ReactDOM.render(excalidrawComponent, container);
            } catch (error) {
                console.error('Error rendering Excalidraw diagram:', error);
                document.getElementById('app').innerHTML = 
                    '<div class="error">Error loading diagram: ' + error.message + '</div>';
            }
        })();
    <\/script>
</body>
</html>`
}

export default ExcalidrawEmitter
