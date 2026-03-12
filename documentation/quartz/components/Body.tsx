// @ts-ignore
import clipboardScript from "./scripts/clipboard.inline"
// @ts-ignore
import javadocScript from "./scripts/javadoc.inline"
import clipboardStyle from "./styles/clipboard.scss"
import { QuartzComponent, QuartzComponentConstructor, QuartzComponentProps } from "./types"

const Body: QuartzComponent = ({ children }: QuartzComponentProps) => {
  return <div id="quartz-body">{children}</div>
}

// Combine both scripts
Body.afterDOMLoaded = clipboardScript + "\n;\n" + javadocScript
Body.css = clipboardStyle

export default (() => Body) satisfies QuartzComponentConstructor
