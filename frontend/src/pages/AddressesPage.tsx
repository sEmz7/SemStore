import { useEffect, useMemo, useRef, useState } from "react";
import { createAddress, deleteAddress, listAddresses, updateAddress } from "../api/addresses";
import type { AddressCreateDto, AddressDto } from "../api/types";
import { useTranslation } from "react-i18next";
import ConfirmDialog from "../components/ConfirmDialog";
import FormField from "../components/FormField";
import { cn } from "../utils/cn";
import { scrollToElement } from "../utils/scroll";
import {
  addressDtoToForm,
  formatRuPhone,
  normalizeForCompare,
  sameForm,
  validateAddress,
} from "../utils/address";
import { ADDRESS_FIELDS, EMPTY_ADDRESS_FORM, TOUCHED_ALL, TOUCHED_EMPTY } from "../utils/addressFormSchema";

const MAX_ADDRESSES = 10;

function normalizeCreateAddressError(msg: unknown, t: (k: string, opt?: any) => string) {
  const s = typeof msg === "string" ? msg : "";
  const m = s.toLowerCase();

  if (m.includes("only 10 address") || m.includes("can have only 10")) {
    return t("errors.maxAddresses", { max: MAX_ADDRESSES });
  }

  return s || t("errors.createAddressFail");
}

export function AddressesPage() {
  const { t } = useTranslation();

  const [items, setItems] = useState<AddressDto[]>([]);
  const [form, setForm] = useState<AddressCreateDto>(EMPTY_ADDRESS_FORM);

  const [editingId, setEditingId] = useState<string | null>(null);
  const [initialEditForm, setInitialEditForm] = useState<AddressCreateDto | null>(null);

  const [touched, setTouched] = useState(TOUCHED_EMPTY);

  const [pageError, setPageError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [confirmDelete, setConfirmDelete] = useState<{ id: string; address: string } | null>(null);

  const formHeaderRef = useRef<HTMLDivElement | null>(null);

  const isEditMode = !!editingId;
  const limitReached = !isEditMode && items.length >= MAX_ADDRESSES;

  const fieldErrors = useMemo(() => validateAddress(form, t), [form, t]);
  const isValid = useMemo(() => Object.keys(fieldErrors).length === 0, [fieldErrors]);

  const isDirtyEdit = useMemo(() => {
    if (!editingId || !initialEditForm) return false;
    return !sameForm(form, initialEditForm);
  }, [editingId, initialEditForm, form]);

  const canSave = !saving && isValid && (isEditMode ? isDirtyEdit : !limitReached);

  function setField<K extends keyof AddressCreateDto>(key: K, value: AddressCreateDto[K]) {
    setForm((p) => ({ ...p, [key]: value }));
  }

  function resetForm() {
    setEditingId(null);
    setInitialEditForm(null);
    setForm(EMPTY_ADDRESS_FORM);
    setTouched(TOUCHED_EMPTY);
    setPageError(null);
  }

  function scrollToForm() {
    const el = formHeaderRef.current;
    if (!el) return;
    scrollToElement(el);
    setTimeout(() => scrollToElement(el), 120);
  }

  async function reload() {
    setLoading(true);
    try {
      setPageError(null);
      setItems(await listAddresses());
    } catch (e: any) {
      setPageError(e?.response?.data?.message ?? t("errors.loadAddressesFail"));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    reload().catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function startEdit(a: AddressDto) {
    setPageError(null);
    setEditingId(a.id);

    const next = addressDtoToForm(a);
    setForm(next);
    setInitialEditForm(next);
    setTouched(TOUCHED_EMPTY);

    setTimeout(scrollToForm, 0);
  }

  async function submit() {
    setPageError(null);
    setTouched(TOUCHED_ALL);

    if (!isValid) {
      setPageError(t("errors.fixForm"));
      return;
    }

    if (!isEditMode && limitReached) {
      setPageError(t("errors.maxAddresses", { max: MAX_ADDRESSES }));
      return;
    }

    if (isEditMode && !isDirtyEdit) return;

    setSaving(true);
    try {
      const dto = normalizeForCompare(form);
      if (editingId) await updateAddress(editingId, dto as any);
      else await createAddress(dto);

      resetForm();
      await reload();
    } catch (e: any) {
      const msg = e?.response?.data?.message ?? e?.message;
      setPageError(normalizeCreateAddressError(msg, t));
    } finally {
      setSaving(false);
    }
  }

  async function doDeleteAddress(addressId: string) {
    setPageError(null);
    setDeletingId(addressId);
    try {
      await deleteAddress(addressId);
      await reload();
      if (editingId === addressId) resetForm();
    } catch (e: any) {
      setPageError(e?.response?.data?.message ?? t("errors.deleteFail"));
    } finally {
      setDeletingId(null);
    }
  }

  const bannerText = pageError ? pageError : limitReached ? t("errors.maxAddresses", { max: MAX_ADDRESSES }) : null;
  const bannerCls = pageError
    ? "border-red-200 bg-red-50 text-red-700 dark:border-red-900/50 dark:bg-red-950/40 dark:text-red-300"
    : "border-amber-200 bg-amber-50 text-amber-800 dark:border-amber-900/40 dark:bg-amber-950/40 dark:text-amber-200";

  return (
    <div className="space-y-6">
      {/* header */}
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold">{t("addresses.title")}</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">{t("addresses.subtitle")}</p>
        </div>

        <button
          onClick={reload}
          className="w-full sm:w-auto px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                     dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
        >
          {t("common.refresh")}
        </button>
      </div>

      {!!bannerText && <div className={cn("rounded-xl border px-4 py-3 text-sm", bannerCls)}>{bannerText}</div>}

      {/* form */}
      <div className="rounded-2xl border bg-white p-4 dark:bg-slate-950 dark:border-slate-800">
        <div ref={formHeaderRef} className="flex items-center justify-between gap-3">
          <div className="text-sm font-semibold">{isEditMode ? t("addresses.edit") : t("addresses.add")}</div>

          <div className="text-xs text-slate-500 dark:text-slate-400 shrink-0">
            {loading ? t("common.loading") : t("addresses.saved", { count: items.length })}
          </div>
        </div>

        <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-3">
          {ADDRESS_FIELDS.map((f) => {
            const label = t(f.labelKey);
            const placeholder = f.placeholder ? f.placeholder() : label;

            const value = f.viewValue ? f.viewValue(form) : (form[f.key] ?? "").toString();

            const errText = touched[f.key] ? fieldErrors[f.key] : null;

            return (
              <FormField
                key={f.key}
                label={label}
                placeholder={placeholder}
                value={value}
                inputMode={f.inputMode}
                onFocus={() => f.onFocus?.(form, setField as any)}
                onChange={(raw) => {
                  const next = f.normalizeIn ? f.normalizeIn(raw) : raw;
                  setField(f.key as any, next as any);
                }}
                onBlur={() => setTouched((p) => ({ ...p, [f.key]: true }))}
                error={errText}
              />
            );
          })}
        </div>

        <div className="mt-4 flex flex-wrap items-center gap-2">
          <button
            disabled={!canSave}
            onClick={submit}
            className="w-full sm:w-auto px-4 py-2 rounded-xl text-sm font-semibold bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50 transition
                       dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
          >
            {saving ? t("common.saving") : t("common.save")}
          </button>

          {isEditMode ? (
            <button
              disabled={saving}
              onClick={resetForm}
              className="w-full sm:w-auto px-4 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                         dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
            >
              {t("common.cancel")}
            </button>
          ) : (
            <button
              onClick={() => {
                setForm(EMPTY_ADDRESS_FORM);
                setTouched(TOUCHED_EMPTY);
                setPageError(null);
              }}
              className="w-full sm:w-auto px-4 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                         dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
            >
              {t("common.clear")}
            </button>
          )}
        </div>

        {isEditMode && !isDirtyEdit && isValid && (
          <div className="mt-2 text-xs text-slate-500 dark:text-slate-400">{t("errors.noChangesToSave")}</div>
        )}
      </div>

      {/* list */}
      <div className="rounded-2xl border bg-white overflow-hidden dark:bg-slate-950 dark:border-slate-800">
        <div className="px-4 py-3 border-b text-sm font-semibold dark:border-slate-800">
          {t("addresses.yourAddresses")}
        </div>

        {items.length === 0 ? (
          <div className="p-6 text-sm text-slate-500 dark:text-slate-400">{t("addresses.noAddresses")}</div>
        ) : (
          <ul className="divide-y dark:divide-slate-800">
            {items.map((a) => {
              const isDeleting = deletingId === a.id;
              const addrLabel = `${a.city}, ${a.street} ${a.building} — ${a.postalCode}`;
              const phone = formatRuPhone((a as any).phone ?? a.phone ?? "") || ((a as any).phone ?? a.phone ?? "");

              return (
                <li key={a.id} className="p-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                  <div className="min-w-0">
                    <div className="font-medium break-words">
                      {a.city}, {a.street} {a.building}
                      <span className="text-slate-500 dark:text-slate-400"> — {a.postalCode}</span>
                    </div>
                    <div className="text-sm text-slate-600 dark:text-slate-300 break-words">
                      {a.firstname} {a.lastname} · {phone}
                    </div>
                  </div>

                  <div className="grid grid-cols-2 sm:flex gap-2 shrink-0">
                    <button
                      onClick={() => startEdit(a)}
                      className="w-full sm:w-auto px-3 py-2 rounded-xl text-sm font-medium border hover:bg-slate-50 transition
                                 dark:border-slate-800 dark:hover:bg-slate-900/60"
                    >
                      {t("common.edit")}
                    </button>

                    <button
                      disabled={isDeleting}
                      onClick={() => {
                        setPageError(null);
                        setConfirmDelete({ id: a.id, address: addrLabel });
                      }}
                      className="w-full sm:w-auto px-3 py-2 rounded-xl text-sm font-medium border transition disabled:opacity-50
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

      <ConfirmDialog
        open={!!confirmDelete}
        title={t("addresses.confirmDeleteTitle")}
        description={confirmDelete ? t("addresses.confirmDeleteText", { address: confirmDelete.address }) : undefined}
        confirmText={t("common.delete")}
        cancelText={t("common.cancel")}
        loading={!!confirmDelete && deletingId === confirmDelete.id}
        onClose={() => setConfirmDelete(null)}
        onConfirm={async () => {
          if (!confirmDelete) return;
          const id = confirmDelete.id;
          setConfirmDelete(null);
          await doDeleteAddress(id);
        }}
      />
    </div>
  );
}

export default AddressesPage;
