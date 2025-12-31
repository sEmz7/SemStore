export function cn(...a: Array<string | false | null | undefined>) {
  return a.filter(Boolean).join(" ");
}
