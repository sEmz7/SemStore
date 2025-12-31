// src/hooks/useOrderItemForm.ts
import { useMemo, useState } from "react";
import type { TFunction } from "i18next";
import { ORDER_ITEM_LIMITS, type OrderItemField } from "../utils/orderItemLimits";
import { validateOrderItemField } from "../utils/validation";

type FormState = Record<OrderItemField, string>;
type TouchedState = Record<OrderItemField, boolean>;

const EMPTY_FORM: FormState = { link: "", size: "", configuration: "" };
const EMPTY_TOUCHED: TouchedState = { link: false, size: false, configuration: false };

export function useOrderItemForm(t: TFunction) {
  const [form, setForm] = useState<FormState>(EMPTY_FORM);
  const [touched, setTouched] = useState<TouchedState>(EMPTY_TOUCHED);

  function setField(key: OrderItemField, value: string) {
    setForm((p) => ({ ...p, [key]: value }));
  }

  function touch(key: OrderItemField) {
    setTouched((p) => ({ ...p, [key]: true }));
  }

  function touchAll() {
    setTouched({ link: true, size: true, configuration: true });
  }

  function reset() {
    setForm(EMPTY_FORM);
    setTouched(EMPTY_TOUCHED);
  }

  const errors = useMemo(() => {
    return {
      link: validateOrderItemField("link", form.link, t),
      size: validateOrderItemField("size", form.size, t),
      configuration: validateOrderItemField("configuration", form.configuration, t),
    };
  }, [form, t]);

  const shownErrors = useMemo(() => {
    return {
      link: touched.link ? errors.link : null,
      size: touched.size ? errors.size : null,
      configuration: touched.configuration ? errors.configuration : null,
    };
  }, [errors, touched]);

  const canSubmit = useMemo(() => {
    return !errors.link && !errors.size && !errors.configuration;
  }, [errors]);

  function getPayload() {
    return {
      link: form.link.trim(),
      size: form.size.trim(),
      configuration: form.configuration.trim(),
    };
  }

  return {
    form,
    touched,
    errors,
    shownErrors,
    canSubmit,

    limits: ORDER_ITEM_LIMITS,

    setField,
    touch,
    touchAll,
    reset,
    getPayload,
  };
}
