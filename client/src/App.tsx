import {
  Activity,
  CalendarClock,
  CircleDollarSign,
  Dumbbell,
  Edit3,
  MapPin,
  Plus,
  RefreshCw,
  Search,
  Trash2,
  Trophy,
  Users,
  X,
} from 'lucide-react';
import type { FormEvent, InputHTMLAttributes, ReactNode } from 'react';
import { useEffect, useState } from 'react';
import { api } from './api';
import type {
  Booking,
  BookingSearchFilters,
  BookingSearchResponse,
  EntityKind,
  EquipmentOffer,
  OpenGame,
  Pitch,
  User,
} from './types';

type ViewKey = 'dashboard' | 'pitches' | 'bookings' | 'openGames' | 'users' | 'equipment';
type ModalMode = 'create' | 'edit';
type FormValue = string | string[];
type FormState = Record<string, FormValue>;

interface ModalState {
  kind: EntityKind;
  mode: ModalMode;
  id?: number;
  form: FormState;
}

const pitchTypes = ['FIVE_TURF', 'FIVE_FUTSAL', 'EIGHT', 'ELEVEN'] as const;
const bookingStatuses = ['CREATED', 'CONFIRMED', 'CANCELLED'] as const;
const openGameStatuses = ['OPEN', 'FULL', 'CANCELLED'] as const;
const userRoles = ['PLAYER', 'VENUE_OWNER', 'ADMIN'] as const;
const equipmentTypes = ['BALL', 'BIBS'] as const;

const pitchTypeLabels = {
  FIVE_TURF: '5x5 Turf',
  FIVE_FUTSAL: '5x5 Futsal',
  EIGHT: '8x8',
  ELEVEN: '11x11',
};

const statusLabels = {
  CREATED: 'Создано',
  CONFIRMED: 'Подтверждено',
  CANCELLED: 'Отменено',
  OPEN: 'Открыта',
  FULL: 'Набрана',
};

const roleLabels = {
  PLAYER: 'Игрок',
  VENUE_OWNER: 'Владелец',
  ADMIN: 'Админ',
};

const equipmentLabels = {
  BALL: 'Мячи',
  BIBS: 'Манишки',
};

const navItems: Array<{ key: ViewKey; label: string; short: string }> = [
  { key: 'dashboard', label: 'Главная', short: 'Home' },
  { key: 'pitches', label: 'Площадки', short: 'Pitch' },
  { key: 'bookings', label: 'Бронирования', short: 'Book' },
  { key: 'openGames', label: 'Открытые игры', short: 'Game' },
  { key: 'users', label: 'Пользователи', short: 'User' },
  { key: 'equipment', label: 'Инвентарь', short: 'Gear' },
];

const emptyBookingFilters: BookingSearchFilters = {
  district: '',
  pitchType: '',
  organizerName: '',
  status: '',
  startFrom: '',
  startTo: '',
  page: 0,
  size: 12,
};

function App() {
  const [activeView, setActiveView] = useState<ViewKey>('dashboard');
  const [pitches, setPitches] = useState<Pitch[]>([]);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [openGames, setOpenGames] = useState<OpenGame[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [equipmentOffers, setEquipmentOffers] = useState<EquipmentOffer[]>([]);
  const [pitchFilterResult, setPitchFilterResult] = useState<Pitch[] | null>(null);
  const [bookingSearchResult, setBookingSearchResult] = useState<BookingSearchResponse | null>(null);
  const [bookingFilters, setBookingFilters] = useState<BookingSearchFilters>(emptyBookingFilters);
  const [pitchDistrict, setPitchDistrict] = useState('');
  const [catalogSearch, setCatalogSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState<ModalState | null>(null);
  const [toast, setToast] = useState('');

  useEffect(() => {
    void loadAll();
  }, []);

  const pitchMap = new Map(pitches.map((pitch) => [pitch.id, pitch]));
  const userMap = new Map(users.map((user) => [user.id, user]));
  const bookingMap = new Map(bookings.map((booking) => [booking.id, booking]));
  const normalizedSearch = catalogSearch.trim().toLowerCase();
  const displayedBookings = bookingSearchResult?.content ?? bookings;
  const displayedPitches = pitchFilterResult ?? pitches;

  const searchedPitches = pitches.filter((pitch) => {
    return matchesSearch([pitch.name, pitch.district, pitch.metro, pitch.type], normalizedSearch);
  });
  const filteredPitches = displayedPitches.filter((pitch) => {
    return matchesSearch([pitch.name, pitch.district, pitch.metro, pitch.type], normalizedSearch);
  });
  const filteredBookings = displayedBookings.filter((booking) => {
    const pitch = pitchMap.get(booking.pitchId);
    const organizer = userMap.get(booking.organizerId);
    return matchesSearch([pitch?.name, organizer?.name, booking.status, booking.startAt], normalizedSearch);
  });
  const filteredOpenGames = openGames.filter((game) => {
    const organizer = userMap.get(game.organizerId);
    const participantNames = game.participantIds.map((id) => userMap.get(id)?.name).join(' ');
    return matchesSearch([organizer?.name, participantNames, game.status], normalizedSearch);
  });
  const filteredUsers = users.filter((user) => {
    return matchesSearch([user.name, user.role, String(user.rating)], normalizedSearch);
  });
  const filteredEquipment = equipmentOffers.filter((offer) => {
    const pitch = pitchMap.get(offer.pitchId);
    return matchesSearch([offer.itemType, pitch?.name, pitch?.district], normalizedSearch);
  });

  async function loadAll() {
    setLoading(true);
    try {
      const [loadedPitches, loadedBookings, loadedOpenGames, loadedUsers, loadedEquipment] = await Promise.all([
        api.listPitches(),
        api.listBookings(),
        api.listOpenGames(),
        api.listUsers(),
        api.listEquipmentOffers(),
      ]);
      setPitches(loadedPitches);
      setBookings(loadedBookings);
      setOpenGames(loadedOpenGames);
      setUsers(loadedUsers);
      setEquipmentOffers(loadedEquipment);
      setPitchFilterResult(null);
      setBookingSearchResult(null);
      setPitchDistrict('');
    } catch (error) {
      showError(error);
    } finally {
      setLoading(false);
    }
  }

  async function applyPitchFilter() {
    setLoading(true);
    try {
      setPitchFilterResult(await api.listPitches(pitchDistrict));
    } catch (error) {
      showError(error);
    } finally {
      setLoading(false);
    }
  }

  function clearPitchFilter() {
    setPitchDistrict('');
    setPitchFilterResult(null);
  }

  async function applyBookingFilters(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    try {
      setBookingSearchResult(await api.searchBookings(bookingFilters));
      setActiveView('bookings');
    } catch (error) {
      showError(error);
    } finally {
      setLoading(false);
    }
  }

  function clearBookingFilters() {
    setBookingFilters(emptyBookingFilters);
    setBookingSearchResult(null);
  }

  function showError(error: unknown) {
    setToast(error instanceof Error ? error.message : 'Неожиданная ошибка API');
  }

  function openCreate(kind: EntityKind) {
    setModal({
      kind,
      mode: 'create',
      form: createDefaultForm(kind, pitches, bookings, users),
    });
  }

  function openEdit(kind: EntityKind, item: Pitch | Booking | OpenGame | User | EquipmentOffer) {
    setModal({
      kind,
      mode: 'edit',
      id: item.id,
      form: createEditForm(kind, item),
    });
  }

  function setModalValue(name: string, value: FormValue) {
    if (!modal) {
      return;
    }
    setModal({
      ...modal,
      form: {
        ...modal.form,
        [name]: value,
      },
    });
  }

  async function submitModal(event: FormEvent) {
    event.preventDefault();
    if (!modal) {
      return;
    }

    try {
      const payload = createPayload(modal);
      const id = modal.id ?? 0;
      if (modal.kind === 'pitch') {
        await (modal.mode === 'create' ? api.createPitch(payload) : api.updatePitch(id, payload));
      }
      if (modal.kind === 'booking') {
        await (modal.mode === 'create' ? api.createBooking(payload) : api.updateBooking(id, payload));
      }
      if (modal.kind === 'openGame') {
        await (modal.mode === 'create' ? api.createOpenGame(payload) : api.updateOpenGame(id, payload));
      }
      if (modal.kind === 'user') {
        await (modal.mode === 'create' ? api.createUser(payload) : api.updateUser(id, payload));
      }
      if (modal.kind === 'equipment') {
        await (modal.mode === 'create' ? api.createEquipmentOffer(payload) : api.updateEquipmentOffer(id, payload));
      }
      setToast('Изменения сохранены');
      setModal(null);
      await loadAll();
    } catch (error) {
      showError(error);
    }
  }

  async function deleteEntity(kind: EntityKind, id: number) {
    const confirmed = window.confirm('Удалить запись? Если у неё есть связанные данные, backend может отклонить удаление.');
    if (!confirmed) {
      return;
    }

    try {
      if (kind === 'pitch') {
        await api.deletePitch(id);
      }
      if (kind === 'booking') {
        await api.deleteBooking(id);
      }
      if (kind === 'openGame') {
        await api.deleteOpenGame(id);
      }
      if (kind === 'user') {
        await api.deleteUser(id);
      }
      if (kind === 'equipment') {
        await api.deleteEquipmentOffer(id);
      }
      setToast('Запись удалена');
      await loadAll();
    } catch (error) {
      showError(error);
    }
  }

  return (
    <div className="app-shell">
      <Sidebar
        activeView={activeView}
        counts={{
          dashboard: pitches.length + bookings.length + openGames.length,
          pitches: pitches.length,
          bookings: bookings.length,
          openGames: openGames.length,
          users: users.length,
          equipment: equipmentOffers.length,
        }}
        onNavigate={setActiveView}
      />

      <main className="workspace">
        <Hero search={catalogSearch} onSearch={setCatalogSearch} onRefresh={loadAll} />

        {loading && <div className="loading-banner">Загружаем данные из Spring Boot API...</div>}

        {activeView === 'dashboard' && (
          <Dashboard
            pitches={searchedPitches}
            bookings={bookings}
            users={users}
            openGames={openGames}
            equipmentOffers={equipmentOffers}
            pitchMap={pitchMap}
            userMap={userMap}
            onOpenPitches={() => setActiveView('pitches')}
            onCreatePitch={() => openCreate('pitch')}
            onEditPitch={(pitch) => openEdit('pitch', pitch)}
            onDeletePitch={(id) => void deleteEntity('pitch', id)}
          />
        )}

        {activeView === 'pitches' && (
          <section className="section-stack">
            <SectionHeader title="Площадки" subtitle="CRUD, фильтр по району и OneToMany связи" onAdd={() => openCreate('pitch')} />
            <div className="filter-strip">
              <label>
                Район
                <input value={pitchDistrict} onChange={(event) => setPitchDistrict(event.target.value)} placeholder="Например: Центральный" />
              </label>
              <button type="button" onClick={applyPitchFilter}>Применить</button>
              <button type="button" className="ghost-button" onClick={clearPitchFilter}>Сбросить</button>
            </div>
            <CardGrid>
              {filteredPitches.map((pitch) => (
                <PitchCard
                  key={pitch.id}
                  pitch={pitch}
                  bookings={bookings.filter((booking) => booking.pitchId === pitch.id)}
                  equipment={equipmentOffers.filter((offer) => offer.pitchId === pitch.id)}
                  onEdit={() => openEdit('pitch', pitch)}
                  onDelete={() => void deleteEntity('pitch', pitch.id)}
                />
              ))}
            </CardGrid>
          </section>
        )}

        {activeView === 'bookings' && (
          <section className="section-stack">
            <SectionHeader title="Бронирования" subtitle="CRUD и фильтрация через JPQL search endpoint" onAdd={() => openCreate('booking')} />
            <BookingFilters
              filters={bookingFilters}
              onChange={setBookingFilters}
              onSubmit={applyBookingFilters}
              onReset={clearBookingFilters}
            />
            {bookingSearchResult && (
              <div className="search-result-note">
                Найдено: {bookingSearchResult.totalElements}. Страница {bookingSearchResult.pageNumber + 1} из {Math.max(bookingSearchResult.totalPages, 1)}.
              </div>
            )}
            <CardGrid>
              {filteredBookings.map((booking) => (
                <BookingCard
                  key={booking.id}
                  booking={booking}
                  pitch={pitchMap.get(booking.pitchId)}
                  organizer={userMap.get(booking.organizerId)}
                  openGame={openGames.find((game) => game.bookingId === booking.id)}
                  onEdit={() => openEdit('booking', booking)}
                  onDelete={() => void deleteEntity('booking', booking.id)}
                />
              ))}
            </CardGrid>
          </section>
        )}

        {activeView === 'openGames' && (
          <section className="section-stack">
            <SectionHeader title="Открытые игры" subtitle="ManyToMany участники + CRUD" onAdd={() => openCreate('openGame')} />
            <CardGrid>
              {filteredOpenGames.map((game) => (
                <OpenGameCard
                  key={game.id}
                  game={game}
                  booking={bookingMap.get(game.bookingId)}
                  pitch={pitchMap.get(bookingMap.get(game.bookingId)?.pitchId ?? -1)}
                  organizer={userMap.get(game.organizerId)}
                  participants={game.participantIds.map((id) => userMap.get(id)).filter(Boolean) as User[]}
                  onEdit={() => openEdit('openGame', game)}
                  onDelete={() => void deleteEntity('openGame', game.id)}
                />
              ))}
            </CardGrid>
          </section>
        )}

        {activeView === 'users' && (
          <section className="section-stack">
            <SectionHeader title="Пользователи" subtitle="CRUD и связи пользователя с играми" onAdd={() => openCreate('user')} />
            <CardGrid>
              {filteredUsers.map((user) => (
                <UserCard
                  key={user.id}
                  user={user}
                  bookings={bookings.filter((booking) => booking.organizerId === user.id)}
                  openGames={openGames.filter((game) => game.organizerId === user.id || game.participantIds.includes(user.id))}
                  onEdit={() => openEdit('user', user)}
                  onDelete={() => void deleteEntity('user', user.id)}
                />
              ))}
            </CardGrid>
          </section>
        )}

        {activeView === 'equipment' && (
          <section className="section-stack">
            <SectionHeader title="Инвентарь" subtitle="CRUD и связь ManyToOne с площадками" onAdd={() => openCreate('equipment')} />
            <CardGrid>
              {filteredEquipment.map((offer) => (
                <EquipmentCard
                  key={offer.id}
                  offer={offer}
                  pitch={pitchMap.get(offer.pitchId)}
                  onEdit={() => openEdit('equipment', offer)}
                  onDelete={() => void deleteEntity('equipment', offer.id)}
                />
              ))}
            </CardGrid>
          </section>
        )}
      </main>

      {modal && (
        <EntityModal
          modal={modal}
          pitches={pitches}
          bookings={bookings}
          users={users}
          onChange={setModalValue}
          onClose={() => setModal(null)}
          onSubmit={submitModal}
        />
      )}

      {toast && (
        <div className="toast">
          <span>{toast}</span>
          <button type="button" onClick={() => setToast('')} aria-label="Закрыть уведомление">
            <X size={16} />
          </button>
        </div>
      )}
    </div>
  );
}

function Sidebar({
  activeView,
  counts,
  onNavigate,
}: {
  activeView: ViewKey;
  counts: Record<ViewKey, number>;
  onNavigate: (view: ViewKey) => void;
}) {
  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-mark">F</div>
        <div>
          <span>Коллекция</span>
          <strong>Pitch Market</strong>
        </div>
      </div>
      <nav className="nav-list">
        {navItems.map((item) => (
          <button
            key={item.key}
            type="button"
            className={activeView === item.key ? 'nav-item active' : 'nav-item'}
            onClick={() => onNavigate(item.key)}
          >
            <span>{item.label}</span>
            <b>{counts[item.key]}</b>
          </button>
        ))}
      </nav>
      <div className="sidebar-footer">
        <span>SPA Client</span>
        <strong>React + API</strong>
      </div>
    </aside>
  );
}

function Hero({
  search,
  onSearch,
  onRefresh,
}: {
  search: string;
  onSearch: (value: string) => void;
  onRefresh: () => void;
}) {
  return (
    <section className="hero-card">
      <div className="eyebrow">Лабораторная 7 • Client</div>
      <div className="hero-content">
        <div>
          <h1>Football Pitch Marketplace</h1>
          <p>SPA-клиент для площадок, бронирований, открытых игр, пользователей и инвентаря.</p>
        </div>
        <button type="button" className="refresh-button" onClick={onRefresh}>
          <RefreshCw size={18} />
          Обновить API
        </button>
      </div>
      <label className="hero-search">
        <Search size={18} />
        <input value={search} onChange={(event) => onSearch(event.target.value)} placeholder="Поиск по названию, району, статусу или игроку" />
      </label>
    </section>
  );
}

function Dashboard({
  pitches,
  bookings,
  users,
  openGames,
  equipmentOffers,
  pitchMap,
  userMap,
  onOpenPitches,
  onCreatePitch,
  onEditPitch,
  onDeletePitch,
}: {
  pitches: Pitch[];
  bookings: Booking[];
  users: User[];
  openGames: OpenGame[];
  equipmentOffers: EquipmentOffer[];
  pitchMap: Map<number, Pitch>;
  userMap: Map<number, User>;
  onOpenPitches: () => void;
  onCreatePitch: () => void;
  onEditPitch: (pitch: Pitch) => void;
  onDeletePitch: (id: number) => void;
}) {
  return (
    <section className="section-stack">
      <div className="stats-grid">
        <StatCard icon={<MapPin />} value={pitches.length} label="Площадки" />
        <StatCard icon={<CalendarClock />} value={bookings.length} label="Бронирования" />
        <StatCard icon={<Trophy />} value={openGames.length} label="Открытые игры" />
        <StatCard icon={<Users />} value={users.length} label="Пользователи" />
        <StatCard icon={<Dumbbell />} value={equipmentOffers.length} label="Инвентарь" />
      </div>

      <div className="relationship-panel">
        <div>
          <span className="eyebrow">Связи в проекте</span>
          <h2>OneToMany и ManyToMany показаны прямо в карточках</h2>
        </div>
        <div className="relation-grid">
          <RelationPill title="Pitch → Bookings" value={bookings.length} />
          <RelationPill title="Pitch → Equipment" value={equipmentOffers.length} />
          <RelationPill title="OpenGame ↔ Users" value={openGames.reduce((sum, game) => sum + game.participantIds.length, 0)} />
        </div>
      </div>

      <div className="section-title-row">
        <div>
          <h2>Популярные площадки</h2>
          <p>Карточки как в музыкальном каталоге, но с реальными данными backend API.</p>
        </div>
        <div className="action-row">
          <button type="button" className="ghost-button" onClick={onOpenPitches}>Открыть все</button>
          <button type="button" onClick={onCreatePitch}>
            <Plus size={16} />
            Добавить
          </button>
        </div>
      </div>

      <CardGrid>
        {pitches.slice(0, 8).map((pitch) => (
          <PitchCard
            key={pitch.id}
            pitch={pitch}
            bookings={bookings.filter((booking) => booking.pitchId === pitch.id)}
            equipment={equipmentOffers.filter((offer) => offer.pitchId === pitch.id)}
            onEdit={() => onEditPitch(pitch)}
            onDelete={() => onDeletePitch(pitch.id)}
          />
        ))}
      </CardGrid>

      <div className="compact-board">
        {bookings.slice(0, 4).map((booking) => (
          <MiniBooking key={booking.id} booking={booking} pitch={pitchMap.get(booking.pitchId)} user={userMap.get(booking.organizerId)} />
        ))}
      </div>
    </section>
  );
}

function SectionHeader({
  title,
  subtitle,
  onAdd,
}: {
  title: string;
  subtitle: string;
  onAdd: () => void;
}) {
  return (
    <div className="section-title-row">
      <div>
        <h2>{title}</h2>
        <p>{subtitle}</p>
      </div>
      <button type="button" onClick={onAdd}>
        <Plus size={16} />
        Создать
      </button>
    </div>
  );
}

function BookingFilters({
  filters,
  onChange,
  onSubmit,
  onReset,
}: {
  filters: BookingSearchFilters;
  onChange: (filters: BookingSearchFilters) => void;
  onSubmit: (event: FormEvent) => void;
  onReset: () => void;
}) {
  return (
    <form className="filter-strip wide" onSubmit={onSubmit}>
      <label>
        Район
        <input value={filters.district ?? ''} onChange={(event) => onChange({ ...filters, district: event.target.value })} />
      </label>
      <label>
        Тип
        <select value={filters.pitchType ?? ''} onChange={(event) => onChange({ ...filters, pitchType: event.target.value as BookingSearchFilters['pitchType'] })}>
          <option value="">Все</option>
          {pitchTypes.map((type) => (
            <option key={type} value={type}>{pitchTypeLabels[type]}</option>
          ))}
        </select>
      </label>
      <label>
        Организатор
        <input value={filters.organizerName ?? ''} onChange={(event) => onChange({ ...filters, organizerName: event.target.value })} />
      </label>
      <label>
        Статус
        <select value={filters.status ?? ''} onChange={(event) => onChange({ ...filters, status: event.target.value as BookingSearchFilters['status'] })}>
          <option value="">Все</option>
          {bookingStatuses.map((status) => (
            <option key={status} value={status}>{statusLabels[status]}</option>
          ))}
        </select>
      </label>
      <button type="submit">Фильтровать</button>
      <button type="button" className="ghost-button" onClick={onReset}>Сбросить</button>
    </form>
  );
}

function PitchCard({
  pitch,
  bookings,
  equipment,
  onEdit,
  onDelete,
}: {
  pitch: Pitch;
  bookings: Booking[];
  equipment: EquipmentOffer[];
  onEdit: () => void;
  onDelete: () => void;
}) {
  return (
    <article className="catalog-card">
      <GradientCover title={pitch.name} seed={pitch.id} />
      <div className="muted">{pitchTypeLabels[pitch.type]}</div>
      <h3>{pitch.name}</h3>
      <p>{pitch.district} • метро {pitch.metro}</p>
      <div className="price-line">
        <CircleDollarSign size={16} />
        {formatMoney(pitch.pricePerHour)} BYN/час
      </div>
      <RelationLine label="Брони" value={bookings.length ? bookings.map((booking) => `#${booking.id} ${booking.status}`).join(', ') : 'нет'} />
      <RelationLine label="Инвентарь" value={equipment.length ? equipment.map((offer) => equipmentLabels[offer.itemType]).join(', ') : 'нет'} />
      <CardActions onEdit={onEdit} onDelete={onDelete} />
    </article>
  );
}

function BookingCard({
  booking,
  pitch,
  organizer,
  openGame,
  onEdit,
  onDelete,
}: {
  booking: Booking;
  pitch?: Pitch;
  organizer?: User;
  openGame?: OpenGame;
  onEdit: () => void;
  onDelete: () => void;
}) {
  return (
    <article className="catalog-card text-card">
      <StatusBadge status={booking.status} />
      <h3>Бронь #{booking.id}</h3>
      <p>{pitch?.name ?? `Площадка #${booking.pitchId}`}</p>
      <RelationLine label="Организатор" value={organizer?.name ?? `User #${booking.organizerId}`} />
      <RelationLine label="Время" value={`${formatDate(booking.startAt)} → ${formatDate(booking.endAt)}`} />
      <RelationLine label="OpenGame" value={openGame ? `Игра #${openGame.id}, ${openGame.participantIds.length} участников` : 'не создана'} />
      <CardActions onEdit={onEdit} onDelete={onDelete} />
    </article>
  );
}

function OpenGameCard({
  game,
  booking,
  pitch,
  organizer,
  participants,
  onEdit,
  onDelete,
}: {
  game: OpenGame;
  booking?: Booking;
  pitch?: Pitch;
  organizer?: User;
  participants: User[];
  onEdit: () => void;
  onDelete: () => void;
}) {
  return (
    <article className="catalog-card text-card">
      <StatusBadge status={game.status} />
      <h3>Открытая игра #{game.id}</h3>
      <p>{pitch?.name ?? `Бронь #${game.bookingId}`} • {booking ? formatDate(booking.startAt) : 'время уточняется'}</p>
      <RelationLine label="Организатор" value={organizer?.name ?? `User #${game.organizerId}`} />
      <RelationLine label="Навык" value={`${game.targetSkillMin}–${game.targetSkillMax}`} />
      <RelationLine label="Лимит" value={`${participants.length}/${game.maxPlayers} игроков`} />
      <div className="chip-row">
        {participants.length ? participants.map((user) => <span key={user.id}>{user.name}</span>) : <span>Участников пока нет</span>}
      </div>
      <CardActions onEdit={onEdit} onDelete={onDelete} />
    </article>
  );
}

function UserCard({
  user,
  bookings,
  openGames,
  onEdit,
  onDelete,
}: {
  user: User;
  bookings: Booking[];
  openGames: OpenGame[];
  onEdit: () => void;
  onDelete: () => void;
}) {
  return (
    <article className="catalog-card text-card">
      <GradientCover title={user.name} seed={user.id + 30} compact />
      <h3>{user.name}</h3>
      <p>{roleLabels[user.role]} • рейтинг {user.rating}</p>
      <RelationLine label="Организует" value={`${bookings.length} бронирований`} />
      <RelationLine label="Игры" value={`${openGames.length} связанных open games`} />
      <CardActions onEdit={onEdit} onDelete={onDelete} />
    </article>
  );
}

function EquipmentCard({
  offer,
  pitch,
  onEdit,
  onDelete,
}: {
  offer: EquipmentOffer;
  pitch?: Pitch;
  onEdit: () => void;
  onDelete: () => void;
}) {
  return (
    <article className="catalog-card text-card">
      <Dumbbell className="entity-icon" />
      <h3>{equipmentLabels[offer.itemType]}</h3>
      <p>{pitch?.name ?? `Площадка #${offer.pitchId}`}</p>
      <RelationLine label="Остаток" value={`${offer.stockTotal} шт.`} />
      <RelationLine label="Цена" value={`${formatMoney(offer.rentFixedPrice)} BYN`} />
      <CardActions onEdit={onEdit} onDelete={onDelete} />
    </article>
  );
}

function EntityModal({
  modal,
  pitches,
  bookings,
  users,
  onChange,
  onClose,
  onSubmit,
}: {
  modal: ModalState;
  pitches: Pitch[];
  bookings: Booking[];
  users: User[];
  onChange: (name: string, value: FormValue) => void;
  onClose: () => void;
  onSubmit: (event: FormEvent) => void;
}) {
  return (
    <div className="modal-backdrop">
      <form className="modal-card" onSubmit={onSubmit}>
        <div className="modal-head">
          <div>
            <span className="eyebrow">{modal.mode === 'create' ? 'Создание' : 'Редактирование'}</span>
            <h2>{modalTitle(modal.kind)}</h2>
          </div>
          <button type="button" className="icon-button" onClick={onClose} aria-label="Закрыть">
            <X size={18} />
          </button>
        </div>

        <div className="form-grid">
          {modal.kind === 'pitch' && (
            <>
              <TextField label="Название" name="name" value={fieldValue(modal.form.name)} onChange={onChange} />
              <SelectField label="Тип" name="type" value={fieldValue(modal.form.type)} options={pitchTypes.map((type) => [type, pitchTypeLabels[type]])} onChange={onChange} />
              <TextField label="Район" name="district" value={fieldValue(modal.form.district)} onChange={onChange} />
              <TextField label="Метро" name="metro" value={fieldValue(modal.form.metro)} onChange={onChange} />
              <TextField label="Цена за час" name="pricePerHour" type="number" step="0.01" value={fieldValue(modal.form.pricePerHour)} onChange={onChange} />
            </>
          )}

          {modal.kind === 'booking' && (
            <>
              <SelectField label="Площадка" name="pitchId" value={fieldValue(modal.form.pitchId)} options={pitches.map((pitch) => [String(pitch.id), pitch.name])} onChange={onChange} />
              <SelectField label="Организатор" name="organizerId" value={fieldValue(modal.form.organizerId)} options={users.map((user) => [String(user.id), user.name])} onChange={onChange} />
              <TextField label="Начало" name="startAt" type="datetime-local" value={fieldValue(modal.form.startAt)} onChange={onChange} />
              <TextField label="Конец" name="endAt" type="datetime-local" value={fieldValue(modal.form.endAt)} onChange={onChange} />
              <SelectField label="Статус" name="status" value={fieldValue(modal.form.status)} options={bookingStatuses.map((status) => [status, statusLabels[status]])} onChange={onChange} />
            </>
          )}

          {modal.kind === 'openGame' && (
            <>
              <SelectField label="Бронь" name="bookingId" value={fieldValue(modal.form.bookingId)} options={bookings.map((booking) => [String(booking.id), `#${booking.id} ${booking.startAt}`])} onChange={onChange} />
              <SelectField label="Организатор" name="organizerId" value={fieldValue(modal.form.organizerId)} options={users.map((user) => [String(user.id), user.name])} onChange={onChange} />
              <TextField label="Навык от" name="targetSkillMin" type="number" min="0" max="100" value={fieldValue(modal.form.targetSkillMin)} onChange={onChange} />
              <TextField label="Навык до" name="targetSkillMax" type="number" min="0" max="100" value={fieldValue(modal.form.targetSkillMax)} onChange={onChange} />
              <TextField label="Максимум игроков" name="maxPlayers" type="number" min="2" max="50" value={fieldValue(modal.form.maxPlayers)} onChange={onChange} />
              <SelectField label="Статус" name="status" value={fieldValue(modal.form.status)} options={openGameStatuses.map((status) => [status, statusLabels[status]])} onChange={onChange} />
              <label className="field full-width">
                Участники
                <select
                  multiple
                  value={arrayValue(modal.form.participantIds)}
                  onChange={(event) => onChange('participantIds', Array.from(event.target.selectedOptions).map((option) => option.value))}
                >
                  {users.map((user) => (
                    <option key={user.id} value={user.id}>{user.name}</option>
                  ))}
                </select>
              </label>
            </>
          )}

          {modal.kind === 'user' && (
            <>
              <TextField label="Имя" name="name" value={fieldValue(modal.form.name)} onChange={onChange} />
              <TextField label="Рейтинг" name="rating" type="number" min="0" max="100" value={fieldValue(modal.form.rating)} onChange={onChange} />
              <SelectField label="Роль" name="role" value={fieldValue(modal.form.role)} options={userRoles.map((role) => [role, roleLabels[role]])} onChange={onChange} />
            </>
          )}

          {modal.kind === 'equipment' && (
            <>
              <SelectField label="Площадка" name="pitchId" value={fieldValue(modal.form.pitchId)} options={pitches.map((pitch) => [String(pitch.id), pitch.name])} onChange={onChange} />
              <SelectField label="Тип" name="itemType" value={fieldValue(modal.form.itemType)} options={equipmentTypes.map((type) => [type, equipmentLabels[type]])} onChange={onChange} />
              <TextField label="Количество" name="stockTotal" type="number" min="0" value={fieldValue(modal.form.stockTotal)} onChange={onChange} />
              <TextField label="Цена аренды" name="rentFixedPrice" type="number" step="0.01" value={fieldValue(modal.form.rentFixedPrice)} onChange={onChange} />
            </>
          )}
        </div>

        <div className="modal-actions">
          <button type="button" className="ghost-button" onClick={onClose}>Отмена</button>
          <button type="submit">Сохранить</button>
        </div>
      </form>
    </div>
  );
}

type TextFieldProps = {
  label: string;
  name: string;
  value: string;
  onChange: (name: string, value: string) => void;
  type?: string;
} & Omit<InputHTMLAttributes<HTMLInputElement>, 'name' | 'onChange' | 'type' | 'value'>;

function TextField({
  label,
  name,
  value,
  onChange,
  type = 'text',
  ...inputProps
}: TextFieldProps) {
  return (
    <label className="field">
      {label}
      <input {...inputProps} type={type} value={value} onChange={(event) => onChange(name, event.target.value)} required />
    </label>
  );
}

function SelectField({
  label,
  name,
  value,
  options,
  onChange,
}: {
  label: string;
  name: string;
  value: string;
  options: Array<readonly [string, string]>;
  onChange: (name: string, value: string) => void;
}) {
  return (
    <label className="field">
      {label}
      <select value={value} onChange={(event) => onChange(name, event.target.value)} required>
        {options.map(([optionValue, labelText]) => (
          <option key={optionValue} value={optionValue}>{labelText}</option>
        ))}
      </select>
    </label>
  );
}

function CardGrid({ children }: { children: ReactNode }) {
  return <div className="card-grid">{children}</div>;
}

function CardActions({ onEdit, onDelete }: { onEdit: () => void; onDelete: () => void }) {
  return (
    <div className="card-actions">
      <button type="button" className="icon-button" onClick={onEdit} aria-label="Редактировать">
        <Edit3 size={16} />
      </button>
      <button type="button" className="icon-button danger" onClick={onDelete} aria-label="Удалить">
        <Trash2 size={16} />
      </button>
    </div>
  );
}

function GradientCover({ title, seed, compact = false }: { title: string; seed: number; compact?: boolean }) {
  const initials = title
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase();
  return (
    <div className={compact ? `cover compact cover-${seed % 6}` : `cover cover-${seed % 6}`}>
      {initials || 'FM'}
    </div>
  );
}

function StatCard({ icon, value, label }: { icon: ReactNode; value: number; label: string }) {
  return (
    <article className="stat-card">
      {icon}
      <strong>{value}</strong>
      <span>{label}</span>
    </article>
  );
}

function RelationPill({ title, value }: { title: string; value: number }) {
  return (
    <div className="relation-pill">
      <Activity size={16} />
      <span>{title}</span>
      <b>{value}</b>
    </div>
  );
}

function RelationLine({ label, value }: { label: string; value: string }) {
  return (
    <div className="relation-line">
      <b>{label}</b>
      <span>{value}</span>
    </div>
  );
}

function MiniBooking({ booking, pitch, user }: { booking: Booking; pitch?: Pitch; user?: User }) {
  return (
    <div className="mini-booking">
      <CalendarClock size={18} />
      <div>
        <strong>{pitch?.name ?? `Площадка #${booking.pitchId}`}</strong>
        <span>{user?.name ?? `User #${booking.organizerId}`} • {formatDate(booking.startAt)}</span>
      </div>
      <StatusBadge status={booking.status} />
    </div>
  );
}

function StatusBadge({ status }: { status: string }) {
  return <span className={`status-badge status-${status.toLowerCase()}`}>{statusLabels[status as keyof typeof statusLabels] ?? status}</span>;
}

function matchesSearch(values: Array<string | undefined>, search: string) {
  if (!search) {
    return true;
  }
  return values.some((value) => value?.toLowerCase().includes(search));
}

function createDefaultForm(kind: EntityKind, pitches: Pitch[], bookings: Booking[], users: User[]): FormState {
  const tomorrow = new Date(Date.now() + 24 * 60 * 60 * 1000);
  tomorrow.setMinutes(0, 0, 0);
  const afterTomorrow = new Date(tomorrow.getTime() + 2 * 60 * 60 * 1000);
  const firstPitchId = String(pitches[0]?.id ?? '');
  const firstBookingId = String(bookings[0]?.id ?? '');
  const firstUserId = String(users[0]?.id ?? '');

  if (kind === 'pitch') {
    return { name: 'Новая арена', type: 'FIVE_TURF', district: 'Центральный', metro: 'Немига', pricePerHour: '120.00' };
  }
  if (kind === 'booking') {
    return {
      pitchId: firstPitchId,
      organizerId: firstUserId,
      startAt: toDateInputValue(tomorrow),
      endAt: toDateInputValue(afterTomorrow),
      status: 'CREATED',
    };
  }
  if (kind === 'openGame') {
    return {
      bookingId: firstBookingId,
      organizerId: firstUserId,
      targetSkillMin: '30',
      targetSkillMax: '80',
      maxPlayers: '10',
      status: 'OPEN',
      participantIds: [],
    };
  }
  if (kind === 'user') {
    return { name: 'Новый игрок', rating: '70', role: 'PLAYER' };
  }
  return { pitchId: firstPitchId, itemType: 'BALL', stockTotal: '8', rentFixedPrice: '10.00' };
}

function createEditForm(kind: EntityKind, item: Pitch | Booking | OpenGame | User | EquipmentOffer): FormState {
  if (kind === 'pitch') {
    const pitch = item as Pitch;
    return {
      name: pitch.name,
      type: pitch.type,
      district: pitch.district,
      metro: pitch.metro,
      pricePerHour: String(pitch.pricePerHour),
    };
  }
  if (kind === 'booking') {
    const booking = item as Booking;
    return {
      pitchId: String(booking.pitchId),
      organizerId: String(booking.organizerId),
      startAt: booking.startAt.slice(0, 16),
      endAt: booking.endAt.slice(0, 16),
      status: booking.status,
    };
  }
  if (kind === 'openGame') {
    const game = item as OpenGame;
    return {
      bookingId: String(game.bookingId),
      organizerId: String(game.organizerId),
      targetSkillMin: String(game.targetSkillMin),
      targetSkillMax: String(game.targetSkillMax),
      maxPlayers: String(game.maxPlayers),
      status: game.status,
      participantIds: game.participantIds.map(String),
    };
  }
  if (kind === 'user') {
    const user = item as User;
    return { name: user.name, rating: String(user.rating), role: user.role };
  }
  const offer = item as EquipmentOffer;
  return {
    pitchId: String(offer.pitchId),
    itemType: offer.itemType,
    stockTotal: String(offer.stockTotal),
    rentFixedPrice: String(offer.rentFixedPrice),
  };
}

function createPayload(modal: ModalState): Record<string, unknown> {
  const form = modal.form;
  if (modal.kind === 'pitch') {
    return {
      name: fieldValue(form.name),
      type: fieldValue(form.type),
      district: fieldValue(form.district),
      metro: fieldValue(form.metro),
      pricePerHour: Number(fieldValue(form.pricePerHour)),
    };
  }
  if (modal.kind === 'booking') {
    return {
      pitchId: Number(fieldValue(form.pitchId)),
      organizerId: Number(fieldValue(form.organizerId)),
      startAt: toApiDate(fieldValue(form.startAt)),
      endAt: toApiDate(fieldValue(form.endAt)),
      status: fieldValue(form.status),
    };
  }
  if (modal.kind === 'openGame') {
    return {
      bookingId: Number(fieldValue(form.bookingId)),
      organizerId: Number(fieldValue(form.organizerId)),
      targetSkillMin: Number(fieldValue(form.targetSkillMin)),
      targetSkillMax: Number(fieldValue(form.targetSkillMax)),
      maxPlayers: Number(fieldValue(form.maxPlayers)),
      status: fieldValue(form.status),
      participantIds: arrayValue(form.participantIds).map(Number),
    };
  }
  if (modal.kind === 'user') {
    return {
      name: fieldValue(form.name),
      rating: Number(fieldValue(form.rating)),
      role: fieldValue(form.role),
    };
  }
  return {
    pitchId: Number(fieldValue(form.pitchId)),
    itemType: fieldValue(form.itemType),
    stockTotal: Number(fieldValue(form.stockTotal)),
    rentFixedPrice: Number(fieldValue(form.rentFixedPrice)),
  };
}

function modalTitle(kind: EntityKind) {
  if (kind === 'pitch') {
    return 'Площадка';
  }
  if (kind === 'booking') {
    return 'Бронирование';
  }
  if (kind === 'openGame') {
    return 'Открытая игра';
  }
  if (kind === 'user') {
    return 'Пользователь';
  }
  return 'Инвентарь';
}

function fieldValue(value: FormValue | undefined) {
  return Array.isArray(value) ? value.join(',') : value ?? '';
}

function arrayValue(value: FormValue | undefined) {
  return Array.isArray(value) ? value : [];
}

function toDateInputValue(date: Date) {
  const pad = (value: number) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function toApiDate(value: string) {
  return value.length === 16 ? `${value}:00` : value;
}

function formatDate(value: string) {
  return value.replace('T', ' ').slice(0, 16);
}

function formatMoney(value: number) {
  return Number(value).toFixed(2);
}

export default App;
