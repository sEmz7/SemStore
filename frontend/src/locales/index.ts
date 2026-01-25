// src/locales/index.ts
import en from "./en";
import ru from "./ru";

export { en, ru };

export const resources = {
  en: { translation: en },
  ru: { translation: ru },
} as const;

export const supportedLngs = Object.keys(resources) as Array<keyof typeof resources>;
export const fallbackLng: keyof typeof resources = "ru";
export type AppTranslation = typeof ru;
export type SupportedLng = keyof typeof resources;