import type {
  ApiErrorResponse,
  Booking,
  BookingSearchFilters,
  BookingSearchResponse,
  EquipmentOffer,
  OpenGame,
  Pitch,
  User,
} from './types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api/v1';

type JsonPayload = Record<string, unknown>;

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  const text = await response.text();
  const data = text ? JSON.parse(text) : undefined;

  if (!response.ok) {
    const error = data as ApiErrorResponse | undefined;
    const details = error?.details?.length ? ` ${error.details.join(' ')}` : '';
    throw new Error(`${error?.message ?? response.statusText}${details}`);
  }

  return data as T;
}

function toQuery(filters: BookingSearchFilters): string {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      params.set(key, String(value));
    }
  });
  return params.toString();
}

function body(payload: JsonPayload): RequestInit {
  return {
    body: JSON.stringify(payload),
  };
}

export const api = {
  listPitches: (district = '') => {
    const query = district.trim() ? `?district=${encodeURIComponent(district.trim())}` : '';
    return request<Pitch[]>(`/pitches${query}`);
  },
  createPitch: (payload: JsonPayload) => request<Pitch>('/pitches', { method: 'POST', ...body(payload) }),
  updatePitch: (id: number, payload: JsonPayload) => request<Pitch>(`/pitches/${id}`, { method: 'PUT', ...body(payload) }),
  deletePitch: (id: number) => request<void>(`/pitches/${id}`, { method: 'DELETE' }),

  listBookings: () => request<Booking[]>('/bookings'),
  searchBookings: (filters: BookingSearchFilters) => request<BookingSearchResponse>(`/bookings/search/jpql?${toQuery(filters)}`),
  createBooking: (payload: JsonPayload) => request<Booking>('/bookings', { method: 'POST', ...body(payload) }),
  updateBooking: (id: number, payload: JsonPayload) => request<Booking>(`/bookings/${id}`, { method: 'PUT', ...body(payload) }),
  deleteBooking: (id: number) => request<void>(`/bookings/${id}`, { method: 'DELETE' }),

  listOpenGames: () => request<OpenGame[]>('/open-games'),
  createOpenGame: (payload: JsonPayload) => request<OpenGame>('/open-games', { method: 'POST', ...body(payload) }),
  updateOpenGame: (id: number, payload: JsonPayload) => request<OpenGame>(`/open-games/${id}`, { method: 'PUT', ...body(payload) }),
  deleteOpenGame: (id: number) => request<void>(`/open-games/${id}`, { method: 'DELETE' }),

  listUsers: () => request<User[]>('/users'),
  createUser: (payload: JsonPayload) => request<User>('/users', { method: 'POST', ...body(payload) }),
  updateUser: (id: number, payload: JsonPayload) => request<User>(`/users/${id}`, { method: 'PUT', ...body(payload) }),
  deleteUser: (id: number) => request<void>(`/users/${id}`, { method: 'DELETE' }),

  listEquipmentOffers: () => request<EquipmentOffer[]>('/equipment-offers'),
  createEquipmentOffer: (payload: JsonPayload) => request<EquipmentOffer>('/equipment-offers', { method: 'POST', ...body(payload) }),
  updateEquipmentOffer: (id: number, payload: JsonPayload) => {
    return request<EquipmentOffer>(`/equipment-offers/${id}`, { method: 'PUT', ...body(payload) });
  },
  deleteEquipmentOffer: (id: number) => request<void>(`/equipment-offers/${id}`, { method: 'DELETE' }),
};
