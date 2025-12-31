// src/types/react-i18next.d.ts
import "react-i18next";
import type { AppTranslation } from "../locales";

declare module "react-i18next" {
  interface CustomTypeOptions {
    defaultNS: "translation";
    resources: AppTranslation;
  }
}
