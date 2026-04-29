export type PitchType = 'FIVE_TURF' | 'FIVE_FUTSAL' | 'EIGHT' | 'ELEVEN';
export type BookingStatus = 'CREATED' | 'CONFIRMED' | 'CANCELLED';
export type OpenGameStatus = 'OPEN' | 'FULL' | 'CANCELLED';
export type UserRole = 'PLAYER' | 'VENUE_OWNER' | 'ADMIN';
export type EquipmentItemType = 'BALL' | 'BIBS';

export type EntityKind = 'pitch' | 'booking' | 'openGame' | 'user' | 'equipment';

export interface Pitch {
  id: number;
  name: string;
  type: PitchType;
  district: string;
  metro: string;
  pricePerHour: number;
}

export interface Booking {
  id: number;
  pitchId: number;
  organizerId: number;
  startAt: string;
  endAt: string;
  status: BookingStatus;
}

export interface OpenGame {
  id: number;
  bookingId: number;
  organizerId: number;
  targetSkillMin: number;
  targetSkillMax: number;
  maxPlayers: number;
  status: OpenGameStatus;
  participantIds: number[];
}

export interface User {
  id: number;
  name: string;
  rating: number;
  role: UserRole;
}

export interface EquipmentOffer {
  id: number;
  pitchId: number;
  itemType: EquipmentItemType;
  stockTotal: number;
  rentFixedPrice: number;
}

export interface BookingSearchFilters {
  district?: string;
  pitchType?: PitchType | '';
  organizerName?: string;
  status?: BookingStatus | '';
  startFrom?: string;
  startTo?: string;
  page?: number;
  size?: number;
}

export interface BookingSearchResponse {
  queryType: string;
  cacheHit: boolean;
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  content: Booking[];
}

export interface ApiErrorResponse {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  details?: string[];
}
