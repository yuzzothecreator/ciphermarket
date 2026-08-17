export async function clientFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
  const response = await fetch(`${API_URL}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...init?.headers,
    },
  });

  if (!response.ok) {
    throw new Error(`API request failed: ${response.status}`);
  }

  return response.json() as Promise<T>;
}
