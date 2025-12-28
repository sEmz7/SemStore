// src/pages/AddressesPage.tsx
import { useEffect, useMemo, useState } from "react";
import {
  createAddress,
  deleteAddress,
  listAddresses,
  updateAddress,
} from "../api/addresses";
import type { AddressCreateDto, AddressDto, AddressUpdateDto } from "../api/types";
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

export function AddressesPage() {
  const { t } = useTranslation();

  const [items, setItems] = useState<AddressDto[]>([]);
  const [form, setForm] = useState<AddressCreateDto>(empty);

  const [editingId, setEditingId] = useState<string | null>(null);

  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const isEditing = !!editingId;

  const canSave = useMemo(() => {
    return (
      form.firstname.trim() &&
      form.lastname.trim() &&
      form.patronymic.trim() &&
      form.phone.trim() &&
      form.city.trim() &&
      form.street.trim() &&
      form.building.trim() &&
      form.postalCode.trim()
    );
  }, [form]);

  async function reload() {
    setLoading(true);
    try {
      setItems(await listAddresses());
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    reload().catch(() => {});
  }, []);

  function set<K extends keyof AddressCreateDto>(key: K, value: string) {
    setForm((p) => ({ ...p, [key]: value }));
  }

  function startEdit(a: AddressDto) {
    setErr(null);
    setEditingId(a.id);

    setForm({
      firstname: (a.firstname ?? "").toString(),
      lastname: (a.lastname ?? "").toString(),
      patronymic: (a.patronymic ?? "").toString(),
      phone: (a.phone ?? "").toString(),
      city: (a.city ?? "").toString(),
      street: (a.street ?? "").toString(),
      building: (a.building ?? "").toString(),
      postalCode: (a.postalCode ?? "").toString(),
    });
  }

  function cancelEdit() {
    setEditingId(null);
    setForm(empty);
  }

  async function onSave() {
    setErr(null);
    setSaving(true);

    try {
      if (!canSave) return;

      if (editingId) {
        // если AddressUpdateDto отличается — подстрой тут
        const dto: AddressUpdateDto = {
          ...form,
        } as any;

        await updateAddress(editingId, dto);
      } else {
        await createAddress(form);
      }

      cancelEdit();
      await reload();
    } catch (e: any) {
      setErr(
        e?.response?.data?.message ??
          (editingId ? "Update address failed" : "Create address failed")
      );
    } finally {
      setSaving(false);
    }
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
          {loading ? t("common.loading") : t("common.refresh")}
        </button>
      </div>

      {err && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                        dark:border-red-900/50 dark:bg-red-950/40 dark:text-red-300">
          {err}
        </div>
      )}

      <div className="rounded-2xl border bg-white p-4 dark:bg-slate-950 dark:border-slate-800">
        <div className="flex items-center justify-between">
          <div className="text-sm font-semibold">
            {isEditing ? t("addresses.edit") : t("addresses.add")}
          </div>

          <div className="text-xs text-slate-500 dark:text-slate-400">
            {loading
              ? t("common.loading")
              : t("addresses.saved", { count: items.length })}
          </div>
        </div>

        <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-3">
          <input
            className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                       dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
            placeholder={t("addresses.firstName")}
            value={form.firstname}
            onChange={(e) => set("firstname", e.target.value)}
          />
          <input
            className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                       dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
            placeholder={t("addresses.lastName")}
            value={form.lastname}
            onChange={(e) => set("lastname", e.target.value)}
          />
          <input
            className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                       dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
            placeholder={t("addresses.patronymic")}
            value={form.patronymic}
            onChange={(e) => set("patronymic", e.target.value)}
          />
          <input
            className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                       dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
            placeholder={t("addresses.phone")}
            value={form.phone}
            onChange={(e) => set("phone", e.target.value)}
          />
          <input
            className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                       dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
            placeholder={t("addresses.city")}
            value={form.city}
            onChange={(e) => set("city", e.target.value)}
          />
          <input
            className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                       dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
            placeholder={t("addresses.street")}
            value={form.street}
            onChange={(e) => set("street", e.target.value)}
          />
          <input
            className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                       dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
            placeholder={t("addresses.building")}
            value={form.building}
            onChange={(e) => set("building", e.target.value)}
          />
          <input
            className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                       dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
            placeholder={t("addresses.postalCode")}
            value={form.postalCode}
            onChange={(e) => set("postalCode", e.target.value)}
          />
        </div>

        <div className="mt-4 flex items-center gap-2">
          <button
            disabled={!canSave || saving}
            onClick={onSave}
            className="px-4 py-2 rounded-xl text-sm font-semibold bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50 transition
                       dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
          >
            {saving ? t("common.saving") : t("common.save")}
          </button>

          {isEditing ? (
            <button
              onClick={cancelEdit}
              disabled={saving}
              className="px-4 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                         dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60 disabled:opacity-50"
            >
              {t("common.cancel")}
            </button>
          ) : (
            <button
              onClick={() => setForm(empty)}
              className="px-4 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                         dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
            >
              {t("addresses.clear")}
            </button>
          )}
        </div>
      </div>

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
            {items.map((a) => (
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
                    {a.firstname} {a.lastname} · {a.phone}
                  </div>
                </div>

                <div className="flex gap-2 shrink-0">
                  <button
                    onClick={() => startEdit(a)}
                    className="px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                               dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
                  >
                    {t("common.edit")}
                  </button>

                  <button
                    onClick={async () => {
                      setErr(null);
                      try {
                        await deleteAddress(a.id);
                        if (editingId === a.id) cancelEdit();
                        await reload();
                      } catch (e: any) {
                        setErr(e?.response?.data?.message ?? "Delete failed");
                      }
                    }}
                    className="px-3 py-2 rounded-xl text-sm font-medium border border-red-200 text-red-700 hover:bg-red-50 transition
                               dark:border-red-900/50 dark:text-red-300 dark:hover:bg-red-950/40"
                  >
                    {t("common.delete")}
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
