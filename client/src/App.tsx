import {
  Activity,
  CalendarClock,
  CircleDollarSign,
  Dumbbell,
  Edit3,
  Globe2,
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
import { useEffect, useRef, useState } from 'react';
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
type Language = 'ru' | 'en';
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

const dictionaries = {
  ru: {
    pitchTypes: {
      FIVE_TURF: '5 на 5, газон',
      FIVE_FUTSAL: '5 на 5, футзал',
      EIGHT: '8 на 8',
      ELEVEN: '11 на 11',
    },
    statuses: {
      CREATED: 'Создано',
      CONFIRMED: 'Подтверждено',
      CANCELLED: 'Отменено',
      OPEN: 'Открыта',
      FULL: 'Набрана',
    },
    roles: {
      PLAYER: 'Игрок',
      VENUE_OWNER: 'Владелец',
      ADMIN: 'Админ',
    },
    equipment: {
      BALL: 'Мячи',
      BIBS: 'Манишки',
    },
    navItems: [
      { key: 'dashboard', label: 'Главная' },
      { key: 'pitches', label: 'Площадки' },
      { key: 'bookings', label: 'Брони' },
      { key: 'openGames', label: 'Игры' },
      { key: 'users', label: 'Игроки' },
      { key: 'equipment', label: 'Прокат' },
    ] as Array<{ key: ViewKey; label: string }>,
  },
  en: {
    pitchTypes: {
      FIVE_TURF: '5v5 turf',
      FIVE_FUTSAL: '5v5 futsal',
      EIGHT: '8v8',
      ELEVEN: '11v11',
    },
    statuses: {
      CREATED: 'Created',
      CONFIRMED: 'Confirmed',
      CANCELLED: 'Cancelled',
      OPEN: 'Open',
      FULL: 'Full',
    },
    roles: {
      PLAYER: 'Player',
      VENUE_OWNER: 'Owner',
      ADMIN: 'Admin',
    },
    equipment: {
      BALL: 'Balls',
      BIBS: 'Bibs',
    },
    navItems: [
      { key: 'dashboard', label: 'Home' },
      { key: 'pitches', label: 'Pitches' },
      { key: 'bookings', label: 'Bookings' },
      { key: 'openGames', label: 'Games' },
      { key: 'users', label: 'Players' },
      { key: 'equipment', label: 'Gear' },
    ] as Array<{ key: ViewKey; label: string }>,
  },
};

const ui = {
  ru: {
    brandCity: 'Минск',
    brandName: 'Аренда полей',
    loading: 'Обновляем данные площадок...',
    saveSuccess: 'Изменения сохранены',
    deleteSuccess: 'Запись удалена',
    genericError: 'Не удалось выполнить действие',
    deleteConfirm: 'Удалить запись? Если она используется в расписании, удалить её не получится.',
    heroTitle: 'Тут бронируют футбольные площадки',
    heroSubtitle: 'Выберите поле, соберите команду и добавьте прокат за пару минут.',
    refresh: 'Обновить',
    searchPlaceholder: 'Площадка, район, игрок или статус',
    district: 'Район',
    districtPlaceholder: 'Например: Центральный',
    show: 'Показать',
    reset: 'Сбросить',
    filter: 'Фильтровать',
    all: 'Все',
    type: 'Тип',
    organizer: 'Организатор',
    status: 'Статус',
    found: 'Найдено записей',
    page: 'Страница',
    of: 'из',
    statsPitches: 'Площадки',
    statsBookings: 'Бронирования',
    statsGames: 'Открытые игры',
    statsUsers: 'Пользователи',
    statsEquipment: 'Инвентарь',
    allTogether: 'Всё под рукой',
    relationTitle: 'Расписание, прокат и участники собраны в одном месте',
    relationText: 'Откройте карточку площадки или игры, чтобы увидеть детали без лишних переходов.',
    bookings: 'Брони',
    gear: 'Прокат',
    participants: 'Участники',
    popularPitches: 'Популярные площадки',
    popularSubtitle: 'Быстрый выбор поля по формату, району и доступным услугам.',
    openAll: 'Открыть все',
    addPitch: 'Добавить площадку',
    create: 'Создать',
    sectionPitches: 'Площадки',
    sectionPitchesSub: 'Выбирайте поле по району, формату игры и цене',
    sectionBookings: 'Брони',
    sectionBookingsSub: 'Проверяйте расписание и быстро находите нужную запись',
    sectionGames: 'Игры',
    sectionGamesSub: 'Открытые матчи, набор игроков и уровень команды',
    sectionUsers: 'Игроки',
    sectionUsersSub: 'Профили, рейтинг и участие в матчах',
    sectionEquipment: 'Прокат',
    sectionEquipmentSub: 'Мячи и манишки, которые доступны на выбранных площадках',
    metro: 'метро',
    priceHour: 'BYN/час',
    noBookings: 'пока нет',
    noGear: 'нет',
    inventory: 'Инвентарь',
    booking: 'Бронь',
    pitch: 'Площадка',
    player: 'Игрок',
    time: 'Время',
    match: 'Матч',
    game: 'Игра',
    notCreated: 'не создан',
    skill: 'Навык',
    limit: 'Лимит',
    players: 'игроков',
    noParticipants: 'Участников пока нет',
    rating: 'рейтинг',
    organizes: 'Организует',
    games: 'Игры',
    items: 'шт.',
    stock: 'Остаток',
    price: 'Цена',
    createMode: 'Создание',
    editMode: 'Редактирование',
    close: 'Закрыть',
    name: 'Название',
    start: 'Начало',
    end: 'Конец',
    skillFrom: 'Навык от',
    skillTo: 'Навык до',
    maxPlayers: 'Максимум игроков',
    role: 'Роль',
    quantity: 'Количество',
    rentPrice: 'Цена аренды',
    cancel: 'Отмена',
    save: 'Сохранить',
    edit: 'Редактировать',
    delete: 'Удалить',
  },
  en: {
    brandCity: 'Minsk',
    brandName: 'Pitch Booking',
    loading: 'Refreshing pitch data...',
    saveSuccess: 'Changes saved',
    deleteSuccess: 'Record deleted',
    genericError: 'Could not complete the action',
    deleteConfirm: 'Delete this record? If it is used in the schedule, deletion may be blocked.',
    heroTitle: 'Book football pitches in minutes',
    heroSubtitle: 'Choose a venue, gather a team, and add gear in one place.',
    refresh: 'Refresh',
    searchPlaceholder: 'Pitch, district, player, or status',
    district: 'District',
    districtPlaceholder: 'Example: Central',
    show: 'Show',
    reset: 'Reset',
    filter: 'Filter',
    all: 'All',
    type: 'Type',
    organizer: 'Organizer',
    status: 'Status',
    found: 'Found',
    page: 'Page',
    of: 'of',
    statsPitches: 'Pitches',
    statsBookings: 'Bookings',
    statsGames: 'Open games',
    statsUsers: 'Players',
    statsEquipment: 'Gear',
    allTogether: 'All in one place',
    relationTitle: 'Schedule, rental gear, and players stay connected',
    relationText: 'Open a pitch or game card to see every important detail without extra screens.',
    bookings: 'Bookings',
    gear: 'Gear',
    participants: 'Players',
    popularPitches: 'Popular pitches',
    popularSubtitle: 'Quickly compare format, district, and available services.',
    openAll: 'View all',
    addPitch: 'Add pitch',
    create: 'Create',
    sectionPitches: 'Pitches',
    sectionPitchesSub: 'Choose by district, game format, and hourly price',
    sectionBookings: 'Bookings',
    sectionBookingsSub: 'Check the schedule and find any booking quickly',
    sectionGames: 'Games',
    sectionGamesSub: 'Open matches, player recruitment, and team level',
    sectionUsers: 'Players',
    sectionUsersSub: 'Profiles, ratings, and match participation',
    sectionEquipment: 'Gear',
    sectionEquipmentSub: 'Balls and bibs available at selected pitches',
    metro: 'metro',
    priceHour: 'BYN/hour',
    noBookings: 'none yet',
    noGear: 'none',
    inventory: 'Gear',
    booking: 'Booking',
    pitch: 'Pitch',
    player: 'Player',
    time: 'Time',
    match: 'Match',
    game: 'Game',
    notCreated: 'not created',
    skill: 'Skill',
    limit: 'Limit',
    players: 'players',
    noParticipants: 'No players yet',
    rating: 'rating',
    organizes: 'Organizes',
    games: 'Games',
    items: 'pcs.',
    stock: 'Stock',
    price: 'Price',
    createMode: 'Create',
    editMode: 'Edit',
    close: 'Close',
    name: 'Name',
    start: 'Start',
    end: 'End',
    skillFrom: 'Skill from',
    skillTo: 'Skill to',
    maxPlayers: 'Max players',
    role: 'Role',
    quantity: 'Quantity',
    rentPrice: 'Rental price',
    cancel: 'Cancel',
    save: 'Save',
    edit: 'Edit',
    delete: 'Delete',
  },
};

type Dictionary = (typeof dictionaries)[Language];
type UiText = (typeof ui)[Language];

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

const cleanPitchNames = [
  { name: 'Дворец футбола', district: 'Центральный', metro: 'Немига' },
  { name: 'Минск-Арена Футбол', district: 'Центральный', metro: 'Спортивная' },
  { name: 'Сокол Арена', district: 'Октябрьский', metro: 'Ковальская Слобода' },
  { name: 'Уручье Парк', district: 'Первомайский', metro: 'Уручье' },
  { name: 'Чижовка Футбол', district: 'Заводской', metro: 'Автозаводская' },
  { name: 'Веснянка Спорт', district: 'Центральный', metro: 'Молодежная' },
  { name: 'Лошица Арена', district: 'Ленинский', metro: 'Пролетарская' },
  { name: 'Комаровка Футзал', district: 'Советский', metro: 'Площадь Якуба Коласа' },
  { name: 'Малиновка Спорт', district: 'Московский', metro: 'Малиновка' },
  { name: 'Каменная Горка Арена', district: 'Фрунзенский', metro: 'Каменная Горка' },
];

const cleanUserNames = ['Алексей', 'Максим', 'Илья', 'Денис', 'Егор', 'Артем', 'Павел', 'Никита'];

function App() {
  const [activeView, setActiveView] = useState<ViewKey>('dashboard');
  const [pitches, setPitches] = useState<Pitch[]>([]);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [openGames, setOpenGames] = useState<OpenGame[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [equipmentOffers, setEquipmentOffers] = useState<EquipmentOffer[]>([]);
  const [pitchFilterResult, setPitchFilterResult] = useState<Pitch[] | null>(null);
  const [pitchDistrict, setPitchDistrict] = useState('');
  const [dashboardSearchInput, setDashboardSearchInput] = useState('');
  const [dashboardSearch, setDashboardSearch] = useState('');
  const [dashboardSearching, setDashboardSearching] = useState(false);
  const [language, setLanguage] = useState<Language>('ru');
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState<ModalState | null>(null);
  const [toast, setToast] = useState('');
  const [pageByView, setPageByView] = useState<Record<ViewKey, number>>({
    dashboard: 0,
    pitches: 0,
    bookings: 0,
    openGames: 0,
    users: 0,
    equipment: 0,
  });
  const pageSize = 12;
  const dashboardResultsRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    void loadAll();
  }, []);

  const pitchMap = new Map(pitches.map((pitch) => [pitch.id, pitch]));
  const userMap = new Map(users.map((user) => [user.id, user]));
  const bookingMap = new Map(bookings.map((booking) => [booking.id, booking]));
  const normalizedDashboardSearch = dashboardSearch.trim().toLowerCase();
  const displayedPitches = pitchFilterResult ?? pitches;
  const dictionary = dictionaries[language];
  const t = ui[language];
  const districtOptions = Array.from(new Set(pitches.map((pitch) => pitch.district).filter(Boolean))).sort((a, b) =>
    a.localeCompare(b, 'ru'),
  );

  const searchedPitches = pitches.filter((pitch) => matchesSearch([pitch.name, pitch.district, pitch.metro, pitch.type], normalizedDashboardSearch));
  const searchedBookings = bookings.filter((booking) => {
    const pitch = pitchMap.get(booking.pitchId);
    const organizer = userMap.get(booking.organizerId);
    return matchesSearch([pitch?.name, organizer?.name, booking.status, booking.startAt], normalizedDashboardSearch);
  });
  const searchedOpenGames = openGames.filter((game) => {
    const organizer = userMap.get(game.organizerId);
    const participantNames = game.participantIds.map((id) => userMap.get(id)?.name).join(' ');
    return matchesSearch([organizer?.name, participantNames, game.status], normalizedDashboardSearch);
  });
  const searchedUsers = users.filter((user) => matchesSearch([user.name, user.role, String(user.rating)], normalizedDashboardSearch));
  const searchedEquipment = equipmentOffers.filter((offer) => {
    const pitch = pitchMap.get(offer.pitchId);
    return matchesSearch([offer.itemType, pitch?.name, pitch?.district], normalizedDashboardSearch);
  });

  const filteredPitches = displayedPitches;
  const filteredBookings = bookings;
  const filteredOpenGames = openGames;
  const filteredUsers = users;
  const filteredEquipment = equipmentOffers;

  const pagedPitches = paginate(filteredPitches, pageByView.pitches, pageSize);
  const pagedBookings = paginate(filteredBookings, pageByView.bookings, pageSize);
  const pagedOpenGames = paginate(filteredOpenGames, pageByView.openGames, pageSize);
  const pagedUsers = paginate(filteredUsers, pageByView.users, pageSize);
  const pagedEquipment = paginate(filteredEquipment, pageByView.equipment, pageSize);

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
      setPitches(loadedPitches.map(normalizePitchForDisplay));
      setBookings(loadedBookings);
      setOpenGames(loadedOpenGames);
      setUsers(loadedUsers.map(normalizeUserForDisplay));
      setEquipmentOffers(loadedEquipment);
      setPitchFilterResult(null);
      setPitchDistrict('');
      setPageByView({
        dashboard: 0,
        pitches: 0,
        bookings: 0,
        openGames: 0,
        users: 0,
        equipment: 0,
      });
    } catch (error) {
      showError(error);
    } finally {
      setLoading(false);
    }
  }

  function applyDashboardSearch() {
    setDashboardSearching(true);
    setDashboardSearch(dashboardSearchInput);
    requestAnimationFrame(() => {
      dashboardResultsRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
    window.setTimeout(() => setDashboardSearching(false), 450);
  }

  async function applyPitchFilter() {
    setLoading(true);
    try {
      const filtered = await api.listPitches(pitchDistrict);
      setPitchFilterResult(filtered.map(normalizePitchForDisplay));
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

  function showError(error: unknown) {
    setToast(error instanceof Error ? error.message : t.genericError);
  }

  function openCreate(kind: EntityKind, formOverrides: Partial<FormState> = {}) {
    const form = Object.entries(formOverrides).reduce<FormState>(
      (accumulator, [key, value]) => {
        if (value !== undefined) {
          accumulator[key] = value;
        }
        return accumulator;
      },
      { ...createDefaultForm(kind, pitches, bookings, users) },
    );
    setModal({
      kind,
      mode: 'create',
      form,
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
      setToast(t.saveSuccess);
      setModal(null);
      await loadAll();
    } catch (error) {
      showError(error);
    }
  }

  async function deleteEntity(kind: EntityKind, id: number) {
    const confirmed = window.confirm(t.deleteConfirm);
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
      setToast(t.deleteSuccess);
      await loadAll();
    } catch (error) {
      showError(error);
    }
  }

  return (
    <div className="app-shell">
      <TopBar
        activeView={activeView}
        language={language}
        t={t}
        dictionary={dictionary}
        counts={{
          dashboard: pitches.length + bookings.length + openGames.length,
          pitches: pitches.length,
          bookings: bookings.length,
          openGames: openGames.length,
          users: users.length,
          equipment: equipmentOffers.length,
        }}
        onNavigate={(view) => {
          setActiveView(view);
          setPageByView((current) => ({ ...current, [view]: 0 }));
        }}
        onLanguageChange={setLanguage}
        onRefresh={loadAll}
      />

      <div className="page-shell">
        {activeView === 'dashboard' && (
          <Hero
            search={dashboardSearchInput}
            searching={dashboardSearching}
            t={t}
            onSearch={setDashboardSearchInput}
            onSearchCommit={applyDashboardSearch}
          />
        )}

        <main className="workspace">
          {loading && <div className="loading-banner">{t.loading}</div>}

          {activeView === 'dashboard' && (
            <>
              <div ref={dashboardResultsRef} />
              <Dashboard
                t={t}
                dictionary={dictionary}
                pitches={searchedPitches}
                bookings={searchedBookings}
                users={searchedUsers}
                openGames={searchedOpenGames}
                equipmentOffers={searchedEquipment}
                pitchMap={pitchMap}
                userMap={userMap}
                onOpenPitches={() => setActiveView('pitches')}
                onCreatePitch={() => openCreate('pitch')}
                onEditPitch={(pitch) => openEdit('pitch', pitch)}
                onDeletePitch={(id) => void deleteEntity('pitch', id)}
              />
            </>
          )}

          {activeView === 'pitches' && (
            <section className="section-stack">
              <SectionHeader title={t.sectionPitches} subtitle={t.sectionPitchesSub} addLabel={t.create} onAdd={() => openCreate('pitch')} />
              <div className="filter-strip">
                <label>
                  {t.district}
                  <div className="pretty-select">
                    <select value={pitchDistrict} onChange={(event) => setPitchDistrict(event.target.value)} aria-label={t.district}>
                      <option value="">{t.all}</option>
                      {districtOptions.map((district) => (
                        <option key={district} value={district}>
                          {district}
                        </option>
                      ))}
                    </select>
                  </div>
                </label>
                <button type="button" onClick={applyPitchFilter}>{t.show}</button>
                <button type="button" className="ghost-button" onClick={clearPitchFilter}>{t.reset}</button>
              </div>
              <CardGrid>
                {pagedPitches.items.map((pitch) => (
                  <PitchCard
                    key={pitch.id}
                    t={t}
                    dictionary={dictionary}
                    pitch={pitch}
                    bookings={bookings.filter((booking) => booking.pitchId === pitch.id)}
                    equipment={equipmentOffers.filter((offer) => offer.pitchId === pitch.id)}
                    onEdit={() => openEdit('pitch', pitch)}
                    onDelete={() => void deleteEntity('pitch', pitch.id)}
                  />
                ))}
              </CardGrid>
              <PaginationBar
                t={t}
                page={pagedPitches.page}
                pageSize={pagedPitches.pageSize}
                total={pagedPitches.total}
                onPageChange={(page) => setPageByView((current) => ({ ...current, pitches: page }))}
              />
            </section>
          )}

          {activeView === 'bookings' && (
            <section className="section-stack">
              <SectionHeader title={t.sectionBookings} subtitle={t.sectionBookingsSub} addLabel={t.create} onAdd={() => openCreate('booking')} />
              <CardGrid>
                {pagedBookings.items.map((booking) => (
                  <BookingCard
                    key={booking.id}
                    t={t}
                    dictionary={dictionary}
                    booking={booking}
                    pitch={pitchMap.get(booking.pitchId)}
                    organizer={userMap.get(booking.organizerId)}
                    onEdit={() => openEdit('booking', booking)}
                    onDelete={() => void deleteEntity('booking', booking.id)}
                    onOpenPitch={() => {
                      setActiveView('pitches');
                      setPageByView((current) => ({ ...current, pitches: 0 }));
                    }}
                    onOpenUser={() => {
                      setActiveView('users');
                      setPageByView((current) => ({ ...current, users: 0 }));
                    }}
                  />
                ))}
              </CardGrid>
              <PaginationBar
                t={t}
                page={pagedBookings.page}
                pageSize={pagedBookings.pageSize}
                total={pagedBookings.total}
                onPageChange={(page) => setPageByView((current) => ({ ...current, bookings: page }))}
              />
            </section>
          )}

          {activeView === 'openGames' && (
            <section className="section-stack">
              <SectionHeader title={t.sectionGames} subtitle={t.sectionGamesSub} addLabel={t.create} onAdd={() => openCreate('openGame')} />
              <CardGrid>
                {pagedOpenGames.items.map((game) => (
                  <OpenGameCard
                    key={game.id}
                    t={t}
                    dictionary={dictionary}
                    game={game}
                    booking={bookingMap.get(game.bookingId)}
                    pitch={pitchMap.get(bookingMap.get(game.bookingId)?.pitchId ?? -1)}
                    organizer={userMap.get(game.organizerId)}
                    participants={game.participantIds.map((id) => userMap.get(id)).filter(Boolean) as User[]}
                    onEdit={() => openEdit('openGame', game)}
                    onDelete={() => void deleteEntity('openGame', game.id)}
                    onOpenBooking={() => {
                      setActiveView('bookings');
                      setPageByView((current) => ({ ...current, bookings: 0 }));
                    }}
                    onOpenPitch={() => {
                      setActiveView('pitches');
                      setPageByView((current) => ({ ...current, pitches: 0 }));
                    }}
                    onOpenUser={() => {
                      setActiveView('users');
                      setPageByView((current) => ({ ...current, users: 0 }));
                    }}
                  />
                ))}
              </CardGrid>
              <PaginationBar
                t={t}
                page={pagedOpenGames.page}
                pageSize={pagedOpenGames.pageSize}
                total={pagedOpenGames.total}
                onPageChange={(page) => setPageByView((current) => ({ ...current, openGames: page }))}
              />
            </section>
          )}

          {activeView === 'users' && (
            <section className="section-stack">
              <SectionHeader title={t.sectionUsers} subtitle={t.sectionUsersSub} addLabel={t.create} onAdd={() => openCreate('user')} />
              <CardGrid>
                {pagedUsers.items.map((user) => (
                  <UserCard
                    key={user.id}
                    t={t}
                    dictionary={dictionary}
                    user={user}
                    bookings={bookings.filter((booking) => booking.organizerId === user.id)}
                    openGames={openGames.filter((game) => game.organizerId === user.id || game.participantIds.includes(user.id))}
                    onEdit={() => openEdit('user', user)}
                    onDelete={() => void deleteEntity('user', user.id)}
                  />
                ))}
              </CardGrid>
              <PaginationBar
                t={t}
                page={pagedUsers.page}
                pageSize={pagedUsers.pageSize}
                total={pagedUsers.total}
                onPageChange={(page) => setPageByView((current) => ({ ...current, users: page }))}
              />
            </section>
          )}

          {activeView === 'equipment' && (
            <section className="section-stack">
              <SectionHeader title={t.sectionEquipment} subtitle={t.sectionEquipmentSub} addLabel={t.create} onAdd={() => openCreate('equipment')} />
              <CardGrid>
                {pagedEquipment.items.map((offer) => (
                  <EquipmentCard
                    key={offer.id}
                    t={t}
                    dictionary={dictionary}
                    offer={offer}
                    pitch={pitchMap.get(offer.pitchId)}
                    onEdit={() => openEdit('equipment', offer)}
                    onDelete={() => void deleteEntity('equipment', offer.id)}
                  />
                ))}
              </CardGrid>
              <PaginationBar
                t={t}
                page={pagedEquipment.page}
                pageSize={pagedEquipment.pageSize}
                total={pagedEquipment.total}
                onPageChange={(page) => setPageByView((current) => ({ ...current, equipment: page }))}
              />
            </section>
          )}
        </main>
      </div>

      {modal && (
        <EntityModal
          modal={modal}
          pitches={pitches}
          bookings={bookings}
          users={users}
          t={t}
          dictionary={dictionary}
          onChange={setModalValue}
          onClose={() => setModal(null)}
          onSubmit={submitModal}
        />
      )}

      {toast && (
        <div className="toast">
          <span>{toast}</span>
          <button type="button" onClick={() => setToast('')} aria-label={t.close}>
            <X size={16} />
          </button>
        </div>
      )}
    </div>
  );
}

function TopBar({
  activeView,
  language,
  t,
  dictionary,
  counts,
  onNavigate,
  onLanguageChange,
  onRefresh,
}: {
  activeView: ViewKey;
  language: Language;
  t: UiText;
  dictionary: Dictionary;
  counts: Record<ViewKey, number>;
  onNavigate: (view: ViewKey) => void;
  onLanguageChange: (language: Language) => void;
  onRefresh: () => void;
}) {
  return (
    <header className="topbar">
      <div className="brand">
        <div className="brand-mark">P</div>
        <div>
          <span>{t.brandCity}</span>
          <strong>{t.brandName}</strong>
        </div>
        <button type="button" className="top-action-button refresh-button-inline" onClick={onRefresh} aria-label={t.refresh}>
          <RefreshCw size={18} />
        </button>
      </div>
      <nav className="nav-list">
        {dictionary.navItems.map((item) => (
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
      <div className="top-actions">
        <button
          type="button"
          className="top-action-button language-button"
          onClick={() => onLanguageChange(language === 'ru' ? 'en' : 'ru')}
        >
          <Globe2 size={18} /> {language === 'ru' ? 'EN' : 'RU'}
        </button>
      </div>
    </header>
  );
}

function Hero({
  search,
  searching,
  t,
  onSearch,
  onSearchCommit,
}: {
  search: string;
  searching: boolean;
  t: UiText;
  onSearch: (value: string) => void;
  onSearchCommit: () => void;
}) {
  return (
    <section className="hero-card">
      <div className="hero-content">
        <div>
          <h1>{t.heroTitle}</h1>
          <p>{t.heroSubtitle}</p>
        </div>
      </div>
      <label className={searching ? 'hero-search searching' : 'hero-search'}>
        <Search size={18} />
        <input
          value={search}
          onChange={(event) => onSearch(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter') {
              onSearchCommit();
            }
          }}
          placeholder={t.searchPlaceholder}
        />
        {searching && <span className="search-spinner" aria-hidden="true" />}
      </label>
    </section>
  );
}

function Dashboard({
  t,
  dictionary,
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
  t: UiText;
  dictionary: Dictionary;
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
        <StatCard icon={<MapPin />} value={pitches.length} label={t.statsPitches} />
        <StatCard icon={<CalendarClock />} value={bookings.length} label={t.statsBookings} />
        <StatCard icon={<Trophy />} value={openGames.length} label={t.statsGames} />
        <StatCard icon={<Users />} value={users.length} label={t.statsUsers} />
        <StatCard icon={<Dumbbell />} value={equipmentOffers.length} label={t.statsEquipment} />
      </div>

      <div className="relationship-panel">
        <div>
          <span className="eyebrow">{t.allTogether}</span>
          <h2>{t.relationTitle}</h2>
          <p>{t.relationText}</p>
        </div>
        <div className="relation-grid">
          <RelationPill title={t.bookings} value={bookings.length} />
          <RelationPill title={t.gear} value={equipmentOffers.length} />
          <RelationPill title={t.participants} value={openGames.reduce((sum, game) => sum + game.participantIds.length, 0)} />
        </div>
      </div>

      <div className="section-title-row">
        <div>
          <h2>{t.popularPitches}</h2>
          <p>{t.popularSubtitle}</p>
        </div>
        <div className="action-row">
          <button type="button" className="ghost-button" onClick={onOpenPitches}>{t.openAll}</button>
          <button type="button" onClick={onCreatePitch}>
            <Plus size={16} />
            {t.addPitch}
          </button>
        </div>
      </div>

      <CardGrid>
        {pitches.slice(0, 8).map((pitch) => (
          <PitchCard
            key={pitch.id}
            t={t}
            dictionary={dictionary}
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
          <MiniBooking key={booking.id} t={t} dictionary={dictionary} booking={booking} pitch={pitchMap.get(booking.pitchId)} user={userMap.get(booking.organizerId)} />
        ))}
      </div>
    </section>
  );
}

function SectionHeader({
  title,
  subtitle,
  addLabel,
  onAdd,
}: {
  title: string;
  subtitle: string;
  addLabel: string;
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
        {addLabel}
      </button>
    </div>
  );
}

function BookingFilters({
  t,
  dictionary,
  filters,
  onChange,
  onSubmit,
  onReset,
}: {
  t: UiText;
  dictionary: Dictionary;
  filters: BookingSearchFilters;
  onChange: (filters: BookingSearchFilters) => void;
  onSubmit: (event: FormEvent) => void;
  onReset: () => void;
}) {
  return (
    <form className="filter-strip wide" onSubmit={onSubmit}>
      <label>
        {t.district}
        <input value={filters.district ?? ''} onChange={(event) => onChange({ ...filters, district: event.target.value })} />
      </label>
      <label>
        {t.type}
        <select value={filters.pitchType ?? ''} onChange={(event) => onChange({ ...filters, pitchType: event.target.value as BookingSearchFilters['pitchType'] })}>
          <option value="">{t.all}</option>
          {pitchTypes.map((type) => (
            <option key={type} value={type}>{dictionary.pitchTypes[type]}</option>
          ))}
        </select>
      </label>
      <label>
        {t.organizer}
        <input value={filters.organizerName ?? ''} onChange={(event) => onChange({ ...filters, organizerName: event.target.value })} />
      </label>
      <label>
        {t.status}
        <select value={filters.status ?? ''} onChange={(event) => onChange({ ...filters, status: event.target.value as BookingSearchFilters['status'] })}>
          <option value="">{t.all}</option>
          {bookingStatuses.map((status) => (
            <option key={status} value={status}>{dictionary.statuses[status]}</option>
          ))}
        </select>
      </label>
      <label>
        {t.start}
        <input
          type="datetime-local"
          value={filters.startFrom ?? ''}
          onChange={(event) => onChange({ ...filters, startFrom: event.target.value })}
        />
      </label>
      <label>
        {t.end}
        <input
          type="datetime-local"
          value={filters.startTo ?? ''}
          onChange={(event) => onChange({ ...filters, startTo: event.target.value })}
        />
      </label>
      <button type="submit">{t.filter}</button>
      <button type="button" className="ghost-button" onClick={onReset}>{t.reset}</button>
    </form>
  );
}

function PitchCard({
  t,
  dictionary,
  pitch,
  bookings,
  equipment,
  onEdit,
  onDelete,
}: {
  t: UiText;
  dictionary: Dictionary;
  pitch: Pitch;
  bookings: Booking[];
  equipment: EquipmentOffer[];
  onEdit: () => void;
  onDelete: () => void;
}) {
  return (
    <article className="catalog-card">
      <GradientCover title={pitch.name} seed={pitch.id} />
      <div className="muted">{dictionary.pitchTypes[pitch.type]}</div>
      <h3>
        <button type="button" className="link-button title-link" onClick={onEdit}>
          {pitch.name}
        </button>
      </h3>
      <p>
        <button type="button" className="link-button" onClick={onEdit}>
          {pitch.district} • {t.metro} {pitch.metro}
        </button>
      </p>
      <div className="price-line">
        <CircleDollarSign size={16} />
        {formatMoney(pitch.pricePerHour)} {t.priceHour}
      </div>
      <RelationLine label={t.bookings} value={bookings.length ? bookings.map((booking) => `#${booking.id} ${dictionary.statuses[booking.status]}`).join(', ') : t.noBookings} />
      <RelationLine label={t.inventory} value={equipment.length ? equipment.map((offer) => dictionary.equipment[offer.itemType]).join(', ') : t.noGear} />
      <CardActions t={t} onEdit={onEdit} onDelete={onDelete} />
    </article>
  );
}

function BookingCard({
  t,
  dictionary,
  booking,
  pitch,
  organizer,
  onEdit,
  onDelete,
  onOpenPitch,
  onOpenUser,
}: {
  t: UiText;
  dictionary: Dictionary;
  booking: Booking;
  pitch?: Pitch;
  organizer?: User;
  onEdit: () => void;
  onDelete: () => void;
  onOpenPitch: () => void;
  onOpenUser: () => void;
}) {
  return (
    <article className="catalog-card text-card">
      <StatusBadge status={booking.status} dictionary={dictionary} />
      <h3>
        <button type="button" className="link-button title-link" onClick={onEdit}>
          {t.booking} #{booking.id}
        </button>
      </h3>
      <p>
        <button type="button" className="link-button" onClick={onOpenPitch}>
          {pitch?.name ?? `${t.pitch} #${booking.pitchId}`}
        </button>
      </p>
      <RelationLine
        label={t.organizer}
        value={
          <button type="button" className="link-button" onClick={onOpenUser}>
            {organizer?.name ?? `${t.player} #${booking.organizerId}`}
          </button>
        }
      />
      <RelationLine label={t.time} value={`${new Date(booking.startAt).toLocaleString('ru-RU', {day:'2-digit', month:'2-digit', hour:'2-digit', minute:'2-digit'})} - ${new Date(booking.endAt).toLocaleString('ru-RU', {hour:'2-digit', minute:'2-digit'})}`} />      <CardActions t={t} onEdit={onEdit} onDelete={onDelete} />
    </article>
  );
}

function OpenGameCard({
  t,
  dictionary,
  game,
  booking,
  pitch,
  organizer,
  participants,
  onEdit,
  onDelete,
  onOpenBooking,
  onOpenPitch,
  onOpenUser,
}: {
  t: UiText;
  dictionary: Dictionary;
  game: OpenGame;
  booking?: Booking;
  pitch?: Pitch;
  organizer?: User;
  participants: User[];
  onEdit: () => void;
  onDelete: () => void;
  onOpenBooking: () => void;
  onOpenPitch: () => void;
  onOpenUser: () => void;
}) {
  return (
    <article className="catalog-card text-card">
      <StatusBadge status={game.status} dictionary={dictionary} />
      <h3>
        <button type="button" className="link-button title-link" onClick={onEdit}>
          {t.game} #{game.id}
        </button>
      </h3>
      <p>
        <button type="button" className="link-button" onClick={onOpenPitch}>
          {pitch?.name ?? `${t.pitch} #${booking?.pitchId ?? '?'}`}
        </button>
        {' • '}
        <button type="button" className="link-button" onClick={onOpenBooking}>
          {booking ? formatDate(booking.startAt) : `${t.booking} #${game.bookingId}`}
        </button>
      </p>
      <RelationLine
        label={t.organizer}
        value={
          <button type="button" className="link-button" onClick={onOpenUser}>
            {organizer?.name ?? `${t.player} #${game.organizerId}`}
          </button>
        }
      />
      <RelationLine label={t.skill} value={`${game.targetSkillMin}–${game.targetSkillMax}`} />
      <RelationLine label={t.limit} value={`${participants.length}/${game.maxPlayers} ${t.players}`} />
      <details className="participants-details">
        <summary className="participants-summary">
          {t.participants}: <b>{participants.length}</b>
        </summary>
        <div className="chip-row">
          {participants.length ? participants.map((user) => <span key={user.id}>{user.name}</span>) : <span>{t.noParticipants}</span>}
        </div>
      </details>
      <CardActions t={t} onEdit={onEdit} onDelete={onDelete} />
    </article>
  );
}

function UserCard({
  t,
  dictionary,
  user,
  bookings,
  openGames,
  onEdit,
  onDelete,
}: {
  t: UiText;
  dictionary: Dictionary;
  user: User;
  bookings: Booking[];
  openGames: OpenGame[];
  onEdit: () => void;
  onDelete: () => void;
}) {
  return (
    <article className="catalog-card text-card">
      <GradientCover title={user.name} seed={user.id + 30} compact />
      <h3>
        <button type="button" className="link-button title-link" onClick={onEdit}>
          {user.name}
        </button>
      </h3>
      <p>
        <button type="button" className="link-button" onClick={onEdit}>
          {dictionary.roles[user.role]} • {t.rating} {user.rating}
        </button>
      </p>
      <RelationLine label={t.organizes} value={`${bookings.length} ${t.bookings.toLowerCase()}`} />
      <RelationLine label={t.games} value={`${openGames.length} ${t.games.toLowerCase()}`} />
      <CardActions t={t} onEdit={onEdit} onDelete={onDelete} />
    </article>
  );
}

function EquipmentCard({
  t,
  dictionary,
  offer,
  pitch,
  onEdit,
  onDelete,
}: {
  t: UiText;
  dictionary: Dictionary;
  offer: EquipmentOffer;
  pitch?: Pitch;
  onEdit: () => void;
  onDelete: () => void;
}) {
  return (
    <article className="catalog-card text-card">
      <Dumbbell className="entity-icon" />
      <h3>
        <button type="button" className="link-button title-link" onClick={onEdit}>
          {dictionary.equipment[offer.itemType]}
        </button>
      </h3>
      <p>
        <button type="button" className="link-button" onClick={onEdit}>
          {pitch?.name ?? `${t.pitch} #${offer.pitchId}`}
        </button>
      </p>
      <RelationLine label={t.stock} value={`${offer.stockTotal} ${t.items}`} />
      <RelationLine label={t.price} value={`${formatMoney(offer.rentFixedPrice)} BYN`} />
      <CardActions t={t} onEdit={onEdit} onDelete={onDelete} />
    </article>
  );
}

function EntityModal({
  modal,
  pitches,
  bookings,
  users,
  t,
  dictionary,
  onChange,
  onClose,
  onSubmit,
}: {
  modal: ModalState;
  pitches: Pitch[];
  bookings: Booking[];
  users: User[];
  t: UiText;
  dictionary: Dictionary;
  onChange: (name: string, value: FormValue) => void;
  onClose: () => void;
  onSubmit: (event: FormEvent) => void;
}) {
  return (
    <div className="modal-backdrop">
      <form className="modal-card" onSubmit={onSubmit}>
        <div className="modal-head">
          <div>
            <span className="eyebrow">{modal.mode === 'create' ? t.createMode : t.editMode}</span>
            <h2>{modalTitle(modal.kind, t)}</h2>
          </div>
          <button type="button" className="icon-button" onClick={onClose} aria-label={t.close}>
            <X size={18} />
          </button>
        </div>

        <div className="form-grid">
          {modal.kind === 'pitch' && (
            <>
              <TextField label={t.name} name="name" value={fieldValue(modal.form.name)} onChange={onChange} />
              <SelectField label={t.type} name="type" value={fieldValue(modal.form.type)} options={pitchTypes.map((type) => [type, dictionary.pitchTypes[type]])} onChange={onChange} />
              <TextField label={t.district} name="district" value={fieldValue(modal.form.district)} onChange={onChange} />
              <TextField label={t.metro} name="metro" value={fieldValue(modal.form.metro)} onChange={onChange} />
              <MoneyField label={t.price} name="pricePerHour" value={fieldValue(modal.form.pricePerHour)} onChange={onChange} />
            </>
          )}

          {modal.kind === 'booking' && (
            <>
              <SelectField label={t.pitch} name="pitchId" value={fieldValue(modal.form.pitchId)} options={pitches.map((pitch) => [String(pitch.id), pitch.name])} onChange={onChange} />
              <SelectField label={t.organizer} name="organizerId" value={fieldValue(modal.form.organizerId)} options={users.map((user) => [String(user.id), user.name])} onChange={onChange} />
              <TextField label={t.start} name="startAt" type="datetime-local" value={fieldValue(modal.form.startAt)} onChange={onChange} />
              <TextField label={t.end} name="endAt" type="datetime-local" value={fieldValue(modal.form.endAt)} onChange={onChange} />
              <SelectField label={t.status} name="status" value={fieldValue(modal.form.status)} options={bookingStatuses.map((status) => [status, dictionary.statuses[status]])} onChange={onChange} />
            </>
          )}

          {modal.kind === 'openGame' && (
            <>
              <SelectField label={t.booking} name="bookingId" value={fieldValue(modal.form.bookingId)} options={bookings.map((booking) => [String(booking.id), `#${booking.id} ${booking.startAt}`])} onChange={onChange} />
              <SelectField label={t.organizer} name="organizerId" value={fieldValue(modal.form.organizerId)} options={users.map((user) => [String(user.id), user.name])} onChange={onChange} />
              <TextField label={t.skillFrom} name="targetSkillMin" type="number" min="0" max="100" value={fieldValue(modal.form.targetSkillMin)} onChange={onChange} />
              <TextField label={t.skillTo} name="targetSkillMax" type="number" min="0" max="100" value={fieldValue(modal.form.targetSkillMax)} onChange={onChange} />
              <TextField label={t.maxPlayers} name="maxPlayers" type="number" min="2" max="50" value={fieldValue(modal.form.maxPlayers)} onChange={onChange} />
              <SelectField label={t.status} name="status" value={fieldValue(modal.form.status)} options={openGameStatuses.map((status) => [status, dictionary.statuses[status]])} onChange={onChange} />
              <label className="field full-width">
                {t.participants}
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
              <TextField label={t.name} name="name" value={fieldValue(modal.form.name)} onChange={onChange} />
              <TextField label={t.rating} name="rating" type="number" min="0" max="100" value={fieldValue(modal.form.rating)} onChange={onChange} />
              <SelectField label={t.role} name="role" value={fieldValue(modal.form.role)} options={userRoles.map((role) => [role, dictionary.roles[role]])} onChange={onChange} />
            </>
          )}

          {modal.kind === 'equipment' && (
            <>
              <SelectField label={t.pitch} name="pitchId" value={fieldValue(modal.form.pitchId)} options={pitches.map((pitch) => [String(pitch.id), pitch.name])} onChange={onChange} />
              <SelectField label={t.type} name="itemType" value={fieldValue(modal.form.itemType)} options={equipmentTypes.map((type) => [type, dictionary.equipment[type]])} onChange={onChange} />
              <TextField label={t.quantity} name="stockTotal" type="number" min="0" value={fieldValue(modal.form.stockTotal)} onChange={onChange} />
              <MoneyField label={t.rentPrice} name="rentFixedPrice" value={fieldValue(modal.form.rentFixedPrice)} onChange={onChange} />
            </>
          )}
        </div>

        <div className="modal-actions">
          <button type="button" className="ghost-button" onClick={onClose}>{t.cancel}</button>
          <button type="submit">{t.save}</button>
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

function MoneyField({
  label,
  name,
  value,
  onChange,
}: {
  label: string;
  name: string;
  value: string;
  onChange: (name: string, value: string) => void;
}) {
  const numericValue = Number(value || 0);
  const updateValue = (nextValue: number) => {
    onChange(name, String(Math.max(1, Math.round(nextValue))));
  };

  return (
    <label className="field money-field">
      {label}
      <div className="money-control">
        <button type="button" className="stepper-button" onClick={() => updateValue(numericValue - 1)}>
          −
        </button>
        <input
          type="number"
          min="1"
          step="1"
          value={value}
          onChange={(event) => updateValue(Number(event.target.value))}
          required
        />
        <button type="button" className="stepper-button" onClick={() => updateValue(numericValue + 1)}>
          +
        </button>
      </div>
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

function paginate<T>(items: T[], page: number, pageSize: number) {
  const safePageSize = Math.max(1, Math.floor(pageSize));
  const total = items.length;
  const totalPages = Math.max(1, Math.ceil(total / safePageSize));
  const safePage = Math.min(Math.max(0, Math.floor(page)), totalPages - 1);
  const start = safePage * safePageSize;
  const end = start + safePageSize;
  return {
    page: safePage,
    pageSize: safePageSize,
    total,
    totalPages,
    items: items.slice(start, end),
  };
}

function PaginationBar({
  t,
  page,
  pageSize,
  total,
  onPageChange,
}: {
  t: UiText;
  page: number;
  pageSize: number;
  total: number;
  onPageChange: (page: number) => void;
}) {
  const totalPages = Math.max(1, Math.ceil(total / pageSize));
  const canPrev = page > 0;
  const canNext = page + 1 < totalPages;

  if (total <= pageSize) {
    return null;
  }

  return (
    <div className="pagination">
      <button type="button" className="ghost-button" disabled={!canPrev} onClick={() => onPageChange(page - 1)}>
        ←
      </button>
      <div className="pagination-note">
        {t.page} {page + 1} {t.of} {totalPages}
      </div>
      <button type="button" className="ghost-button" disabled={!canNext} onClick={() => onPageChange(page + 1)}>
        →
      </button>
    </div>
  );
}

function CardActions({ t, onEdit, onDelete }: { t: UiText; onEdit: () => void; onDelete: () => void }) {
  return (
    <div className="card-actions">
      <button type="button" className="icon-button" onClick={onEdit} aria-label={t.edit}>
        <Edit3 size={16} />
      </button>
      <button type="button" className="icon-button danger" onClick={onDelete} aria-label={t.delete}>
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
      {initials || 'ПЛ'}
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

function RelationLine({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div className="relation-line">
      <b>{label}</b>
      <span>{value}</span>
    </div>
  );
}

function MiniBooking({ t, dictionary, booking, pitch, user }: { t: UiText; dictionary: Dictionary; booking: Booking; pitch?: Pitch; user?: User }) {
  return (
    <div className="mini-booking">
      <CalendarClock size={18} />
      <div>
        <strong>{pitch?.name ?? `${t.pitch} #${booking.pitchId}`}</strong>
        <span>{user?.name ?? `${t.player} #${booking.organizerId}`} • {formatDate(booking.startAt)}</span>
      </div>
      <StatusBadge status={booking.status} dictionary={dictionary} />
    </div>
  );
}

function StatusBadge({ status, dictionary }: { status: string; dictionary: Dictionary }) {
  return <span className={`status-badge status-${status.toLowerCase()}`}>{dictionary.statuses[status as keyof typeof dictionary.statuses] ?? status}</span>;
}

function matchesSearch(values: Array<string | undefined>, search: string) {
  if (!search) {
    return true;
  }
  return values.some((value) => value?.toLowerCase().includes(search));
}

function normalizePitchForDisplay(pitch: Pitch): Pitch {
  if (!isGeneratedPitchName(pitch.name)) {
    return pitch;
  }
  const cleanName = cleanPitchNames[(pitch.id - 1) % cleanPitchNames.length];
  return {
    ...pitch,
    name: cleanName.name,
    district: cleanName.district,
    metro: cleanName.metro,
  };
}

function normalizeUserForDisplay(user: User): User {
  if (!isGeneratedUserName(user.name)) {
    return user;
  }
  return {
    ...user,
    name: cleanUserNames[(user.id - 1) % cleanUserNames.length],
  };
}

function isGeneratedPitchName(name: string) {
  return /^Tx Pitch /.test(name)
    || /^Bulk Tx Pitch /.test(name)
    || /^Cascade Demo Pitch/.test(name)
    || /\d{6,}/.test(name);
}

function isGeneratedUserName(name: string) {
  return /^Tx Organizer /.test(name) || /\d{6,}/.test(name);
}

function createDefaultForm(kind: EntityKind, pitches: Pitch[], bookings: Booking[], users: User[]): FormState {
  const tomorrow = new Date(Date.now() + 24 * 60 * 60 * 1000);
  tomorrow.setMinutes(0, 0, 0);
  const afterTomorrow = new Date(tomorrow.getTime() + 2 * 60 * 60 * 1000);
  const firstPitchId = String(pitches[0]?.id ?? '');
  const firstBookingId = String(bookings[0]?.id ?? '');
  const firstUserId = String(users[0]?.id ?? '');

  if (kind === 'pitch') {
    return { name: 'Новая арена', type: 'FIVE_TURF', district: 'Центральный', metro: 'Немига', pricePerHour: '120' };
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
  return { pitchId: firstPitchId, itemType: 'BALL', stockTotal: '8', rentFixedPrice: '10' };
}

function createEditForm(kind: EntityKind, item: Pitch | Booking | OpenGame | User | EquipmentOffer): FormState {
  if (kind === 'pitch') {
    const pitch = item as Pitch;
    return {
      name: pitch.name,
      type: pitch.type,
      district: pitch.district,
      metro: pitch.metro,
      pricePerHour: toWholeMoney(pitch.pricePerHour),
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
    rentFixedPrice: toWholeMoney(offer.rentFixedPrice),
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

function modalTitle(kind: EntityKind, t: UiText) {
  if (kind === 'pitch') {
    return t.pitch;
  }
  if (kind === 'booking') {
    return t.booking;
  }
  if (kind === 'openGame') {
    return t.game;
  }
  if (kind === 'user') {
    return t.player;
  }
  return t.inventory;
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

function toWholeMoney(value: number) {
  return String(Math.round(Number(value)));
}

export default App;
