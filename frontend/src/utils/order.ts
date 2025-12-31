export const ORDER_NAME_MIN = 2;
export const ORDER_NAME_MAX = 255;

export function isOrderNameValid(name: string) {
  const n = name.trim();
  return n.length >= ORDER_NAME_MIN && n.length <= ORDER_NAME_MAX;
}

export function normStr(v: unknown) {
  return (v ?? "").toString().trim();
}
