// src/pages/AddressesPage.tsx
import { useEffect, useMemo, useRef, useState } from "react";
import {
  createAddress,
  deleteAddress,
  listAddresses,
  updateAddress,
} from "../api/addresses";
import type { AddressCreateDto, AddressDto } from "../api/types";
import { useTranslation } from "react-i18next";

const MAX_ADDRESSES = 10;

const empty: AddressCreateDto = {
  firstname: "",
  lastname: "",
  patronymic: "",
  phone: "",
  city: "",
  street: "",
  building: "",
  postalCode: "",
};

type FormKey = keyof AddressCreateDto;

function onlyDigits(v: string) {
  return (v ?? "").replace(/\D/g, "");
}

function stripDigits(v: string) {
  return (v ?? "").replace(/\d/g, "");
}

function normalizeRuPhone(input: string) {
  let d = onlyDigits(input);

  if (!d) return "";

  if (d.startsWith("8")) d = "7" + d.slice(1);
  if (d.length === 10 && d.startsWith("9")) d = "7" + d;

  d = d.slice(0, 11);
  if (!d.startsWith("7")) d = "7" + d.slice(0, 10);

  return d;
}

function formatRuPhone(digitsOnly: string) {
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

function normalizePostal(input: string) {
  return onlyDigits(input).slice(0, 6);
}

function normalizeForCompare(f: AddressCreateDto): AddressCreateDto {
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

function sameForm(a: AddressCreateDto, b: AddressCreateDto) {
  return (
    JSON.stringify(normalizeForCompare(a)) ===
    JSON.stringify(normalizeForCompare(b))
  );
}

function normalizeCreateAddressError(
  msg: unknown,
  t: (k: string, opt?: any) => string
) {
  const s = typeof msg === "string" ? msg : "";
  const m = s.toLowerCase();

  if (m.includes("only 10 address") || m.includes("can have only 10")) {
    return t("errors.maxAddresses", { max: MAX_ADDRESSES });
  }

  return s || t("errors.createAddressFail");
}

function getScrollOffset() {
  const base = window.innerWidth < 640 ? 152 : 104;

  const header =
    document.querySelector<HTMLElement>('header,[role="banner"]') ?? null;

  if (header) {
    const st = getComputedStyle(header);
    const pos = st.position;
    const top = parseFloat(st.top || "0");
    if ((pos === "fixed" || pos === "sticky") && top === 0) {
      const h = Math.round(header.getBoundingClientRect().height);
      return Math.max(base, h + 16);
    }
  }

  return base;
}

export function AddressesPage() {
  const { t } = useTranslation();

  const [items, setItems] = useState<AddressDto[]>([]);
  const [form, setForm] = useState<AddressCreateDto>(empty);

  const [editingId, setEditingId] = useState<string | null>(null);
  const [initialEditForm, setInitialEditForm] =
    useState<AddressCreateDto | null>(null);

  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const [touched, setTouched] = useState<Partial<Record<FormKey, boolean>>>({});
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const formHeaderRef = useRef<HTMLDivElement | null>(null);

  const isEditMode = !!editingId;
  const limitReached = !isEditMode && items.length >= MAX_ADDRESSES;

  function set<K extends FormKey>(key: K, value: AddressCreateDto[K]) {
    setForm((p) => ({ ...p, [key]: value }));
  }

  function touch(key: FormKey) {
    setTouched((p) => ({ ...p, [key]: true }));
  }

  function validate(f: AddressCreateDto) {
    const e: Partial<Record<FormKey, string>> = {};

    const digitsNotAllowed = t("errors.digitsNotAllowed", {
      defaultValue: "Цифры недопустимы",
    });

    const reqNoDigits = (k: FormKey, min = 2) => {
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
    else if (ph.length !== 11 || !ph.startsWith("7"))
      e.phone = t("errors.phoneInvalid");

    reqNoDigits("city", 2);
    reqNoDigits("street", 2);

    const b = (f.building ?? "").toString().trim();
    if (!b) e.building = t("errors.required");
    else if (b.length < 1) e.building = t("errors.minLength", { min: 1 });

    const pc = normalizePostal(f.postalCode);
    if (!pc) e.postalCode = t("errors.required");
    else if (pc.length !== 6) e.postalCode = t("errors.postalInvalid");

    return e;
  }

  const fieldErrors = useMemo(() => validate(form), [form]); // eslint-disable-line react-hooks/exhaustive-deps
  const isValid = useMemo(
    () => Object.keys(fieldErrors).length === 0,
    [fieldErrors]
  );

  const isDirtyEdit = useMemo(() => {
    if (!editingId || !initialEditForm) return false;
    return !sameForm(form, initialEditForm);
  }, [editingId, initialEditForm, form]);

  const canSave =
    !saving && isValid && (isEditMode ? isDirtyEdit : !limitReached);

  async function reload() {
    setLoading(true);
    try {
      setItems(await listAddresses());
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? t("errors.loadAddressesFail"));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    reload().catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function scrollToFormHeader() {
    const el = formHeaderRef.current;
    if (!el) return;

    const offset = getScrollOffset();
    const rect = el.getBoundingClientRect();
    const targetTop = rect.top + window.scrollY - offset;

    window.scrollTo({
      top: Math.max(0, targetTop),
      behavior: "smooth",
    });
  }

  function startEdit(a: AddressDto) {
    setErr(null);
    setEditingId(a.id);

    const next: AddressCreateDto = {
      firstname: stripDigits((a.firstname ?? "").toString()),
      lastname: stripDigits((a.lastname ?? "").toString()),
      patronymic: stripDigits((a.patronymic ?? "").toString()),
      phone: normalizeRuPhone((a.phone ?? "").toString()),
      city: stripDigits((a.city ?? "").toString()),
      street: stripDigits((a.street ?? "").toString()),
      building: (a.building ?? "").toString(),
      postalCode: normalizePostal((a.postalCode ?? "").toString()),
    };

    setForm(next);
    setInitialEditForm(next);
    setTouched({});

    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        scrollToFormHeader();
        setTimeout(scrollToFormHeader, 120);
      });
    });
  }

  function cancelEdit() {
    setEditingId(null);
    setInitialEditForm(null);
    setForm(empty);
    setTouched({});
    setErr(null);
  }

  function markAllTouched() {
    const all: Partial<Record<FormKey, boolean>> = {};
    (Object.keys(empty) as FormKey[]).forEach((k) => (all[k] = true));
    setTouched(all);
  }

  const noChangesText = t("errors.noChangesToSave");

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold">{t("addresses.title")}</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            {t("addresses.subtitle")}
          </p>
        </div>

        <button
          onClick={reload}
          className="w-full sm:w-auto px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                     dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
        >
          {t("common.refresh")}
        </button>
      </div>

      {(err || limitReached) && (
        <div
          className={`rounded-xl border px-4 py-3 text-sm
            ${
              err
                ? "border-red-200 bg-red-50 text-red-700 dark:border-red-900/50 dark:bg-red-950/40 dark:text-red-300"
                : "border-amber-200 bg-amber-50 text-amber-800 dark:border-amber-900/40 dark:bg-amber-950/40 dark:text-amber-200"
            }`}
        >
          {err ? err : t("errors.maxAddresses", { max: MAX_ADDRESSES })}
        </div>
      )}

      {/* FORM */}
      <div className="rounded-2xl border bg-white p-4 dark:bg-slate-950 dark:border-slate-800">
        <div
          ref={formHeaderRef}
          className="flex items-center justify-between gap-3"
        >
          <div className="text-sm font-semibold">
            {editingId ? t("addresses.edit") : t("addresses.add")}
          </div>

          <div className="text-xs text-slate-500 dark:text-slate-400 shrink-0">
            {loading
              ? t("common.loading")
              : t("addresses.saved", { count: items.length })}
          </div>
        </div>

        <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div className="grid gap-1">
            <input
              className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                         dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
              placeholder={t("addresses.firstName")}
              value={form.firstname}
              onChange={(e) => set("firstname", stripDigits(e.target.value))}
              onBlur={() => touch("firstname")}
            />
            {touched.firstname && fieldErrors.firstname && (
              <div className="text-xs text-red-600 dark:text-red-300">
                {fieldErrors.firstname}
              </div>
            )}
          </div>

          <div className="grid gap-1">
            <input
              className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                         dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
              placeholder={t("addresses.lastName")}
              value={form.lastname}
              onChange={(e) => set("lastname", stripDigits(e.target.value))}
              onBlur={() => touch("lastname")}
            />
            {touched.lastname && fieldErrors.lastname && (
              <div className="text-xs text-red-600 dark:text-red-300">
                {fieldErrors.lastname}
              </div>
            )}
          </div>

          <div className="grid gap-1">
            <input
              className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                         dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
              placeholder={t("addresses.patronymic")}
              value={form.patronymic}
              onChange={(e) => set("patronymic", stripDigits(e.target.value))}
              onBlur={() => touch("patronymic")}
            />
            {touched.patronymic && fieldErrors.patronymic && (
              <div className="text-xs text-red-600 dark:text-red-300">
                {fieldErrors.patronymic}
              </div>
            )}
          </div>

          <div className="grid gap-1">
            <input
              className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                         dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
              placeholder="+7 (900) 000-00-00"
              value={formatRuPhone(form.phone)}
              onFocus={() => {
                if (!form.phone) set("phone", "7");
              }}
              onChange={(e) => set("phone", normalizeRuPhone(e.target.value))}
              onBlur={() => touch("phone")}
              inputMode="tel"
            />
            {touched.phone && fieldErrors.phone && (
              <div className="text-xs text-red-600 dark:text-red-300">
                {fieldErrors.phone}
              </div>
            )}
          </div>

          <div className="grid gap-1">
            <input
              className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                         dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
              placeholder={t("addresses.city")}
              value={form.city}
              onChange={(e) => set("city", stripDigits(e.target.value))}
              onBlur={() => touch("city")}
            />
            {touched.city && fieldErrors.city && (
              <div className="text-xs text-red-600 dark:text-red-300">
                {fieldErrors.city}
              </div>
            )}
          </div>

          <div className="grid gap-1">
            <input
              className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                         dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
              placeholder={t("addresses.street")}
              value={form.street}
              onChange={(e) => set("street", stripDigits(e.target.value))}
              onBlur={() => touch("street")}
            />
            {touched.street && fieldErrors.street && (
              <div className="text-xs text-red-600 dark:text-red-300">
                {fieldErrors.street}
              </div>
            )}
          </div>

          <div className="grid gap-1">
            <input
              className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                         dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
              placeholder={t("addresses.building")}
              value={form.building}
              onChange={(e) => set("building", e.target.value)}
              onBlur={() => touch("building")}
            />
            {touched.building && fieldErrors.building && (
              <div className="text-xs text-red-600 dark:text-red-300">
                {fieldErrors.building}
              </div>
            )}
          </div>

          <div className="grid gap-1">
            <input
              className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                         dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
              placeholder={t("addresses.postalCode")}
              value={form.postalCode}
              onChange={(e) => set("postalCode", normalizePostal(e.target.value))}
              onBlur={() => touch("postalCode")}
              inputMode="numeric"
            />
            {touched.postalCode && fieldErrors.postalCode && (
              <div className="text-xs text-red-600 dark:text-red-300">
                {fieldErrors.postalCode}
              </div>
            )}
          </div>
        </div>

        <div className="mt-4 flex flex-wrap items-center gap-2">
          <button
            disabled={!canSave}
            onClick={async () => {
              setErr(null);
              markAllTouched();

              const errs = validate(form);
              if (Object.keys(errs).length > 0) return;

              if (!isEditMode && limitReached) {
                setErr(t("errors.maxAddresses", { max: MAX_ADDRESSES }));
                return;
              }

              setSaving(true);
              try {
                const dto: AddressCreateDto = normalizeForCompare(form);

                if (editingId) {
                  await updateAddress(editingId, dto as any);
                } else {
                  await createAddress(dto);
                }

                cancelEdit();
                await reload();
              } catch (e: any) {
                const msg = e?.response?.data?.message ?? e?.message;
                setErr(normalizeCreateAddressError(msg, t));
              } finally {
                setSaving(false);
              }
            }}
            className="px-4 py-2 rounded-xl text-sm font-semibold bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50 transition
                       dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
          >
            {saving ? t("common.saving") : t("common.save")}
          </button>

          {editingId ? (
            <button
              disabled={saving}
              onClick={cancelEdit}
              className="px-4 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                         dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
            >
              {t("common.cancel")}
            </button>
          ) : (
            <button
              onClick={() => {
                setForm(empty);
                setTouched({});
                setErr(null);
              }}
              className="px-4 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                         dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
            >
              {t("common.clear")}
            </button>
          )}
        </div>

        {isEditMode && !isDirtyEdit && isValid && (
          <div className="mt-2 text-xs text-slate-500 dark:text-slate-400">
            {noChangesText}
          </div>
        )}
      </div>

      {/* LIST */}
      <div className="rounded-2xl border bg-white overflow-hidden dark:bg-slate-950 dark:border-slate-800">
        <div className="px-4 py-3 border-b text-sm font-semibold dark:border-slate-800">
          {t("addresses.yourAddresses")}
        </div>

        {items.length === 0 ? (
          <div className="p-6 text-sm text-slate-500 dark:text-slate-400">
            {t("addresses.noAddresses")}
          </div>
        ) : (
          <ul className="divide-y dark:divide-slate-800">
            {items.map((a) => {
              const isDeleting = deletingId === a.id;

              return (
                <li
                  key={a.id}
                  className="p-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3"
                >
                  <div className="min-w-0">
                    <div className="font-medium break-words">
                      {a.city}, {a.street} {a.building}
                      <span className="text-slate-500 dark:text-slate-400">
                        {" "}
                        — {a.postalCode}
                      </span>
                    </div>
                    <div className="text-sm text-slate-600 dark:text-slate-300 break-words">
                      {a.firstname} {a.lastname} ·{" "}
                      {formatRuPhone((a as any).phone ?? "") || (a as any).phone}
                    </div>
                  </div>

                  <div className="flex gap-2 shrink-0">
                    <button
                      onClick={() => startEdit(a)}
                      className="px-3 py-2 rounded-xl text-sm font-medium border hover:bg-slate-50 transition
                                 dark:border-slate-800 dark:hover:bg-slate-900/60"
                    >
                      {t("common.edit")}
                    </button>

                    <button
                      disabled={isDeleting}
                      onClick={async () => {
                        setErr(null);
                        setDeletingId(a.id);
                        try {
                          await deleteAddress(a.id);
                          await reload();
                          if (editingId === a.id) cancelEdit();
                        } catch (e: any) {
                          setErr(e?.response?.data?.message ?? t("errors.deleteFail"));
                        } finally {
                          setDeletingId(null);
                        }
                      }}
                      className="px-3 py-2 rounded-xl text-sm font-medium border transition disabled:opacity-50
                                 border-red-200 text-red-700 hover:bg-red-50
                                 dark:border-red-900/50 dark:text-red-300 dark:hover:bg-red-950/40"
                    >
                      {isDeleting ? t("common.deleting") : t("common.delete")}
                    </button>
                  </div>
                </li>
              );
            })}
          </ul>
        )}
      </div>
    </div>
  );
}

export default AddressesPage;
