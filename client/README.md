# Football Pitch Marketplace Client

SPA-клиент для лабораторной работы 7. Интерфейс работает с существующим Spring Boot API и показывает CRUD, фильтрацию, OneToMany и ManyToMany связи.

## Запуск

```bash
cd /Users/saku10/Desktop/Java/client
npm install
npm run dev
```

Backend должен быть запущен отдельно на `http://localhost:8080`.

Если API запущен на другом адресе, создай `.env.local`:

```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

Клиент откроется на `http://localhost:5173`.
