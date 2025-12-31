import type { HTMLAttributes } from "react";
import type { AddressCreateDto } from "../api/types";
import { formatRuPhone, normalizePostal, normalizeRuPhone, stripDigits, type AddressFormKey } from "./address";

export type AddressFieldSchema = {
  key: AddressFormKey;
  labelKey: string;
  placeholder?: () => string;
  inputMode?: HTMLAttributes<HTMLInputElement>["inputMode"];
  viewValue?: (form: AddressCreateDto) => string;
  normalizeIn?: (raw: string) => string;
  onFocus?: (form: AddressCreateDto, setField: (k: AddressFormKey, v: string) => void) => void;
};

export const EMPTY_ADDRESS_FORM: AddressCreateDto = {
  firstname: "",
  lastname: "",
  patronymic: "",
  phone: "",
  city: "",
  street: "",
  building: "",
  postalCode: "",
};

export const TOUCHED_EMPTY: Record<AddressFormKey, boolean> = {
  firstname: false,
  lastname: false,
  patronymic: false,
  phone: false,
  city: false,
  street: false,
  building: false,
  postalCode: false,
};

export const TOUCHED_ALL: Record<AddressFormKey, boolean> = {
  firstname: true,
  lastname: true,
  patronymic: true,
  phone: true,
  city: true,
  street: true,
  building: true,
  postalCode: true,
};

export const ADDRESS_FIELDS: AddressFieldSchema[] = [
  { key: "firstname", labelKey: "addresses.firstName", normalizeIn: stripDigits },
  { key: "lastname", labelKey: "addresses.lastName", normalizeIn: stripDigits },
  { key: "patronymic", labelKey: "addresses.patronymic", normalizeIn: stripDigits },

  {
    key: "phone",
    labelKey: "addresses.phone",
    placeholder: () => "+7 (900) 000-00-00",
    inputMode: "tel",
    viewValue: (f) => formatRuPhone(f.phone),
    normalizeIn: normalizeRuPhone,
    onFocus: (f, setField) => {
      if (!f.phone) setField("phone", "7");
    },
  },

  { key: "city", labelKey: "addresses.city", normalizeIn: stripDigits },
  { key: "street", labelKey: "addresses.street", normalizeIn: stripDigits },
  { key: "building", labelKey: "addresses.building" },

  {
    key: "postalCode",
    labelKey: "addresses.postalCode",
    inputMode: "numeric",
    normalizeIn: normalizePostal,
  },
];
