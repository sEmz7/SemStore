import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import LanguageSwitcher from "./LanguageSwitcher";

function cx(...classes: Array<string | false | undefined | null>) {
  return classes.filter(Boolean).join(" ");
}

function getInitialTheme(): "light" | "dark" {
  const saved = localStorage.getItem("theme");
  if (saved === "dark" || saved === "light") return saved;
  return "light";
}

export function Layout({ children }: { children: React.ReactNode }) {
  const nav = useNavigate();
  const { user, logout } = useAuth();
  const { t } = useTranslation();

  const [theme, setTheme] = useState<"light" | "dark">(getInitialTheme);

  useEffect(() => {
    const root = document.documentElement;
    if (theme === "dark") root.classList.add("dark");
    else root.classList.remove("dark");
    localStorage.setItem("theme", theme);
  }, [theme]);

  const linkClass = useMemo(
    () =>
      ({ isActive }: { isActive: boolean }) =>
        cx(
          "px-3 py-2 rounded-xl text-sm font-medium transition",
          isActive
            ? "bg-slate-900 text-white dark:bg-white dark:text-slate-900"
            : "text-slate-700 hover:bg-slate-100 dark:text-slate-200 dark:hover:bg-slate-900/50"
        ),
    []
  );

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 dark:bg-slate-950 dark:text-slate-50">
      <header className="sticky top-0 z-10 border-b bg-white/80 backdrop-blur dark:bg-slate-950/70 dark:border-slate-800">
        <div className="mx-auto max-w-5xl px-4 py-3 flex items-center gap-3">
          <Link to="/" className="font-semibold tracking-tight">
            semstore
          </Link>

          <nav className="flex items-center gap-1">
            <NavLink to="/" className={linkClass} end>
              {t("nav.orders")}
            </NavLink>
            <NavLink to="/addresses" className={linkClass}>
              {t("nav.addresses")}
            </NavLink>
            <NavLink to="/profile" className={linkClass}>
              {t("nav.profile")}
            </NavLink>
          </nav>

          <div className="ml-auto flex items-center gap-2">
            {/* Language */}
            <LanguageSwitcher />

            {/* Theme */}
            <button
              onClick={() => setTheme((th) => (th === "dark" ? "light" : "dark"))}
              className="px-3 py-2 rounded-xl text-sm border bg-white hover:bg-slate-50 transition
                         dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
              title="Toggle theme"
            >
              {theme === "dark" ? `🌙 ${t("nav.themeDark")}` : `☀️ ${t("nav.themeLight")}`}
            </button>

            {user ? (
              <button
                onClick={() => {
                  logout();
                  nav("/login", { replace: true });
                }}
                className="px-3 py-2 rounded-xl text-sm font-semibold bg-slate-900 text-white hover:bg-slate-800 transition
                           dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
              >
                {t("nav.logout")}
              </button>
            ) : (
              <div className="flex items-center gap-1">
                <NavLink to="/login" className={linkClass}>
                  {t("nav.login")}
                </NavLink>
                <NavLink to="/register" className={linkClass}>
                  {t("nav.register")}
                </NavLink>
              </div>
            )}
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-5xl px-4 py-8">{children}</main>
    </div>
  );
}
