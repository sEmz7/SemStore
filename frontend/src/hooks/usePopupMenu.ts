import { useCallback, useEffect, useState } from "react";

type MenuPos = { id: string; top: number; left: number };

export function usePopupMenu({
  width,
  gap = 8,
  approxHeight = 96,
  rootAttr,
  popupAttr,
}: {
  width: number;
  gap?: number;
  approxHeight?: number;
  rootAttr: string;
  popupAttr: string;
}) {
  const [openId, setOpenId] = useState<string | null>(null);
  const [pos, setPos] = useState<MenuPos | null>(null);

  const close = useCallback(() => {
    setOpenId(null);
    setPos(null);
  }, []);

  const openFor = useCallback(
    (id: string, btn: HTMLElement) => {
      const rect = btn.getBoundingClientRect();
      const vw = window.innerWidth;
      const vh = window.innerHeight;

      const hasSpaceBelow = vh - rect.bottom >= approxHeight + gap;
      const top = hasSpaceBelow ? rect.bottom + gap : Math.max(gap, rect.top - approxHeight - gap);

      let left = rect.right - width;
      left = Math.max(gap, Math.min(left, vw - width - gap));

      setOpenId(id);
      setPos({ id, top, left });
    },
    [approxHeight, gap, width]
  );

  const toggleFor = useCallback(
    (id: string, btn: HTMLElement) => {
      if (openId === id) {
        close();
        return;
      }
      openFor(id, btn);
    },
    [openId, close, openFor]
  );

  useEffect(() => {
    if (!openId) return;

    const onDocPointerDown = (e: PointerEvent) => {
      const target = e.target as HTMLElement | null;
      if (!target) return;

      if (target.closest(`[${rootAttr}="${openId}"]`)) return;
      if (target.closest(`[${popupAttr}="${openId}"]`)) return;

      close();
    };

    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") close();
    };

    const onScrollOrResize = () => close();

    document.addEventListener("pointerdown", onDocPointerDown);
    document.addEventListener("keydown", onKeyDown);
    window.addEventListener("scroll", onScrollOrResize, { passive: true });
    window.addEventListener("resize", onScrollOrResize);

    return () => {
      document.removeEventListener("pointerdown", onDocPointerDown);
      document.removeEventListener("keydown", onKeyDown);
      window.removeEventListener("scroll", onScrollOrResize);
      window.removeEventListener("resize", onScrollOrResize);
    };
  }, [openId, rootAttr, popupAttr, close]);

  return { openId, pos, openFor, toggleFor, close };
}
