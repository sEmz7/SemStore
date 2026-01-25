// src/i18n.ts
import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";
import { resources, supportedLngs, fallbackLng } from "./locales";

i18n.use(LanguageDetector).use(initReactI18next).init({
  resources,
  fallbackLng,
  supportedLngs,
  debug: false,
  interpolation: { escapeValue: false },
  detection: {
    order: ["localStorage", "navigator"],
    caches: ["localStorage"],
    lookupLocalStorage: "lang",
  },
});

export default i18n;
