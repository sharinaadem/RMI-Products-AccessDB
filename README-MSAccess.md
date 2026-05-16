# RMI + MS Access Database Activity
## Data Pipeline: `products.accdb` → Server → RMI → Client

---

## Project Structure

```
RMI/
├── lib/                          ← UCanAccess JAR files (download below)
│   ├── ucanaccess-5.0.1.jar
│   ├── jackcess-4.0.5.jar
│   ├── commons-lang-2.6.jar
│   ├── commons-logging-1.2.jar
│   └── hsqldb-2.7.1.jar
│
├── server/
│   ├── products.accdb            ← MS Access database (you create this)
│   ├── Product.java              ← Remote interface
│   ├── ProductImpl.java          ← Remote object implementation
│   ├── AccessDBLoader.java       ← JDBC reader for MS Access
│   └── Server.java               ← RMI server (reads DB, binds objects)
│
└── client/
    ├── Product.java              ← Remote interface (mirrors server)
    └── Client.java               ← RMI client (displays data)
```

---

## STEP 1 — Download UCanAccess JARs

Download these 5 JARs and place them all inside the `RMI/lib/` folder:

| JAR | Download URL |
|-----|-------------|
| ucanaccess-5.0.1.jar | https://repo1.maven.org/maven2/net/sf/ucanaccess/ucanaccess/5.0.1/ucanaccess-5.0.1.jar |
| jackcess-4.0.5.jar | https://repo1.maven.org/maven2/com/healthmarketscience/jackcess/jackcess/4.0.5/jackcess-4.0.5.jar |
| commons-lang-2.6.jar | https://repo1.maven.org/maven2/commons-lang/commons-lang/2.6/commons-lang-2.6.jar |
| commons-logging-1.2.jar | https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar |
| hsqldb-2.7.1.jar | https://repo1.maven.org/maven2/org/hsqldb/hsqldb/2.7.1/hsqldb-2.7.1.jar |

---

## STEP 2 — Create the MS Access Database

1. Open **Microsoft Access**
2. Create a new **Blank Database**, save it as `products.accdb` inside `RMI/server/`
3. Go to **Create → Table Design** and add these columns:

| Field Name  | Data Type   | Notes           |
|-------------|-------------|-----------------|
| ID          | AutoNumber  | Primary Key     |
| Name        | Short Text  |                 |
| Description | Short Text  |                 |
| Price       | Number (Double) |             |

4. Save the table as **Products**
5. Switch to **Datasheet View** and enter these rows:

| Name    | Description                                        | Price    |
|---------|----------------------------------------------------|----------|
| Laptop  | Lenovo ThinkPad X1 Carbon - 14 inch Intel Core i7 | 800000.0 |
| Mobile  | Xiaomi Mi 9 - 6.39 inch AMOLED Snapdragon 855     | 24000.0  |
| Bag     | Samsonite Premium Laptop Bag - Water Resistant     | 800.0    |
| Charger | Xiaomi 65W Fast Charger - USB-C GaN Technology    | 230.0    |

---

## STEP 3 — Compile (run from the `RMI/` root folder)

### Windows (PowerShell or CMD):
```powershell
javac -cp "lib/*" server/Product.java server/ProductImpl.java server/AccessDBLoader.java server/Server.java client/Product.java client/Client.java
```

### macOS / Linux (Terminal):
```bash
javac -cp "lib/*" server/Product.java server/ProductImpl.java server/AccessDBLoader.java server/Server.java client/Product.java client/Client.java
```

---

## STEP 4 — Run the Server (Terminal 1)

### Windows:
```powershell
java -cp ".;lib/*" server.Server
```

### macOS / Linux:
```bash
java -cp ".:lib/*" server.Server
```

### Expected Server Output:
```
╔══════════════════════════════════════════╗
║        RMI SERVER — MS ACCESS DB         ║
╚══════════════════════════════════════════╝

[Server] Step 1 — Loading data from MS Access...
[AccessDBLoader] Connecting to MS Access DB: server/products.accdb
[AccessDBLoader] Connected. Executing query...
[AccessDBLoader] Row 1 -> Laptop     | Lenovo ThinkPad X1 Carbon...    | PHP 800000.00
[AccessDBLoader] Row 2 -> Mobile     | Xiaomi Mi 9...                  | PHP 24000.00
[AccessDBLoader] Row 3 -> Bag        | Samsonite Premium Laptop Bag... | PHP 800.00
[AccessDBLoader] Row 4 -> Charger    | Xiaomi 65W Fast Charger...      | PHP 230.00
[AccessDBLoader] Total records loaded: 4

[Server] Step 2 — RMI Registry started on port 9000
[Server] Step 3 — Binding products to RMI Registry...
[Server] Bound -> "laptop"
[Server] Bound -> "mobile"
[Server] Bound -> "bag"
[Server] Bound -> "charger"

[Server] ✓ All products registered successfully.
[Server] Pipeline: MS Access → Server → RMI → Client
[Server] Waiting for client connections on port 9000...
```

---

## STEP 5 — Run the Client (Terminal 2)

### Windows:
```powershell
java -cp ".;lib/*" client.Client
```

### macOS / Linux:
```bash
java -cp ".:lib/*" client.Client
```

### Expected Client Output:
```
╔══════════════════════════════════════════╗
║        RMI CLIENT — MS ACCESS DB         ║
╚══════════════════════════════════════════╝
[Client] Connecting to RMI Registry at 127.0.0.1:9000...
[Client] Connection established.

╔══════════════════════════════════════════════════════════════════════╗
║               PRODUCTS  (Source: MS Access Database)                ║
╠══════════╦═══════════════════════════════════════════════╦═══════════╣
║ NAME     ║ DESCRIPTION                                   ║ PRICE(PHP)║
╠══════════╬═══════════════════════════════════════════════╬═══════════╣
║ Laptop   ║ Lenovo ThinkPad X1 Carbon - 14 inch...        ║ 800000.00 ║
║ Mobile   ║ Xiaomi Mi 9 - 6.39 inch AMOLED...            ║  24000.00 ║
║ Bag      ║ Samsonite Premium Laptop Bag...               ║    800.00 ║
║ Charger  ║ Xiaomi 65W Fast Charger...                    ║    230.00 ║
╚══════════╩═══════════════════════════════════════════════╩═══════════╝

[Client] ✓ Data successfully received from Server.
[Client] Pipeline: MS Access → Server → RMI → Client
```

---

## Key Concepts

### Why UCanAccess?
UCanAccess is a pure-Java JDBC driver that reads `.accdb` and `.mdb` files
directly — no need to install MS Access on the server machine, no ODBC setup.
It wraps the Jackcess library and exposes a standard JDBC interface.

### JDBC Connection String
```java
"jdbc:ucanaccess://server/products.accdb;memory=false"
```
- `memory=false` — keeps the DB on disk (good for large files).
- `memory=true` — loads entire DB into RAM (faster for small files).

### How the Pipeline Works
1. `AccessDBLoader` opens a JDBC connection to `products.accdb`
2. Executes `SELECT Name, Description, Price FROM Products`
3. Maps each row into a `ProductData` DTO object
4. `Server` creates one `ProductImpl` per row and binds it to the RMI Registry
5. `Client` does `registry.lookup("laptop")` etc. and calls remote methods
6. Data appears in the client terminal — **all originating from MS Access**
