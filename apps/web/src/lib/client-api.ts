import type { ProblemDetail } from "@ciphermarket/contracts";
import { getAccessToken } from "@/lib/auth";

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  status: number;
  problem?: ProblemDetail;

  constructor(message: string, status: number, problem?: ProblemDetail) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.problem = problem;
  }
}

async function parseProblem(response: Response): Promise<ProblemDetail | undefined> {
  try {
    return (await response.json()) as ProblemDetail;
  } catch {
    return undefined;
  }
}

export async function clientFetch<T>(
  path: string,
  init?: RequestInit & { token?: string | null; json?: unknown },
): Promise<T> {
  const token = init?.token ?? getAccessToken();
  const headers = new Headers(init?.headers);

  if (init?.json !== undefined) {
    headers.set("Content-Type", "application/json");
  } else if (!headers.has("Content-Type") && !(init?.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${API_URL}${path}`, {
    ...init,
    headers,
    body: init?.json !== undefined ? JSON.stringify(init.json) : init?.body,
  });

  if (!response.ok) {
    const problem = await parseProblem(response);
    throw new ApiError(
      problem?.detail ?? problem?.title ?? `API request failed (${response.status})`,
      response.status,
      problem,
    );
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export async function clientUpload<T>(
  path: string,
  file: File,
  token?: string | null,
): Promise<T> {
  const form = new FormData();
  form.append("file", file);

  return clientFetch<T>(path, {
    method: "POST",
    body: form,
    token,
  });
}

export function getApiUrl(): string {
  return API_URL;
}
