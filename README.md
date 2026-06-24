# MediCare-Clinic

> Sistem de management al clinicii medicale — aplicație web dezvoltată cu Spring Boot și React.

**Autori:** Țelicov Letiția (405) · Stan Bianca (408) · Iunie 2026

---

## Descrierea proiectului

MediCare Clinic este o aplicație web full-stack pentru administrarea unei clinici medicale.

## Tehnologii

**Backend**

| Tehnologie | Versiune |
|---|---|
| Java | 17 |
| Spring Boot | 4.0.6 |
| Spring Security | 6 |
| Spring Data JPA / Hibernate | 7.2 |
| PostgreSQL | 18 (profil `dev`) |
| H2 | in-memory (profil `test`) |
| Lombok | latest |
| Maven | 3.x |

**Frontend**

| Tehnologie | Versiune |
|---|---|
| React | 18 |
| Vite | latest |
| React Router DOM | 6 |
| Axios | latest |
| Lucide React | ^1.21.0 |

---

## Arhitectură

### Vedere de ansamblu

```
┌─────────────────────────────────────────────────────┐
│                   Browser (React 18)                │
│              http://localhost:5173                  │
│                                                     │
│  ┌──────────┐  ┌──────────┐  ┌───────────────────┐ │
│  │  Pages   │  │Components│  │   API modules     │ │
│  │ (9 pg.)  │  │(Sidebar, │  │ (auth, appoint-   │ │
│  │          │  │ Modals,  │  │  ments, doctors,  │ │
│  │          │  │PrivRoute)│  │  patients, etc.)  │ │
│  └──────────┘  └──────────┘  └───────────────────┘ │
└────────────────────────┬────────────────────────────┘
                         │ HTTP + cookie sesiune
                         │ (JSESSIONID / remember-me)
┌────────────────────────▼────────────────────────────┐
│              Spring Boot (port 8080)                │
│                                                     │
│  ┌─────────────────────────────────────────────┐   │
│  │          SecurityFilterChain                │   │
│  │  CORS · csrf.disable() · hasRole() · BCrypt │   │
│  └──────────────────┬──────────────────────────┘   │
│                     │                               │
│  ┌──────────────────▼──────────────────────────┐   │
│  │         REST Controllers (@RestController)  │   │
│  └──────────────────┬──────────────────────────┘   │
│                     │                               │
│  ┌──────────────────▼──────────────────────────┐   │
│  │    Service Layer (@Service @Transactional)  │   │
│  │  logică business · validări · conflict check│   │
│  └──────────────────┬──────────────────────────┘   │
│                     │                               │
│  ┌──────────────────▼──────────────────────────┐   │
│  │        Repository (JpaRepository)           │   │
│  └──────────────────┬──────────────────────────┘   │
└────────────────────-┼───────────────────────────────┘
                      │
          ┌───────────┴───────────┐
          │                       │
   ┌──────▼──────┐       ┌────────▼───────┐
   │ PostgreSQL  │       │  H2 in-memory  │
   │  (dev)      │       │   (test)       │
   └─────────────┘       └────────────────┘
```

### Fluxul de autentificare

```
POST /api/auth/login (form params: username, password)
          │
          ▼
CustomUserDetailsService.loadUserByUsername()
          │
          ▼
BCryptPasswordEncoder.matches()
          │
     ┌────┴─────┐
     │ succes   │ eșec
     ▼          ▼
JSON { username, roles[] }    JSON { error, message }
HTTP 200                      HTTP 401
+ Set-Cookie: JSESSIONID
```

### Modelul de date

```
User ──────────────────────────── Role
 │  @ManyToMany (user_roles)
 │
 │ @OneToOne
 ▼
DoctorProfile ─────────────────── Specialty
      │  @ManyToOne               (specialty_id FK)
      │
      │ @OneToMany (CascadeALL, orphanRemoval)
      ▼
DoctorAvailability


User (doctor) ◄──── Appointment ────► Patient
               @ManyToOne  @ManyToOne
                    │
                    │ @ManyToOne (opțional)
                    ▼
              Prescription ──────────► Patient
                            @ManyToOne
                            └─────────► User (doctor)


Patient ──────────────── MedicalRecord
          @OneToOne      (patient_id FK UNIQUE)
```

---

## Structura proiectului

```
MediCare-Clinic/
├── backend/
│   ├── src/main/java/com/medicareclinic/backend/
│   │   ├── config/             # CorsConfig, DataInitializer
│   │   ├── controller/         # REST controllers
│   │   ├── dto/                # Request / Response DTOs
│   │   ├── exception/          # GlobalExceptionHandler
│   │   ├── model/              # Entități JPA
│   │   ├── repository/         # Spring Data JPA repositories
│   │   ├── security/           # SecurityConfig, CustomUserDetailsService
│   │   └── service/            # Business logic
│   └── src/main/resources/
│       ├── application.properties      # profil activ, setări globale
│       ├── application-dev.yml         # PostgreSQL, DEBUG logging
│       ├── application-test.yml        # H2 in-memory, test config
│       └── logback-spring.xml          # 3 appenders, nivele per profil
│
└── frontend/
    └── src/
        ├── api/                # Module Axios (auth, appointments, doctors...)
        ├── components/         # Sidebar, PrivateRoute, Modale
        ├── context/            # AuthContext
        └── pages/              # LoginPage, Dashboard, DoctorsPage...
```

---

## Instrucțiuni de instalare

### Cerințe prealabile

- Java 17+
- Maven 3.8+
- Node.js 18+
- PostgreSQL 14+ *(doar pentru profilul `dev`)*

---

### Backend

**1. Clonează repository-ul**
```bash
git clone https://github.com/Bianca1289/MediCare-Clinic.git
cd MediCare-Clinic/backend
```

**2. Creează baza de date**
```sql
CREATE DATABASE medicareclinic;
```

**3. Verifică / modifică credențialele** în `src/main/resources/application-dev.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/medicareclinic
    username: postgres
    password: postgres
```

**4. Pornește aplicația**
```bash
mvn spring-boot:run
```

Backend-ul pornește pe **http://localhost:8080**

> La prima pornire, `DataInitializer` creează automat: 4 roluri, 15 utilizatori demo, 12 specialități medicale și 11 profiluri de medici cu disponibilitate săptămânală.

---

### Rulare fără PostgreSQL (H2 in-memory)

Dacă nu ai PostgreSQL instalat, schimbă profilul activ în `application.properties`:

```properties
spring.profiles.active=test
```

Aplicația pornește cu H2 in-memory. Datele se resetează la fiecare restart.

---

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend-ul pornește pe **http://localhost:5173**

---

### Profile disponibile

| Profil | Bază de date | DDL | Activare |
|---|---|---|---|
| `dev` *(implicit)* | PostgreSQL `localhost:5432/medicareclinic` | `update` | implicit |
| `test` | H2 in-memory | `create-drop` | `spring.profiles.active=test` sau `@ActiveProfiles("test")` |

---

## Documentație API

Toate endpoint-urile returnează JSON. Erorile urmează formatul:

```json
{
  "timestamp": "2026-06-23T12:00:00",
  "status": 400,
  "error": "Validation failed",
  "fieldErrors": {
    "fullName": "must not be blank"
  }
}
```

| Cod HTTP | Semnificație |
|---|---|
| `200 / 201` | Succes |
| `400` | Date invalide / eroare de validare |
| `401` | Neautentificat |
| `403` | Rol insuficient |
| `404` | Resursă negăsită |
| `409` | Conflict (username duplicat, conflict orar) |
| `500` | Eroare internă |

---

## Rularea testelor

```bash
cd backend

# Toate testele
mvn test

# Doar teste unitare
mvn test -Dtest="*ServiceTest"

# Doar teste de integrare
mvn test -Dtest="*IntegrationTest"
```

| Tip | Adnotări | Bază de date |
|---|---|---|
| Unitare | `@ExtendWith(MockitoExtension.class)` | Fără context Spring |
| Integrare | `@SpringBootTest` + `@ActiveProfiles("test")` | H2 in-memory |

---

## Logging

| Fișier | Conținut |
|---|---|
| `backend/logs/app.log` | Toate nivelele (INFO, DEBUG, WARN, ERROR) |
| `backend/logs/error.log` | Doar ERROR — fișier separat pentru monitorizare |

Configurare: `src/main/resources/logback-spring.xml`

| Profil | Nivel aplicație | Nivel Security |
|---|---|---|
| `dev` | DEBUG | INFO |
| `test` | INFO | WARN |
