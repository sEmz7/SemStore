import { useEffect, useMemo, useState } from "react";
import { createAddress, deleteAddress, listAddresses } from "../api/addresses";
import type { AddressCreateDto, AddressDto } from "../api/types";

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
  const [items, setItems] = useState<AddressDto[]>([]);
  const [form, setForm] = useState<AddressCreateDto>(empty);
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

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

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">Addresses</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Manage delivery addresses
          </p>
        </div>

        <button
          onClick={reload}
          className="px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                     dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
        >
          Refresh
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
          <div className="text-sm font-semibold">Add address</div>
          <div className="text-xs text-slate-500 dark:text-slate-400">
            {loading ? "Loading..." : `${items.length} saved`}
          </div>
        </div>

        <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-3">
          <input
            className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                       dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
            placeholder="First name"
            value={form.firstname}
            onChange={(e) => set("firstname", e.target.value)}
          />
          <input
            className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                       dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
            placeholder="Last name"
            value={form.lastname}
            onChange={(e) => set("lastname", e.target.value)}
          />
          <input
            className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                       dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
            placeholder="Patronymic"
            value={form.patronymic}
            onChange={(e) => set("patronymic", e.target.value)}
          />
          <input
            className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                       dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
            placeholder="Phone"
            value={form.phone}
            onChange={(e) => set("phone", e.target.value)}
          />
          <input
            className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                       dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
            placeholder="City"
            value={form.city}
            onChange={(e) => set("city", e.target.value)}
          />
          <input
            className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                       dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
            placeholder="Street"
            value={form.street}
            onChange={(e) => set("street", e.target.value)}
          />
          <input
            className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                       dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
            placeholder="Building"
            value={form.building}
            onChange={(e) => set("building", e.target.value)}
          />
          <input
            className="rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                       dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
            placeholder="Postal code"
            value={form.postalCode}
            onChange={(e) => set("postalCode", e.target.value)}
          />
        </div>

        <div className="mt-4 flex items-center gap-2">
          <button
            disabled={!canSave}
            onClick={async () => {
              setErr(null);
              try {
                await createAddress(form);
                setForm(empty);
                await reload();
              } catch (e: any) {
                setErr(e?.response?.data?.message ?? "Create address failed");
              }
            }}
            className="px-4 py-2 rounded-xl text-sm font-semibold bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50 transition
                       dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
          >
            Save
          </button>

          <button
            onClick={() => setForm(empty)}
            className="px-4 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                       dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
          >
            Clear
          </button>
        </div>
      </div>

      <div className="rounded-2xl border bg-white overflow-hidden dark:bg-slate-950 dark:border-slate-800">
        <div className="px-4 py-3 border-b text-sm font-semibold dark:border-slate-800">
          Your addresses
        </div>

        {items.length === 0 ? (
          <div className="p-6 text-sm text-slate-500 dark:text-slate-400">
            No addresses yet.
          </div>
        ) : (
          <ul className="divide-y dark:divide-slate-800">
            {items.map((a) => (
              <li key={a.id} className="p-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                <div className="min-w-0">
                  <div className="font-medium">
                    {a.city}, {a.street} {a.building}
                    <span className="text-slate-500 dark:text-slate-400"> — {a.postalCode}</span>
                  </div>
                  <div className="text-sm text-slate-600 dark:text-slate-300">
                    {a.firstname} {a.lastname} · {a.phone}
                  </div>
                </div>

                <button
                  onClick={async () => {
                    await deleteAddress(a.id);
                    await reload();
                  }}
                  className="px-3 py-2 rounded-xl text-sm font-medium border hover:bg-slate-50 transition
                             dark:border-slate-800 dark:hover:bg-slate-900/60"
                >
                  delete
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
