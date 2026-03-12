import { PageLayout, SharedLayout } from "./quartz/cfg"
import * as Component from "./quartz/components"

// components shared across all pages
export const sharedPageComponents: SharedLayout = {
  head: Component.Head(),
  header: [],
  afterBody: [Component.MobileOnly(Component.MobileSidebar())],
  footer: Component.Footer({
    links: {
      "GitHub du projet": "https://github.com/Hdvbxh7/FRED",
    },
  }),
}

// components for pages that display a single page (e.g. a single note)
export const defaultContentPageLayout: PageLayout = {
  beforeBody: [
    Component.Breadcrumbs(),
    Component.ArticleTitle(),
    Component.ContentMeta(),
    Component.TagList(),
  ],
  left: [
    Component.PageTitle(),
    Component.MobileOnly(Component.Spacer()),
    Component.Search(),
    Component.Darkmode(),
    Component.MobileOnly(Component.MobileSidebarToggle()),
    Component.DesktopOnly(Component.Explorer()),
    Component.DesktopOnly(Component.TableOfContents()),
  ],
  right: [],
  afterBody: [
    Component.Graph(),
    Component.Backlinks(),
  ],
}

// components for pages that display lists of pages  (e.g. tags or folders)
export const defaultListPageLayout: PageLayout = {
  beforeBody: [Component.Breadcrumbs(), Component.ArticleTitle(), Component.ContentMeta()],
  left: [
    Component.PageTitle(),
    Component.MobileOnly(Component.Spacer()),
    Component.Search(),
    Component.Darkmode(),
    Component.MobileOnly(Component.MobileSidebarToggle()),
    Component.DesktopOnly(Component.Explorer()),
  ],
  right: [],
}
