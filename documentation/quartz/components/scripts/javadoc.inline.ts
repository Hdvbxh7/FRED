// Javadoc tab functionality
// This script handles the interactive tabs in embedded Javadoc tables

const evenRowColor = "even-row-color"
const oddRowColor = "odd-row-color"
const activeTableTab = "active-table-tab"
const tableTab = "table-tab"

function toggleStyle(classList: DOMTokenList, condition: boolean, trueStyle: string, falseStyle: string) {
  if (condition) {
    classList.remove(falseStyle)
    classList.add(trueStyle)
  } else {
    classList.remove(trueStyle)
    classList.add(falseStyle)
  }
}

// Shows the elements of a table belonging to a specific category
function showJavadocTab(tableId: string, selected: string, columns: number) {
  if (tableId !== selected) {
    document.querySelectorAll(`div.${tableId}:not(.${selected})`).forEach((elem) => {
      ;(elem as HTMLElement).style.display = "none"
    })
  }
  document.querySelectorAll(`div.${selected}`).forEach((elem, index) => {
    ;(elem as HTMLElement).style.display = ""
    const isEvenRow = index % (columns * 2) < columns
    toggleStyle(elem.classList, isEvenRow, evenRowColor, oddRowColor)
  })
  updateJavadocTabs(tableId, selected)
}

function updateJavadocTabs(tableId: string, selected: string) {
  const tabpanel = document.getElementById(`${tableId}.tabpanel`)
  if (tabpanel) {
    tabpanel.setAttribute("aria-labelledby", selected)
  }
  
  document.querySelectorAll<HTMLButtonElement>(`button[id^="${tableId}"]`).forEach((tab, index) => {
    if (selected === tab.id || (tableId === selected && index === 0)) {
      tab.className = activeTableTab
      tab.setAttribute("aria-selected", "true")
      tab.setAttribute("tabindex", "0")
    } else {
      tab.className = tableTab
      tab.setAttribute("aria-selected", "false")
      tab.setAttribute("tabindex", "-1")
    }
  })
}

function switchJavadocTab(e: KeyboardEvent) {
  const selected = document.querySelector<HTMLElement>("[aria-selected=true]")
  if (selected) {
    if (
      (e.keyCode === 37 || e.keyCode === 38) &&
      selected.previousElementSibling
    ) {
      // left or up arrow key pressed: move focus to previous tab
      ;(selected.previousElementSibling as HTMLElement).click()
      ;(selected.previousElementSibling as HTMLElement).focus()
      e.preventDefault()
    } else if (
      (e.keyCode === 39 || e.keyCode === 40) &&
      selected.nextElementSibling
    ) {
      // right or down arrow key pressed: move focus to next tab
      ;(selected.nextElementSibling as HTMLElement).click()
      ;(selected.nextElementSibling as HTMLElement).focus()
      e.preventDefault()
    }
  }
}

// Make functions available globally for onclick handlers in Javadoc HTML
;(window as any).showJavadocTab = showJavadocTab
;(window as any).switchJavadocTab = switchJavadocTab

// Initialize Javadoc tabs on page load
document.addEventListener("nav", () => {
  // Fix onclick handlers to use the new function names
  document.querySelectorAll<HTMLButtonElement>(".javadoc-embedded button[onclick]").forEach((button) => {
    const onclickAttr = button.getAttribute("onclick")
    if (onclickAttr && onclickAttr.includes("show(")) {
      // Replace show() calls with showJavadocTab()
      const newOnclick = onclickAttr.replace(/show\(/g, "showJavadocTab(")
      button.setAttribute("onclick", newOnclick)
    }
  })
  
  // Fix onkeydown handlers
  document.querySelectorAll<HTMLButtonElement>(".javadoc-embedded button[onkeydown]").forEach((button) => {
    const onkeydownAttr = button.getAttribute("onkeydown")
    if (onkeydownAttr && onkeydownAttr.includes("switchTab")) {
      const newOnkeydown = onkeydownAttr.replace(/switchTab/g, "switchJavadocTab")
      button.setAttribute("onkeydown", newOnkeydown)
    }
  })
})
