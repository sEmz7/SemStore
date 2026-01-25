function getScrollOffset() {
  const base = window.innerWidth < 640 ? 152 : 104; // mobile / desktop base

  const header = document.querySelector<HTMLElement>('header,[role="banner"]') ?? null;
  if (!header) return base;

  const st = getComputedStyle(header);
  const pos = st.position;
  const top = Number.parseFloat(st.top || "0");

  if ((pos === "fixed" || pos === "sticky") && top === 0) {
    const h = Math.round(header.getBoundingClientRect().height);
    return Math.max(base, h + 16);
  }

  return base;
}

export function scrollToElement(el: HTMLElement, behavior: ScrollBehavior = "smooth") {
  const offset = getScrollOffset();
  const rect = el.getBoundingClientRect();
  const targetTop = rect.top + window.scrollY - offset;
  window.scrollTo({ top: Math.max(0, targetTop), behavior });
}
