// src/pages/AddressesPage.tsx
import { useEffect, useMemo, useState } from "react";
import {
  createAddress,
  deleteAddress,
  listAddresses,
  updateAddress,
} from "../api/addresses";
import type { AddressCreateDto, AddressDto } from "../api/types";
import { useTranslation } from "react-i18next";

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

function normalizeRuPhone(input: string) {
  let d = onlyDigits(input);

  if (!d) return "";

  if (d.startsWith("8")) d = "7" + d.slice(1);

  if (d.length === 10 && d.startsWith("9")) d = "7" + d;

  // ограничиваем до 11 цифр
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

export function AddressesPage() {
  const { t } = useTranslation();

  const [items, setItems] = useState<AddressDto[]>([]);
  const [form, setForm] = useState<AddressCreateDto>(empty);

  const [editingId, setEditingId] = useState<string | null>(null);

  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const [touched, setTouched] = useState<Partial<Record<FormKey, boolean>>>({});
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  function set<K extends FormKey>(key: K, value: AddressCreateDto[K]) {
    setForm((p) => ({ ...p, [key]: value }));
  }

  function touch(key: FormKey) {
    setTouched((p) => ({ ...p, [key]: true }));
  }

  function validate(f: AddressCreateDto) {
    const e: Partial<Record<FormKey, string>> = {};

    const req = (k: FormKey, min = 2) => {
      const v = (f[k] ?? "").toString().trim();
      if (!v) e[k] = t("errors.required");
      else if (v.length < min) e[k] = t("errors.minLength", { min });
    };

    req("firstname", 2);
    req("lastname", 2);
    req("patronymic", 2);

    // phone
    const ph = normalizeRuPhone(f.phone);
    if (!ph) e.phone = t("errors.required");
    else if (ph.length !== 11 || !ph.startsWith("7")) e.phone = t("errors.phoneInvalid");

    req("city", 2);
    req("street", 2);

    // building можно помягче
    const b = (f.building ?? "").toString().trim();
    if (!b) e.building = t("errors.required");
    else if (b.length < 1) e.building = t("errors.minLength", { min: 1 });

    // postal code
    const pc = normalizePostal(f.postalCode);
    if (!pc) e.postalCode = t("errors.required");
    else if (pc.length !== 6) e.postalCode = t("errors.postalInvalid");

    return e;
  }

  const fieldErrors = useMemo(() => validate(form), [form]); // eslint-disable-line react-hooks/exhaustive-deps
  const isValid = useMemo(() => Object.keys(fieldErrors).length === 0, [fieldErrors]);

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

  function startEdit(a: AddressDto) {
    setErr(null);
    setEditingId(a.id);

    setForm({
      firstname: (a.firstname ?? "").toString(),
      lastname: (a.lastname ?? "").toString(),
      patronymic: (a.patronymic ?? "").toString(),
      phone: normalizeRuPhone((a.phone ?? "").toString()),
      city: (a.city ?? "").toString(),
      street: (a.street ?? "").toString(),
      building: (a.building ?? "").toString(),
      postalCode: normalizePostal((a.postalCode ?? "").toString()),
    });

    setTouched({});
  }

  function cancelEdit() {
    setEditingId(null);
    setForm(empty);
    setTouched({});
    setErr(null);
  }

  function markAllTouched() {
    const all: Partial<Record<FormKey, boolean>> = {};
    (Object.keys(empty) as FormKey[]).forEach((k) => (all[k] = true));
    setTouched(all);
  }

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">{t("addresses.title")}</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            {t("addresses.subtitle")}
          </p>
        </div>

        <button
          onClick={reload}
          className="px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                     dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
        >
          {t("common.refresh")}
        </button>
      </div>

      {err && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                        dark:border-red-900/50 dark:bg-red-950/40 dark:text-red-300">
          {err}
        </div>
      )}

      {/* FORM */}
      <div className="rounded-2xl border bg-white p-4 dark:bg-slate-950 dark:border-slate-800">
        <div className="flex items-center justify-between">
          <div className="text-sm font-semibold">
            {editingId ? t("addresses.edit") : t("addresses.add")}
          </div>

          <div className="text-xs text-slate-500 dark:text-slate-400">
            {loading ? t("common.loading") : t("addresses.saved", { count: items.length })}
          </div>
        </div>

        <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-3">
          {/* firstname */}
          <div className="grid gap-1">
            <input
              className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                         dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
              placeholder={t("addresses.firstName")}
              value={form.firstname}
              onChange={(e) => set("firstname", e.target.value)}
              onBlur={() => touch("firstname")}
            />
            {touched.firstname && fieldErrors.firstname && (
              <div className="text-xs text-red-600 dark:text-red-300">
                {fieldErrors.firstname}
              </div>
            )}
          </div>

          {/* lastname */}
          <div className="grid gap-1">
            <input
              className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                         dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
              placeholder={t("addresses.lastName")}
              value={form.lastname}
              onChange={(e) => set("lastname", e.target.value)}
              onBlur={() => touch("lastname")}
            />
            {touched.lastname && fieldErrors.lastname && (
              <div className="text-xs text-red-600 dark:text-red-300">{fieldErrors.lastname}</div>
            )}
          </div>

          {/* patronymic */}
          <div className="grid gap-1">
            <input
              className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                         dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
              placeholder={t("addresses.patronymic")}
              value={form.patronymic}
              onChange={(e) => set("patronymic", e.target.value)}
              onBlur={() => touch("patronymic")}
            />
            {touched.patronymic && fieldErrors.patronymic && (
              <div className="text-xs text-red-600 dark:text-red-300">{fieldErrors.patronymic}</div>
            )}
          </div>

          {/* phone (masked) */}
          <div className="grid gap-1">
            <input
              className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                         dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
              placeholder="+7 (900) 000-00-00"
              value={formatRuPhone(form.phone)}
              onFocus={() => {
                // если пусто — сразу ставим "7" чтобы появилось +7
                if (!form.phone) set("phone", "7");
              }}
              onChange={(e) => set("phone", normalizeRuPhone(e.target.value))}
              onBlur={() => touch("phone")}
              inputMode="tel"
            />
            {touched.phone && fieldErrors.phone && (
              <div className="text-xs text-red-600 dark:text-red-300">{fieldErrors.phone}</div>
            )}
          </div>

          {/* city */}
          <div className="grid gap-1">
            <input
              className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                         dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
              placeholder={t("addresses.city")}
              value={form.city}
              onChange={(e) => set("city", e.target.value)}
              onBlur={() => touch("city")}
            />
            {touched.city && fieldErrors.city && (
              <div className="text-xs text-red-600 dark:text-red-300">{fieldErrors.city}</div>
            )}
          </div>

          {/* street */}
          <div className="grid gap-1">
            <input
              className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                         dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
              placeholder={t("addresses.street")}
              value={form.street}
              onChange={(e) => set("street", e.target.value)}
              onBlur={() => touch("street")}
            />
            {touched.street && fieldErrors.street && (
              <div className="text-xs text-red-600 dark:text-red-300">{fieldErrors.street}</div>
            )}
          </div>

          {/* building */}
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
              <div className="text-xs text-red-600 dark:text-red-300">{fieldErrors.building}</div>
            )}
          </div>

          {/* postal */}
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

        <div className="mt-4 flex items-center gap-2">
          <button
            disabled={saving || !isValid}
            onClick={async () => {
              setErr(null);
              markAllTouched();

              const errs = validate(form);
              if (Object.keys(errs).length > 0) return;

              setSaving(true);
              try {
                const dto: AddressCreateDto = {
                  firstname: form.firstname.trim(),
                  lastname: form.lastname.trim(),
                  patronymic: form.patronymic.trim(),
                  phone: normalizeRuPhone(form.phone), // digits only
                  city: form.city.trim(),
                  street: form.street.trim(),
                  building: form.building.trim(),
                  postalCode: normalizePostal(form.postalCode),
                };

                if (editingId) {
                  await updateAddress(editingId, dto as any);
                } else {
                  await createAddress(dto);
                }

                cancelEdit();
                await reload();
              } catch (e: any) {
                setErr(e?.response?.data?.message ?? t("errors.createAddressFail"));
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
                    <div className="font-medium">
                      {a.city}, {a.street} {a.building}
                      <span className="text-slate-500 dark:text-slate-400">
                        {" "}
                        — {a.postalCode}
                      </span>
                    </div>
                    <div className="text-sm text-slate-600 dark:text-slate-300">
                      {a.firstname} {a.lastname} · {formatRuPhone((a as any).phone ?? "") || (a as any).phone}
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
                      className="px-3 py-2 rounded-xl text-sm font-medium border hover:bg-slate-50 transition
                                 disabled:opacity-50
                                 dark:border-slate-800 dark:hover:bg-slate-900/60"
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
