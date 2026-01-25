import type { AddressCreateDto, AddressDto } from "../api/types";
import type { TFunction } from "i18next";

export type AddressFormKey = keyof AddressCreateDto;

function onlyDigits(v: string) {
  return (v ?? "").replace(/\D/g, "");
}

export function stripDigits(v: string) {
  return (v ?? "").replace(/\d/g, "");
}

export function normalizeRuPhone(input: string) {
  let d = onlyDigits(input);
  if (!d) return "";

  if (d.startsWith("8")) d = "7" + d.slice(1);
  if (d.length === 10 && d.startsWith("9")) d = "7" + d;

  d = d.slice(0, 11);
  if (!d.startsWith("7")) d = "7" + d.slice(0, 10);

  return d;
}

export function formatRuPhone(digitsOnly: string) {
  const d = normalizeRuPhone(digitsOnly);
  if (!d) return "";

  const a = d.slice(1);
  const p1 = a.slice(0, 3);
  const p2 = a.slice(3, 6);
  const p3 = a.slice(6, 8);
  const p4 = a.slice(8, 10);

  let out = "+7";
  if (p1.length) out += " (" + p1;
  if (p1.length === 3) out += ")";
  if (p2.length) out += " " + p2;
  if (p3.length) out += "-" + p3;
  if (p4.length) out += "-" + p4;

  return out;
}

export function normalizePostal(input: string) {
  return onlyDigits(input).slice(0, 6);
}

export function normalizeForCompare(f: AddressCreateDto): AddressCreateDto {
  return {
    firstname: stripDigits((f.firstname ?? "").toString()).trim(),
    lastname: stripDigits((f.lastname ?? "").toString()).trim(),
    patronymic: stripDigits((f.patronymic ?? "").toString()).trim(),
    phone: normalizeRuPhone((f.phone ?? "").toString()),
    city: stripDigits((f.city ?? "").toString()).trim(),
    street: stripDigits((f.street ?? "").toString()).trim(),
    building: (f.building ?? "").toString().trim(),
    postalCode: normalizePostal((f.postalCode ?? "").toString()),
  };
}

export function sameForm(a: AddressCreateDto, b: AddressCreateDto) {
  return JSON.stringify(normalizeForCompare(a)) === JSON.stringify(normalizeForCompare(b));
}

export function addressDtoToForm(a: AddressDto): AddressCreateDto {
  return {
    firstname: stripDigits((a.firstname ?? "").toString()),
    lastname: stripDigits((a.lastname ?? "").toString()),
    patronymic: stripDigits((a.patronymic ?? "").toString()),
    phone: normalizeRuPhone((a.phone ?? "").toString()),
    city: stripDigits((a.city ?? "").toString()),
    street: stripDigits((a.street ?? "").toString()),
    building: (a.building ?? "").toString(),
    postalCode: normalizePostal((a.postalCode ?? "").toString()),
  };
}

export function validateAddress(f: AddressCreateDto, t: TFunction) {
  const e: Partial<Record<AddressFormKey, string>> = {};

  const digitsNotAllowed = t("errors.digitsNotAllowed", { defaultValue: "Цифры недопустимы" });

  const reqNoDigits = (k: AddressFormKey, min = 2) => {
    const v = (f[k] ?? "").toString().trim();
    if (!v) e[k] = t("errors.required");
    else if (/\d/.test(v)) e[k] = digitsNotAllowed;
    else if (v.length < min) e[k] = t("errors.minLength", { min });
  };

  reqNoDigits("firstname", 2);
  reqNoDigits("lastname", 2);
  reqNoDigits("patronymic", 2);

  const ph = normalizeRuPhone(f.phone);
  if (!ph) e.phone = t("errors.required");
  else if (ph.length !== 11 || !ph.startsWith("7")) e.phone = t("errors.phoneInvalid");

  reqNoDigits("city", 2);
  reqNoDigits("street", 2);

  const b = (f.building ?? "").toString().trim();
  if (!b) e.building = t("errors.required");

  const pc = normalizePostal(f.postalCode);
  if (!pc) e.postalCode = t("errors.required");
  else if (pc.length !== 6) e.postalCode = t("errors.postalInvalid");

  return e;
}
